#!/bin/bash

MANUAL="KSP Reference Manual(6.0.2).txt"

python3 ExtractCallbackFromManual.py "$MANUAL" > extract_callback.txt
python3 ExtractCommandFromManual.py "$MANUAL" > extract_command.txt
python3 ExtractVariableFromManual.py "$MANUAL" > extract_variables.txt
