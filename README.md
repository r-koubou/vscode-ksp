# Language support for NI KONTAKT(TM) Script Processor (KSP) PREVIEW

## KSP Compatibility

- KONTAKT 8.x / 7.x / 6.x / 5.x

## What's New in v1.0.0

- Extension and compiler was redesigned and implemented from scratch (See also [CHANGELOG](CHANGELOG.md))

## Features

- Syntax Analysis / Semantic Analysis
    ![](resources/readme/analysis.jpg)

- Completion
    ![](resources/readme/completion.gif)

- Document Symbol
    ![](resources/readme/document_symbol.jpg)

- Go to Definition
- Find References in script
    ![](resources/readme/find_references_01.gif)
    ![](resources/readme/find_references_02.gif)

- Hover
    - Built-in command / variable information
    - ![](resources/readme/hover_01.jpg)
    - Comments for user-defined variables and function definitions (Markdown support)
    - ![](resources/readme/hover_02.jpg)


- Rename
- Signature Help
- Obfuscation

## Obfuscate a Script

1. Open a Script file
2. Set language mode to `ksp`
3. Open command palette and type `ksp`
4. Select `Obfuscate`


## Restart Language Server

1. Open command palette and type `ksp`
2. Select `Restart Language Server`


## Limitations

- Extended syntax is not supported

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
