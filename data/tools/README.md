tools
=======

## Requirements

- Powesehll 7 (or later)
  - [Windows](https://learn.microsoft.com/powershell/scripting/install/installing-powershell-on-windows)
  - [macOS](https://learn.microsoft.com/powershell/scripting/install/installing-powershell-on-macos)
- Python 3.8 (or later)
  - virtualenv
  - pipenv

## Setup

### 1. Install virtualenv and pipenv

#### virtualenv

```bash
pip install virtualenv
```
#### pipenv

Windows

```bash
pip install pipenv
```

(macOS+Homebrew)
```bash
brew install pipenv
```

## 2. Setup for this project

Install Python modules to the virtual environment.

```bash
pwsh ./setup.ps1
```


## 3. Run the script

```bash
pwsh ./gen.ps1
```

- Collect variables, commands from spreadsheet files / KSP Reference Manual
- Generate TypeScript files for vsce code completion
- Generate json files for vsce code completion

### See also

- extract.ps1
  - Extract variables, commands from KSP Reference Manual
