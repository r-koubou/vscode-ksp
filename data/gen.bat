@echo off

call pythonenv.bat

%PYTHON3% Excel2CompleteCommands.py KSP.xlsx
%PYTHON3% Excel2CompleteVariables.py KSP.xlsx
%PYTHON3% Excel2Snippet.py KSP.xlsx

call .\extract.bat
echo "# Generate Command Name Array: ../src/features/generated/KSPCommandNames.ts"
%PYTHON3% VerifyExtractedManualData.py extract_command.ksp > nul
%PYTHON3% Text2TSArray.py  __mergeed__.txt ..\src\features\generated\KSPCommandNames.ts commandNameList ui_knob ui_label                          ui_slider                          ui_table                          ui_value_edit                          ui_waveform

echo  # Generate Builtin Variable Name Array : ../src/features/generated/KSPBuiltinVariableNames.ts
%PYTHON3% VerifyExtractedManualData.py extract_variables.ksp -v > nul
%PYTHON3% Text2TSArray.py  __mergeed__.txt ../src/features/generated/KSPBuiltinVariableNames.ts builtinVariableNameList

del /q .\__mergeed__.txt
del /q .\extract_*.ksp

echo TODO: ../snippets/ksp.json - remove ',' on end of last json array element
