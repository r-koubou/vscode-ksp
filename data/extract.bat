@echo off

setlocal

set MANUAL=KSP Reference Manual(6.7.0).txt

pipenv run python ExtractCallbackFromManual.py "%MANUAL%" > extract_callback.txt
pipenv run python ExtractCommandFromManual.py "%MANUAL%" > extract_command.txt
pipenv run python ExtractVariableFromManual.py "%MANUAL%" > extract_variables.txt

endlocal
