@echo off

pipenv run python Excel2Completion.py
pipenv run python Excel2Snippet.py

call .\extract.bat
echo "# Generate Command Name Array: ../src/features/generated/KSPCommandNames.ts"
pipenv run python VerifyExtractedManualData.py extract_command.txt > nul
pipenv run python Text2TSArray.py  __mergeed__.txt ..\src\features\generated\KSPCommandNames.ts CommandNames ui_knob ui_label                          ui_slider                          ui_table                          ui_value_edit                          ui_waveform

echo  # Generate Builtin Variable Name Array : ../src/features/generated/KSPBuiltinVariableNames.ts
pipenv run python VerifyExtractedManualData.py extract_variables.txt -v > nul
pipenv run python Text2TSArray.py  __mergeed__.txt ../src/features/generated/KSPBuiltinVariableNames.ts BuiltinVariableNames

del /q .\__mergeed__.txt
del /q .\extract_*.txt

echo TODO: ../snippets/ksp.json - remove ',' on end of last json array element
