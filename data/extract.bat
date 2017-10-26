@echo off

python ExtractCallbackFromManual.py > extract_callback.txt
python ExtractCommandFromManual.py > extract_command.txt
python ExtractVariableFromManual.py > extract_variables.txt
