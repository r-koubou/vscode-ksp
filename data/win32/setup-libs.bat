@echo off

pushd ..

call venv\Scripts\activate.bat
pip install -r win32\requirements.txt

popd
