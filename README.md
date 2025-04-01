# Language support for NI KONTAKT(TM) Script Processor (KSP) PREVIEW

## This extension is in "PREVIEW" version

**If you have already installed previous version of [KONTAKT Script Processor (KSP)](https://marketplace.visualstudio.com/items?itemName=rkoubou.ksp), please uninstall it before installing this extension.**


## KSP Compatibility

- KONTAKT 8.x / 7.x / 6.x / 5.x

## Updates from previous extensions

* KSP compiler was redesigned and implemented from scratch using dotnet/C#
    * Improved script analysis
    * Language Server Protocol (LSP) supported

## Features

* Syntax Highlighting
* Syntax Analysis
* Semantic Analysis
* Code Completion
* Document Symbol
* Go to Definition
* Find References in script
* Find Symbols
* Folding
* Hover
* Document Symbol
* Rename Refactoring
* Signature Help
* Obfuscation


## Requirements

* .NET Install Tool
    * Install with this extension automatically if not installed
* .NET Runtime
    * Automatically installed by .NET Install Tool


## Obfuscate a Script

1. Open a Script file
2. Set language mode to `ksp`
3. Open command palette and type `ksp`
4. Select `Obfuscate`


## Restart Language Server

1. Open command palette and type `ksp`
2. Select `Restart Language Server`


## TODO

* Detect unused variables
* Tweak / bug fix if needed

## Source code

[github repository](https://github.com/r-koubou/vscode-ksp)

### License

[MIT License](https://github.com/r-koubou/vscode-ksp/blob/master/LICENSE)

## Source code of KSP Compler

[github repository](https://github.com/r-koubou/KSPCompiler)

## Author

R-Koubou

* Twitter: [@rkoubou_jp](https://twitter.com/rkoubou_jp)
* GitHub:  [https://github.com/r-koubou/](https://github.com/r-koubou/)

## About KONTAKT

KONTAKT is registered trademarks of Native Instruments GmbH.

[https://www.native-instruments.com/](https://www.native-instruments.com/)
