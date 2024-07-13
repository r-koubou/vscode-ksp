#!/bin/bash

LOG_DIR=.logs

mkdir $LOG_DIR

pipenv run python spreadsheet/Xlsx2Completion.py ../KSP_Completion.xlsx ../../src/features/generated/
pipenv run python spreadsheet/Xlsx2Snippet.py ../KSP_Snippet.xlsx ../../snippets/

./extract.sh

echo  "# Generate Command Name Array : ../src/features/generated/KSPCommandNames.ts"
pipenv run python ./spreadsheet/VerifyExtractedCommandData.py ../KSP_Completion.xlsx data/extracted/extract_command.txt __merged_comand__.txt > $LOG_DIR/__merged_comand_dump__.txt
pipenv run python Text2TSArray.py \
                        __merged_comand__.txt \
                        ../../src/features/generated/KSPCommandNames.ts \
                        CommandNames \
                        ui_knob \
                        ui_label \
                        ui_slider \
                        ui_table \
                        ui_value_edit \
                        ui_waveform

echo  "# Generate Builtin Variable Name Array : ../src/features/generated/KSPBuiltinVariableNames.ts"
pipenv run python ./spreadsheet/VerifyExtractedVariableData.py ../KSP_Completion.xlsx data/extracted/extract_variables.txt __merged_variables__.txt > $LOG_DIR/__merged_variables_dump__.txt
pipenv run python Text2TSArray.py \
                        __merged_variables__.txt \
                        ../../src/features/generated/KSPBuiltinVariableNames.ts \
                        BuiltinVariableNames
