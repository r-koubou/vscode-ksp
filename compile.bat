@echo off

if exist .\out rmdir /q /s .\out

tsc -p .
