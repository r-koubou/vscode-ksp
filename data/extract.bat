@echo off

call pythonenv.bat

%PYTHON3% ExtractCallbackFromManual.py > extract_callback.ksp
%PYTHON3% ExtractCommandFromManual.py > extract_command.ksp
%PYTHON3% ExtractVariableFromManual.py > extract_variables.ksp
