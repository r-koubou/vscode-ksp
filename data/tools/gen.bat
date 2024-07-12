@echo off

pipenv run python spreadsheet\Excel2Completion.py ..\KSP_Completion.xlsx ..\..\src\features\generated\
pipenv run python spreadsheet\Excel2Snippet.py ..\KSP_Snippet.xlsx ..\..\snippets\

call .\extract.bat
echo "# Generate Command Name Array: ../src/features/generated/KSPCommandNames.ts"
pipenv run python VerifyExtractedManualData.py extract_command.txt > nul
pipenv run python Text2TSArray.py ^
    __mergeed__.txt ^
    ..\..\src\features\generated\KSPCommandNames.ts CommandNames ^
    ui_knob ui_label ^
    ui_slider ^
    ui_table ^
    ui_value_edit ^
    ui_waveform

echo  # Generate Builtin Variable Name Array : ../src/features/generated/KSPBuiltinVariableNames.ts
pipenv run python VerifyExtractedManualData.py extract_variables.txt -v > nul
pipenv run python Text2TSArray.py ^
    __mergeed__.txt ^
    ..\..\srcfeatures\generated\KSPBuiltinVariableNames.ts ^
    BuiltinVariableNames

del /q .\__mergeed__.txt
del /q .\extract_*.txt
