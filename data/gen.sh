#!/bin/bash

python Excel2CompleteCommands.py KSP.xlsx
python Excel2CompleteVariables.py KSP.xlsx
python Excel2Snippet.py KSP.xlsx

./extract.sh
echo  "# Generate Command Name Array : ../src/features/generated/KSPCommandNames.ts"
python VerifyExtractedManualData.py extract_command.ksp > /dev/null
python Text2TSArray.py  __mergeed__.txt \
                        ../src/features/generated/KSPCommandNames.ts \
                        commandNameList \
                        ui_knob \
                        ui_label \
                        ui_slider \
                        ui_table \
                        ui_value_edit \
                        ui_waveform

echo  "# Generate Builtin Variable Name Array : ../src/features/generated/KSPBuiltinVariableNames.ts"
python VerifyExtractedManualData.py extract_variables.ksp -v > /dev/null
python Text2TSArray.py  __mergeed__.txt \
                        ../src/features/generated/KSPBuiltinVariableNames.ts \
                        builtinVariableNameList

rm ./__mergeed__.txt
rm ./extract_*.ksp

echo TODO: ../snippets/ksp.json - remove ',' on end of last json array element
