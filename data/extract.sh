#!/bin/bash

python3 ExtractCallbackFromManual.py > extract_callback.ksp
python3 ExtractCommandFromManual.py > extract_command.ksp
python3 ExtractVariableFromManual.py > extract_variables.ksp
