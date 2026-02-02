# Language support for NI KONTAKT Script Processor (KSP)

![Screenshot](https://raw.githubusercontent.com/r-koubou/vscode-ksp/refs/heads/main/resources/readme/screenshot.png)

## KSP Compatibility

- KONTAKT 8.x / 7.x / 6.x / 5.x

## Features

This extension now bundles the KSP language server and provides the following features:

- Syntax and semantic analysis (diagnostics)
- Code completion
- Outline (document symbols)
- Go to Definition
- Find All References
- Hover
  - Built-in command and variable information
  - Documentation comments written immediately before variable declarations and function definitions are also shown. Markdown is supported.
    ```
    {
        ## This is a my variable.
        - value is used for ...
        - value range is 0 to 100
    }
    declare $myVariable
    ```
- Rename Symbol
- Signature Help (parameter hints)
- Obfuscation

## Obfuscate a Script

1. Open a script file.
2. Set the language mode to `ksp`.
3. Open the Command Palette and type `ksp`.
4. Select `Obfuscate`.

## Restart the Language Server

1. Open the Command Palette and type `ksp`.
2. Select `Restart Language Server`.

## Limitations

- Extended syntax is not supported.

## Source Code

- Extension: [GitHub repository](https://github.com/r-koubou/vscode-ksp)
    - [Issues](https://github.com/r-koubou/vscode-ksp/issues)
- Compiler, Language Server: [GitHub repository](https://github.com/r-koubou/KSPCompiler)
    - [Issues](https://github.com/r-koubou/KSPCompiler/issues)

## License

[MIT License](https://github.com/r-koubou/vscode-ksp/blob/master/LICENSE)

## Author

R-Koubou

- Twitter: [@rkoubou_jp](https://twitter.com/rkoubou_jp)
- GitHub: [https://github.com/r-koubou/](https://github.com/r-koubou/)

## About KONTAKT

KONTAKT is a registered trademark of Native Instruments GmbH.

[https://www.native-instruments.com/](https://www.native-instruments.com/)
