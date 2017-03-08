@echo off

set PYTHON2=C:\Python\2.x\python.exe

%PYTHON2% Excel2CompleteCommands.py KSP.xlsx > cmd.txt
%PYTHON2% Excel2CompleteVariables.py KSP.xlsx > var.txt
%PYTHON2% Excel2Snippet.py KSP.xlsx > sni.txt
