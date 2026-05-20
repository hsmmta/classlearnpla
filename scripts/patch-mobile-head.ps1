$ErrorActionPreference = "Stop"
$root = Join-Path $PSScriptRoot "..\src\main\webapp"
$viewport = '    <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">'
$marker = '<meta charset="UTF-8">'
$jspLink = '    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/mobile.css">'
$htmlLink = '    <link rel="stylesheet" href="../css/mobile.css">'

Get-ChildItem -Path $root -Recurse -Include '*.jsp','*.html' | ForEach-Object {
    $content = Get-Content $_.FullName -Raw -Encoding UTF8
    if ($content -match 'viewport-fit=cover') {
        Write-Host "Skip: $($_.Name)"
        return
    }
    if ($content -notmatch [regex]::Escape($marker)) {
        Write-Host "No charset: $($_.FullName)"
        return
    }
    $link = if ($_.Extension -eq '.jsp') { $jspLink } else { $htmlLink }
    $insert = "$marker`n$viewport`n$link"
    $newContent = $content.Replace($marker, $insert)
    [System.IO.File]::WriteAllText($_.FullName, $newContent, [System.Text.UTF8Encoding]::new($false))
    Write-Host "Updated: $($_.Name)"
}
