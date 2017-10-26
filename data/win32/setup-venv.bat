@echo off

pushd ..

python -m venv venv
call venv\Scripts\activate.bat
python -m pip install --upgrade pip

popd
