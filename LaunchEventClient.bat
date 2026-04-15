@echo off
echo ===================================================
echo   Starting the Data Logger Vault Client...
echo   Please wait while the code compiles.
echo ===================================================

cd /d "%~dp0"
echo Launching RuneLite...
call gradlew.bat run

pause
