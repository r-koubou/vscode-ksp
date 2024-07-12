@echo off

setlocal

set VERSION=7.10.0
set MANUAL=KSP Reference Manual(%VERSION%).txt

set OUTPUT_DIR=data\extracted

mkdir %OUTPUT_DIR%

pipenv run python extractor\manual\ExtractCallbackFromManual.py "%MANUAL%" %OUTPUT_DIR%\extract_callback.txt  %OUTPUT_DIR%\extract_callback_dump.txt
pipenv run python extractor\manual\ExtractCommandFromManual.py "%MANUAL%"  %OUTPUT_DIR%\extract_command.txt   %OUTPUT_DIR%\extract_command_dump.txt
pipenv run python extractor\manual\ExtractVariableFromManual.py "%MANUAL%" %OUTPUT_DIR%\extract_variables.txt %OUTPUT_DIR%\extract_variables_dump.txt

endlocal
