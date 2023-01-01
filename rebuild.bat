@echo off

call clean.bat

call npm i
tsc -p .
