#!/bin/bash

pipenv run python spreadsheet/Excel2Completion.py ../KSP_Completion.xlsx ../../src/features/generated/
pipenv run python spreadsheet/Excel2Snippet.py

./extract.sh

echo  "# Generate Command Name Array : ../src/features/generated/KSPCommandNames.ts"
pipenv run python VerifyExtractedManualData.py extract_command.txt > /dev/null
pipenv run python Text2TSArray.py  __mergeed__.txt \
                        ../src/features/generated/KSPCommandNames.ts \
                        CommandNames \
                        ui_knob \
                        ui_label \
                        ui_slider \
                        ui_table \
                        ui_value_edit \
                        ui_waveform

echo  "# Generate Builtin Variable Name Array : ../src/features/generated/KSPBuiltinVariableNames.ts"
pipenv run python VerifyExtractedManualData.py extract_variables.txt -v > /dev/null
pipenv run python Text2TSArray.py  __mergeed__.txt \
                        ../src/features/generated/KSPBuiltinVariableNames.ts \
                        BuiltinVariableNames

rm ./__mergeed__.txt

echo TODO: ../snippets/ksp.json - remove ',' on end of last json array element
