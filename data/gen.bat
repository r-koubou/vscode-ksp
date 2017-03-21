@echo off

set PYTHON3=C:\Python\3.x\python.exe

%PYTHON3% Excel2CompleteCommands.py KSP.xlsx
%PYTHON3% Excel2CompleteVariables.py KSP.xlsx
%PYTHON3% Excel2Snippet.py KSP.xlsx
