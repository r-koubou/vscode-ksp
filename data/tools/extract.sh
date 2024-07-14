#!/bin/bash

VERSION="7.10.0"
MANUAL="data/ksp-manuals/KSP Reference Manual(${VERSION}).txt"

OUTPUT_DIR="data/extracted"
STORE_DIR="data/extracted/${VERSION}"

mkdir $OUTPUT_DIR

pipenv run python extractor/manual/ExtractCallbackFromManual.py "$MANUAL" $OUTPUT_DIR/extract_callback.txt  $OUTPUT_DIR/extract_callback_dump.txt
pipenv run python extractor/manual/ExtractCommandFromManual.py "$MANUAL"  $OUTPUT_DIR/extract_command.txt   $OUTPUT_DIR/extract_command_dump.txt
pipenv run python extractor/manual/ExtractVariableFromManual.py "$MANUAL" $OUTPUT_DIR/extract_variables.txt $OUTPUT_DIR/extract_variables_dump.txt

mkdir $STORE_DIR
cp $OUTPUT_DIR/extract_callback.txt  $STORE_DIR
cp $OUTPUT_DIR/extract_command.txt   $STORE_DIR
cp $OUTPUT_DIR/extract_variables.txt $STORE_DIR
