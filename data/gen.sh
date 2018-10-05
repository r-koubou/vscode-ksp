#!/bin/bash

python3 Excel2CompleteCommands.py KSP.xlsx
python3 Excel2CompleteVariables.py KSP.xlsx
python3 Excel2Snippet.py KSP.xlsx

echo  "# Generate Command Name Array : ../src/features/generated/KSPCommandNames.ts"
python3 VerifyExtractedManualData.py extract_command.txt > /dev/null
python3 Text2TSArray.py  __mergeed__.txt \
                        ../src/features/generated/KSPCommandNames.ts \
                        commandNameList \
                        ui_knob \
                        ui_label \
                        ui_slider \
                        ui_table \
                        ui_value_edit \
                        ui_waveform

echo  "# Generate Builtin Variable Name Array : ../src/features/generated/KSPBuiltinVariableNames.ts"
python3 VerifyExtractedManualData.py extract_variables.txt -v > /dev/null
python3 Text2TSArray.py  __mergeed__.txt \
                        ../src/features/generated/KSPBuiltinVariableNames.ts \
                        builtinVariableNameList

rm ./__mergeed__.txt

echo TODO: ../snippets/ksp.json - remove ',' on end of last json array element
