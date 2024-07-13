@echo off

pipenv run python spreadsheet\Xlsx2Completion.py ..\KSP_Completion.xlsx ..\..\src\features\generated\
pipenv run python spreadsheet\Xlsx2Snippet.py ..\KSP_Snippet.xlsx ..\..\snippets\

call .\extract.bat
echo "# Generate Command Name Array: ../src/features/generated/KSPCommandNames.ts"
pipenv run python .\spreadsheet\VerifyExtractedCommandData.py ..\KSP_Completion.xlsx data\extracted\extract_command.txt __merged__.txt > nul
pipenv run python Text2TSArray.py ^
    __mergeed__.txt ^
    ..\..\src\features\generated\KSPCommandNames.ts CommandNames ^
    ui_knob ui_label ^
    ui_slider ^
    ui_table ^
    ui_value_edit ^
    ui_waveform

echo  # Generate Builtin Variable Name Array : ../src/features/generated/KSPBuiltinVariableNames.ts
pipenv run python .\spreadsheet\VerifyExtractedVariableData.py ..\KSP_Completion.xlsx data\extracted\extract_variables.txt __merged__.txt > nul
pipenv run python Text2TSArray.py ^
    __merged__.txt ^
    ..\..\srcfeatures\generated\KSPBuiltinVariableNames.ts ^
    BuiltinVariableNames

del /q .\__mergeed__.txt
