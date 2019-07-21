<small>[日本語はコチラ](https://github.com/r-koubou/vscode-ksp/blob/master/README.ja.md)</small>

![](https://vsmarketplacebadge.apphb.com/version/rkoubou.ksp.svg
) ![](https://vsmarketplacebadge.apphb.com/installs-short/rkoubou.ksp.svg
)

# Language support for NI KONTAKT(TM) Script Processor (KSP)

## KSP Compatibility

- KONTAKT 6.1.0 / 5.x

## Features

* Syntax check and script copy to clipboard
* Outline view
* Obfuscate and Optimize a Script **(BETA)**
* Syntax highlighting
* Autocomplete
* Snippet
* Hover
* Go to Definition
* Find symbols and find references in script
* Syntax validation
* Rename Identifiers

## Syntax check and script copy to clipboard

If `F7` key pressed and your script has no problem, your script will copy to clip board.

or run from command palette.

![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/parse_cmd_en.png)

- **You need to ready for "Syntax validation" function.**
- If `Shift+F7` pressed, run obfuscator.

## Outline view

Outline view is available in Explorer.

![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/outline_vscode.png)

## Syntax validation (BETA)

* Syntax Analysis
* Semantic Analysis
    - Validate array size at declared
    - Validate argument for all KSP command, UI initializer
    - Validate unused variable / user function

    etc.

    `Default is "disabled".`
    You can change setting at Preferences -> Settings (Part of "KONTAKT Script Processor (KSP)" ).

## Obfuscate a Script (BETA)

**OUT OF WARRANTY because it is BETA version.**

Run from command palet

### [How to Run]

1. Open a Script file
2. Set language mode to 'ksp'
3. Open command palette and type 'ksp'
4. Select Obfuscator

![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/obfuscate_01.gif)

or Run from context menu in editor.

![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/obfuscate_ctx_en.png)

### Detail

* Expand constant variable / literal

~~~
    e.g.
    [Before]
    declare const $MAX := 100
    declare $i
    declare @s
    $i := $MAX * 10
    @a := "MAX is" & $MAX & ". $MAX is always 100"

    [After]
    declare $_geug
    declare @_sxhd
    $_geug := 1000
    @_sxhd := "MAX is 100. $MAX is always 100"
~~~

* Rename
    - variable name
    - user function
* Shrink
    * user variable if unused anywhere
    * user function if unused anywhere

* inline user function

    Default is disabled. If you try, turn on vscode preference **"ksp.obfuscator.inline.function"**

### About Syntax validation

* You need to install Java 1.6 (or higher).

    `Recomended: 1.8 or higher to work it`

* `[NOTE] Although the parser will attempt as much error detection as possible, if the grammar of the your script content deviates from the KSP specification, it will not function properly.`

## Screenshots

* Syntax highlighting

    ![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/syntaxhilghting.png)

* Syntax check (*Option)

    ![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/syntaxparser.gif)

* Autocomplete

    ![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/completion.gif)

* Snippet

    ![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/snippet.gif)

* Hover

    ![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/hover.png)

* Go to Definition

    ![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/goto1.png)

    ![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/goto2.png)

## TODO

* Tweak / bug fix if needed

## Source Code

[github repository](https://github.com/r-koubou/vscode-ksp)

### License

[MIT License](https://github.com/r-koubou/vscode-ksp/blob/master/LICENSE)

## Source Code of KSP Syntax Parser Program

[github repository](https://github.com/r-koubou/KSPSyntaxParser)

## Author

R-Koubou

* Twitter: [@rkoubou_jp](https://twitter.com/rkoubou_jp)
* GitHub:  [https://github.com/r-koubou/](https://github.com/r-koubou/)

## About KONTAKT

KONTAKT is registered trademarks of Native Instruments GmbH.

[https://www.native-instruments.com/](https://www.native-instruments.com/)
