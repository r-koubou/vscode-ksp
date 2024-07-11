#!/bin/bash

MANUAL="KSP Reference Manual(7.10.0).txt"

pipenv run python extractor/manual/ExtractCallbackFromManual.py "$MANUAL" extract_callback.txt extract_callback_dump.txt
pipenv run python extractor/manual/ExtractCommandFromManual.py "$MANUAL" extract_command.txt extract_command_dump.txt
pipenv run python extractor/manual/ExtractVariableFromManual.py "$MANUAL" extract_variables.txt extract_variables_dump.txt
