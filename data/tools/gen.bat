@echo off

setlocal

set LOG_DIR=.logs

set XLSX_COMPLETION=..\KSP_Completion.xlsx
set XLSX_SNIPPET=..\KSP_Snippet.xlsx

set MANUAL_EXTRACTED_DIR=data\extracted
set OUTPUT_TS_DIR=..\..\src\features\generated
set OUTPUT_SNIPPET_DIR=..\..\snippets


mkdir %LOG_DIR%\

pipenv run python spreadsheet\Xlsx2Completion.py %XLSX_COMPLETION% %OUTPUT_TS_DIR%
pipenv run python spreadsheet\Xlsx2Snippet.py    %XLSX_SNIPPET% %OUTPUT_SNIPPET_DIR%

call .\extract.bat
echo "# Generate Command Name Array: ../src/features/generated/KSPCommandNames.ts"
pipenv run python .\spreadsheet\VerifyExtractedCommandData.py %XLSX_COMPLETION% %MANUAL_EXTRACTED_DIR%\extract_command.txt __merged_comand__.txt > %LOG_DIR%\__merged_comand_dump__.txt
pipenv run python Text2TSArray.py ^
    __merged_comand__.txt ^
    %OUTPUT_TS_DIR%\KSPCommandNames.ts CommandNames ^
    ui_knob ui_label ^
    ui_slider ^
    ui_table ^
    ui_value_edit ^
    ui_waveform

echo  # Generate Builtin Variable Name Array : ../src/features/generated/KSPBuiltinVariableNames.ts
pipenv run python .\spreadsheet\VerifyExtractedVariableData.py %XLSX_COMPLETION% %MANUAL_EXTRACTED_DIR%\extract_variables.txt __merged_variables__.txt > %LOG_DIR%\__merged_variables_dump__.txt
pipenv run python Text2TSArray.py ^
    __merged_variables__.txt ^
    %OUTPUT_TS_DIR%\KSPBuiltinVariableNames.ts ^
    BuiltinVariableNames

endlocal
