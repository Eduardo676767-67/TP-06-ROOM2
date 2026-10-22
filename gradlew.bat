@echo off
setlocal
set "GRADLE_VERSION=8.9"
set "CACHE_DIR=%USERPROFILE%\.gradle\approomproduto-wrapper"
set "GRADLE_HOME=%CACHE_DIR%\gradle-%GRADLE_VERSION%"
set "ZIP_PATH=%CACHE_DIR%\gradle-%GRADLE_VERSION%-bin.zip"

if exist "%GRADLE_HOME%\bin\gradle.bat" goto runGradle
if not exist "%CACHE_DIR%" mkdir "%CACHE_DIR%"
if not exist "%ZIP_PATH%" (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%ZIP_PATH%'"
  if errorlevel 1 exit /b 1
)
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Expand-Archive -LiteralPath '%ZIP_PATH%' -DestinationPath '%CACHE_DIR%' -Force"
if errorlevel 1 exit /b 1

:runGradle
call "%GRADLE_HOME%\bin\gradle.bat" %*
set "RESULT=%ERRORLEVEL%"
endlocal & exit /b %RESULT%
