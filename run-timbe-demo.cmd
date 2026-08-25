@echo off
setlocal
cd /d "%~dp0"
echo.
echo Iniciando demo estatica da Timbe...
echo.
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\build-timbe-pages-local.ps1"
if errorlevel 1 (
  echo.
  echo Falha ao iniciar a demo.
  pause
  exit /b 1
)
endlocal
