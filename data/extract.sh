#!/bin/bash

python3 ExtractCallbackFromManual.py > extract_callback.txt
python3 ExtractCommandFromManual.py > extract_command.txt
python3 ExtractVariableFromManual.py > extract_variables.txt
