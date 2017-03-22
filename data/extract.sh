#!/bin/bash

python ExtractCallbackFromManual.py > extract_callback.ksp
python ExtractCommandFromManual.py > extract_command.ksp
python ExtractVariableFromManual.py > extract_variables.ksp
