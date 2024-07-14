$LOG_DIR = ".logs"
$XLSX_COMPLETION = "../KSP_Completion.xlsx"
$XLSX_SNIPPET = "../KSP_Snippet.xlsx"
$MANUAL_EXTRACTED_DIR = "data/extracted"
$OUTPUT_TS_DIR = "../../src/features/generated"
$OUTPUT_SNIPPET_DIR = "../../snippets"

# Create log directory if it does not exist
if (-not (Test-Path -Path $LOG_DIR)) {
    New-Item -ItemType Directory -Path $LOG_DIR
}

# Run Python scripts using pipenv
pipenv run python spreadsheet/Xlsx2Completion.py $XLSX_COMPLETION $OUTPUT_TS_DIR
pipenv run python spreadsheet/Xlsx2Snippet.py $XLSX_SNIPPET $OUTPUT_SNIPPET_DIR

# Execute extract script
& "./extract.ps1"

Write-Output "# Generate Command Name Array : $($OUTPUT_TS_DIR)/KSPCommandNames.ts"
pipenv run python ./spreadsheet/VerifyExtractedCommandData.py $XLSX_COMPLETION "$MANUAL_EXTRACTED_DIR/extract_command.txt" "__merged_comand__.txt" > "$LOG_DIR/__merged_comand_dump__.txt"
pipenv run python Text2TSArray.py `
                        "__merged_comand__.txt" `
                        "$OUTPUT_TS_DIR/KSPCommandNames.ts" `
                        "CommandNames" `
                        "ui_knob" `
                        "ui_label" `
                        "ui_slider" `
                        "ui_table" `
                        "ui_value_edit" `
                        "ui_waveform"

Write-Output "# Generate Builtin Variable Name Array : $($OUTPUT_TS_DIR)/KSPBuiltinVariableNames.ts"
pipenv run python ./spreadsheet/VerifyExtractedVariableData.py $XLSX_COMPLETION "$MANUAL_EXTRACTED_DIR/extract_variables.txt" "__merged_variables__.txt" > "$LOG_DIR/__merged_variables_dump__.txt"
pipenv run python Text2TSArray.py `
                        "__merged_variables__.txt" `
                        "$OUTPUT_TS_DIR/KSPBuiltinVariableNames.ts" `
                        "BuiltinVariableNames"