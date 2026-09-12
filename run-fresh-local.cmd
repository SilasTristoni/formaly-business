@echo off
setlocal
cd /d "%~dp0"

echo.
echo ==========================================
echo Formaly Business - ambiente local zerado
echo ==========================================
echo.
echo ATENCAO: este comando apaga o banco H2 local usado pelo perfil demo-h2.
echo Use somente quando quiser recomecar o teste do zero.
echo.
set /p CONFIRM=Digite S para apagar o banco e continuar: 
if /I not "%CONFIRM%"=="S" (
  echo Operacao cancelada.
  exit /b 0
)

echo.
echo Removendo banco H2 local de demonstracao...
del /Q "target\formaly-business-demo*" 2>nul

set APP_DEMO_SEED_ENABLED=false

echo Seed demonstrativo: DESATIVADO
echo Primeira execucao: HABILITADA para banco vazio
echo.
echo Ao iniciar, acesse:
echo http://localhost:8080/login.html
echo.

call mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo-h2"
endlocal
