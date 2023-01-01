@echo off

if exist .\node_modules rmdir /q /s .\node_modules
if exist .\out          rmdir /q /s .\out
npm cache clean --force
