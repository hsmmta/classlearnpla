@echo off
REM 绕过 PowerShell 执行策略限制，调用 run-dev.ps1
cd /d "%~dp0.."
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-dev.ps1" %*
