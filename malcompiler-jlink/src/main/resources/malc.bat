@echo off

set BIN_DIR=%~dp0
set BIN_DIR=%BIN_DIR:~0,-1%
for %%F in ("%BIN_DIR%") do set RUNTIME_DIR=%%~dpF
set RUNTIME_DIR=%RUNTIME_DIR:~0,-1%

echo "%BIN_DIR%\java" ^
  --module-path "%RUNTIME_DIR%\dependencies" ^
  --module "org.mal_lang.compiler.cli" ^
  %*
