$LOG_DIR = ".logs"
$XLSX_COMPLETION = "../KSP_Completion.xlsx"
$XLSX_SNIPPET = "../KSP_Snippet.xlsx"
$EXTRACTED_MANUAL_DIR = "data/extracted/ksp-manuals"
$EXTRACTED_DIFF_DIR = "data/extracted/diff"
$OUTPUT_TS_DIR = "../../src/features/generated"
$OUTPUT_SNIPPET_DIR = "../../snippets"

function OutputHeaderText {
    param ( $text )
    Write-Output "--------------------------------------------------------------"
    Write-Output "# $text"
    Write-Output "--------------------------------------------------------------"
}

function MakeDirectory {
    param ( $directory )
    if (-not (Test-Path -Path $directory)) {
        New-Item -ItemType Directory -Path $directory
    }
}

#-------------------------------------------------------------------------------
# Create log directory if it does not exist
#-------------------------------------------------------------------------------
MakeDirectory $LOG_DIR

#-------------------------------------------------------------------------------
# Generate files from XLSX
#-------------------------------------------------------------------------------
OutputHeaderText "Genarate Completion file (*.ts) from XLSX : $XLSX_COMPLETION"
pipenv run python spreadsheet/Xlsx2Completion.py $XLSX_COMPLETION $OUTPUT_TS_DIR

OutputHeaderText "Genarate Snippet file (*.json) from XLSX : $XLSX_SNIPPET"
pipenv run python spreadsheet/Xlsx2Snippet.py $XLSX_SNIPPET $OUTPUT_SNIPPET_DIR

#-------------------------------------------------------------------------------
# Execute extract script
#-------------------------------------------------------------------------------
OutputHeaderText "Execute extract script"
& "./extract.ps1"

#-------------------------------------------------------------------------------
# Merge extracted data and XLSX data
#-------------------------------------------------------------------------------
OutputHeaderText "Merge extracted data and XLSX data"
Write-Output "Generate Command Name Array : $($OUTPUT_TS_DIR)/KSPCommandNames.ts"
pipenv run python ./spreadsheet/VerifyExtractedCommandData.py $XLSX_COMPLETION "$EXTRACTED_MANUAL_DIR/extract_command.txt" "__merged_comand__.txt" > "$LOG_DIR/__merged_comand_dump__.txt"
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

Write-Output "Generate Builtin Variable Name Array : $($OUTPUT_TS_DIR)/KSPBuiltinVariableNames.ts"
pipenv run python ./spreadsheet/VerifyExtractedVariableData.py $XLSX_COMPLETION "$EXTRACTED_MANUAL_DIR/extract_variables.txt" "__merged_variables__.txt" > "$LOG_DIR/__merged_variables_dump__.txt"
pipenv run python Text2TSArray.py `
                        "__merged_variables__.txt" `
                        "$OUTPUT_TS_DIR/KSPBuiltinVariableNames.ts" `
                        "BuiltinVariableNames"

#-------------------------------------------------------------------------------
# Output diff with previous generated files
#-------------------------------------------------------------------------------
OutputHeaderText "Output diff with previous generated files"
function ExtractDiff {
    param ( $item )
    $fileName = $item.Name
    $filePath = $item.FullName
    $patchFilePath = "$LOG_DIR/diff_$fileName.patch"

    Write-Output $fileName
    git diff $filePath > $patchFilePath
    pipenv run python ./extractor/diff/ExtractDiffGeneratedTS.py `
        $patchFilePath `
        "$EXTRACTED_DIFF_DIR/extract_diff_$fileName.txt"
}

MakeDirectory $EXTRACTED_DIFF_DIR

Write-Output $OUTPUT_TS_DIR

$generatedTsFiles = Get-ChildItem $OUTPUT_TS_DIR -Filter *.ts
foreach($item in $generatedTsFiles) {
    ExtractDiff $item
}

Write-Output $OUTPUT_SNIPPET_DIR

$generatedSnippetFiles = Get-ChildItem $OUTPUT_SNIPPET_DIR -Filter *.json
foreach($item in $generatedSnippetFiles) {
    ExtractDiff $item
}

Write-Output "Done."
