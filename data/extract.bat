@echo off

set PYTHON3=C:\Python\3.x\python.exe

%PYTHON3% ExtractCallbackFromManual.py > extract_callback.ksp
%PYTHON3% ExtractCommandFromManual.py > extract_command.ksp
