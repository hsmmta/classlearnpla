# Dev server: JDK 17+ + embedded Tomcat 9 (cargo:run)
$ErrorActionPreference = "Stop"
$projectRoot = Split-Path $PSScriptRoot -Parent
Set-Location $projectRoot

function Import-DotEnvFile([string]$Path) {
    if (-not (Test-Path $Path)) { return }
    Get-Content $Path -Encoding UTF8 | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq '' -or $line.StartsWith('#')) { return }
        $eq = $line.IndexOf('=')
        if ($eq -le 0) { return }
        $key = $line.Substring(0, $eq).Trim().Trim([char]0xFEFF)
        $val = $line.Substring($eq + 1).Trim().Trim('"').Trim("'")
        [Environment]::SetEnvironmentVariable($key, $val, 'Process')
    }
}
Import-DotEnvFile (Join-Path $projectRoot '.env')

function Find-JdkHome {
    if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
        $v = & "$env:JAVA_HOME\bin\java.exe" -version 2>&1 | Out-String
        if ($v -match 'version "(\d+)') {
            $major = [int]$Matches[1]
            if ($major -ge 17) { return $env:JAVA_HOME }
        }
    }
    $candidates = @(
        "C:\Program Files\Java\jdk-21",
        "C:\Program Files\Java\jdk-17",
        "C:\Program Files\Eclipse Adoptium\jdk-21*",
        "C:\Program Files\Eclipse Adoptium\jdk-17*",
        "C:\Program Files\Neo4j Desktop 2\resources\offline\runtime\zulu21.44.17-ca-jdk21.0.8-win_x64",
        "C:\Program Files\Neo4j Desktop 2\resources\offline\runtime\zulu17.60.17-ca-jdk17.0.16-win_x64"
    )
    foreach ($pattern in $candidates) {
        $dirs = @(Get-Item $pattern -ErrorAction SilentlyContinue)
        foreach ($dir in $dirs) {
            if (Test-Path "$($dir.FullName)\bin\java.exe") {
                return $dir.FullName
            }
        }
    }
    return $null
}

function Find-MvnCmd {
    $cmd = Get-Command mvn -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    if ($env:MAVEN_HOME -and (Test-Path "$env:MAVEN_HOME\bin\mvn.cmd")) {
        return "$env:MAVEN_HOME\bin\mvn.cmd"
    }
    $candidates = @(
        "E:\idea\IntelliJ IDEA*\plugins\maven\lib\maven3\bin\mvn.cmd",
        "C:\Program Files\JetBrains\IntelliJ IDEA*\plugins\maven\lib\maven3\bin\mvn.cmd",
        "C:\apache-maven*\bin\mvn.cmd",
        "$env:USERPROFILE\apache-maven*\bin\mvn.cmd"
    )
    foreach ($pattern in $candidates) {
        $found = @(Get-Item $pattern -ErrorAction SilentlyContinue | Sort-Object FullName -Descending)
        if ($found.Count -gt 0) { return $found[0].FullName }
    }
    return $null
}

function Get-ListenerPid([int]$Port) {
    try {
        $conn = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction Stop | Select-Object -First 1
        if ($conn) { return $conn.OwningProcess }
    } catch { }
    $netstat = Join-Path $env:SystemRoot "System32\netstat.exe"
    if (-not (Test-Path $netstat)) { return $null }
    $lines = & $netstat -ano | Select-String ":$Port\s" | Select-String "LISTENING"
    foreach ($line in $lines) {
        if ($line -match '\s+(\d+)\s*$') { return [int]$Matches[1] }
    }
    return $null
}

function Free-DevPort([int]$Port) {
    $listenerPid = Get-ListenerPid $Port
    if (-not $listenerPid) { return $true }
    $proc = Get-Process -Id $listenerPid -ErrorAction SilentlyContinue
    $name = if ($proc) { $proc.ProcessName } else { "unknown" }
    if ($name -notmatch '^(java|javaw)$') {
        Write-Host "Port $Port is used by $name (PID $listenerPid). Stop it manually or set DEV_PORT to another port." -ForegroundColor Red
        return $false
    }
    Write-Host "Port $Port in use by $name (PID $listenerPid). Stopping..."
    Stop-Process -Id $listenerPid -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
    if (Get-ListenerPid $Port) {
        Write-Host "Port $Port still in use. Try: taskkill /PID $listenerPid /F" -ForegroundColor Red
        return $false
    }
    return $true
}

$jdk = Find-JdkHome
if (-not $jdk) {
    Write-Host "JDK 17+ not found. Install JDK 21 and set JAVA_HOME, or run cargo:run from IntelliJ." -ForegroundColor Red
    exit 1
}

$env:JAVA_HOME = $jdk
$system32 = Join-Path $env:SystemRoot "System32"
$pathParts = @("$jdk\bin", $system32) + ($env:Path -split ';' | Where-Object {
    $_ -and $_ -notmatch '\\Java\\jre' -and $_ -notmatch '\\Java\\jdk'
})
$env:Path = ($pathParts | Select-Object -Unique) -join ';'
Write-Host "Using JAVA_HOME=$jdk"

$mvn = Find-MvnCmd
if (-not $mvn) {
    Write-Host "Maven (mvn) not found in PATH. Install Maven or run from IntelliJ terminal." -ForegroundColor Red
    exit 1
}
$mvnDir = Split-Path $mvn -Parent
$env:Path = "$mvnDir;$env:Path"
Write-Host "Using MVN=$mvn"

$devPort = if ($env:DEV_PORT) { [int]$env:DEV_PORT } else { 8080 }
if (-not (Free-DevPort $devPort)) { exit 1 }
Write-Host "Starting Tomcat on port $devPort..."
Write-Host "When ready, open: http://localhost:$devPort/"

& $mvn clean package "cargo:run" "-Dcargo.servlet.port=$devPort" @args
