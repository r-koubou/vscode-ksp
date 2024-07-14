$VERSION = "7.10.0"
$MANUAL = "data/ksp-manuals/KSP Reference Manual($VERSION).txt"

$OUTPUT_DIR = "data/extracted"
$STORE_DIR  = "data/extracted/$VERSION"
$DUMP_DIR   = ".logs"

# Create output directory if it does not exist
if (-not (Test-Path -Path $OUTPUT_DIR)) {
    New-Item -ItemType Directory -Path $OUTPUT_DIR
}
if (-not (Test-Path -Path $DUMP_DIR)) {
    New-Item -ItemType Directory -Path $DUMP_DIR
}

# Run Python scripts using pipenv
pipenv run python extractor/manual/ExtractCallbackFromManual.py "$MANUAL" "$OUTPUT_DIR/extract_callback.txt"  "$DUMP_DIR/extract_callback_dump.txt"
pipenv run python extractor/manual/ExtractCommandFromManual.py  "$MANUAL"  "$OUTPUT_DIR/extract_command.txt"  "$DUMP_DIR/extract_command_dump.txt"
pipenv run python extractor/manual/ExtractVariableFromManual.py "$MANUAL" "$OUTPUT_DIR/extract_variables.txt" "$DUMP_DIR/extract_variables_dump.txt"

# Create store directory if it does not exist
if (-not (Test-Path -Path $STORE_DIR)) {
    New-Item -ItemType Directory -Path $STORE_DIR
}

# Copy extracted files to store directory
Copy-Item "$OUTPUT_DIR/extract_callback.txt"  -Destination $STORE_DIR
Copy-Item "$OUTPUT_DIR/extract_command.txt"   -Destination $STORE_DIR
Copy-Item "$OUTPUT_DIR/extract_variables.txt" -Destination $STORE_DIR
