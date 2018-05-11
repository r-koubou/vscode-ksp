# Changelog

## Version 0.5.0

* Editor

    * **[New]** Outline view is avaiable
    * Code refactoring
    * Small bug fixes


## Version 0.4.6

* Code refactoring

## Version 0.4.5

* Editor

    * Code refactoring
    * Obfuscator

        * Supported: Call command from context menu.
        * Obfuscated code write to clipboard is available.

            Default is disabled. See vscode preference **"ksp.obfuscator.dest.clipboard"**. Value is true, it works. Otherwie write to given filepath.


* Syntax parser

    * Code refactoring
    * Allow maximum array size has been changed 1000000 from 32768 that according to KONTAKT 5.8.0's updated.


## Version 0.4.4

* Syntax parser

    * Small bug fixes

## Version 0.4.3

* Syntax parser

    * [Bug fixes](https://github.com/r-koubou/KSPSyntaxParser/issues/8#issuecomment-380670078)

## Version 0.4.2

* Editor

    * Problems View
        * Improved showing when validate a script.
        * Available multiple files showing.

## Version 0.4.1

* Syntax parser

    * [Improved variavle initializer](https://github.com/r-koubou/KSPSyntaxParser/issues/8)

## Version 0.4.0

Minor update.

This version contains important bug fixes.

### New Feature

- **Obfuscate(BETA)**

    **[How to Run]**

    1. Script file open
    2. Set language mode to 'ksp'
    3. Open command palette and type 'ksp'
    4. Select Obfuscator

    - inline function option

        Default is disabled. If you try, turn on vscode preference **"ksp.obfuscator.inline.function"**

### BUG FIXED / Improved

- Syntax parser
    - Bug
        - [Operator "&" problem](https://github.com/r-koubou/KSPSyntaxParser/issues/5)
        - [Variable initializer expression (using with brackets)](https://github.com/r-koubou/KSPSyntaxParser/issues/6)

    - Improved
        - Check array subscribe

- Editor
    - Small bug fixes
    - Snipet behavior improved

### 0.4.0 BETA Release note

- [BETA4](https://github.com/r-koubou/vscode-ksp/releases/tag/Beta-0.4.0_4)
- [BETA3](https://github.com/r-koubou/vscode-ksp/releases/tag/Beta-0.4.0_3)
- [BETA2](https://github.com/r-koubou/vscode-ksp/releases/tag/Beta-0.4.0_2)
- [BETA1](https://github.com/r-koubou/vscode-ksp/releases/tag/Beta-0.4.0_1)


## Version 0.3.15

* Syntax parser

    * Following bug fixes.

        - [BUG - Value assign to string variable](https://github.com/r-koubou/KSPSyntaxParser/issues/7)
        - [BUG - Callback "on __pgs_changed" is not reconized](https://github.com/r-koubou/KSPSyntaxParser/issues/4)
        - [BUG - Array variable declaration](https://github.com/r-koubou/KSPSyntaxParser/issues/1)
        - [BUG - Parser false detection in message() command](https://github.com/r-koubou/KSPSyntaxParser/issues/2)
        - [BUG - Unrecognized some NI built-in variables by parser](https://github.com/r-koubou/KSPSyntaxParser/issues/3)

* Editor

    * Small bug fixes
    * Undocumented NI variables / commands added to snipet etc.

## Version 0.3.14

* Syntax parser

    * Small bug fixes

## Version 0.3.13

* Editor, Syntax parser

    * Small bug fixes

## Version 0.3.12

* Editor

    * Small bug fixes

## Version 0.3.11

* Syntax parser

    * Bug fixes
    * Improving accuracy

## Version 0.3.10

* Syntax parser

    * Bug fixes [https://github.com/r-koubou/KSPSyntaxParser/issues/2](https://github.com/r-koubou/KSPSyntaxParser/issues/2)

## Version 0.3.9

* Syntax parser

    * Bug fixes [https://github.com/r-koubou/KSPSyntaxParser/issues/1](https://github.com/r-koubou/KSPSyntaxParser/issues/1)

    * ADDED: Script line counter.

        If reached to 5,000 lines per callback / function, KONTAKT KSP parser stack memory will be overflow error.

## Version 0.3.8

* Support 5.7 Built-in Variables

    In 5.7, many NI variable has added.

    See also : [http://twitdoc.com/upload/rz_devel/winmerge-file-compare-report.pdf](http://twitdoc.com/upload/rz_devel/winmerge-file-compare-report.pdf)

    \* Usually, my extraction from KSP Reference program. This time i created a diff from 5.6.8. It may be incomplete.

* Syntax Parser
    * Bug fixes
        * Assignment operator "**:=**" Incorrect detection

## Version 0.3.7

* Support 5.6.8 Built-in Variables
    * $NI_CONTROL_PAR_IDX
    * $HIDE_PART_CURSOR

## Version 0.3.6

* Added rename identifiers feature

## Version 0.3.5

* Go to Definition bug fixes

## Version 0.3.4

* Syntax Parser bug fixes

## Version 0.3.3

Syntax highlighting

* Numeric highlighting bug fixes

## Version 0.3.2

Syntax highlighting

* Numeric highlighting bug fixes

## Version 0.3.1

Syntax Parser

* Some bug fixes

## Version 0.3.0

* `Changed Extension ID`

    `Old extension Id no longer updated and will be removed from VScode marketplace on Aug 1st 2017 (JST)`.

    If you have already installed a previous version( `0.2.7 or older` ), please unintall it.

* `UPDATED` Syntax validation ( `"BETA" VERSION` ).
    * Analysis program has some bugs was FIXED
    * Analysis precision improved (`Semantic Analysis` is `READY`)

* Small bug fixes

## Version 0.2.7

Small bug fixes release.

## Version 0.2.6

### Syntax Parser - Bug fixed

Hexadecimal number cannot work

## Version 0.2.5

* Syntax Parser - Bug fixed

## Version 0.2.4

### Improved - Syntax Parser

A Symbol Table implemented

* variable
* callback
* function
* pre-processor ( RESET_CONDITION() )

## Version 0.2.3

### Improved - Syntax Parser

Support Preprocessor statement (Lexical Analysis only)

## Version 0.2.2

### BUG Fixed Syntax Parser program

* For Windows & Locale ja_JP users

    Error messages: Garbled characters

See also: [https://github.com/r-koubou/vscode-ksp/issues/1]()

## Version 0.2.1

### BUG Fixed Syntax Parser program

* Not work when newline is CRLF
* select statement

## Version 0.2.0

1. Modified some snippet.
2. **NEW!: Syntax validation ( ALPHA VERSION! ). Semantic analysis not yet.**

![](https://github.com/r-koubou/vscode-ksp/raw/master/images/readme/syntaxparser.gif)

### NOTE

* Default is **"disabled"**.

    You can change setting at Preferences -> Settings (Part of "KSP NI KONTAKT Script" ).

* You need to install **Java 1.6 (or higher).**

    **Recomended** 1.8 or higher to work it.

## Version 0.1.0

* NEW Feature

    - Find symbols and find references in script (context menu / Shift+F12)

* Improved

    - Syntax highlighting

## Version 0.0.5

* NEW Feature

    - Go to definition

        Ctrl(Command)+Shift+O or F2 or Ctrl(Command)+Mouse Click
        ![](https://github.com/r-koubou/vscode-ksp/raw/master/images/readme/goto1.png)

        - Variable

            - If variable type is ui_####, you can select following

                1. Callback definition
                2. Variable definition

                ![](https://github.com/r-koubou/vscode-ksp/raw/master/images/readme/goto2.png)

        - User function

* Improved

    - Syntax highlighting

## Version 0.0.4

* Improved

    - Syntax highlighting, hover text, autocomplete

        - Some commands (incl non documented)


## Version 0.0.3

* KONTAKT 5.6.5 ready. Following command, variable added

    - New UI control
        - ui_xy
        - $CONTROL_PAR_CURSOR_PICTURE
        - $CONTROL_PAR_MOUSE_MODE
        - $CONTROL_PAR_ACTIVE_INDEX
        - $CONTROL_PAR_MOUSE_BEHAVIOUR_X
        - $CONTROL_PAR_MOUSE_BEHAVIOUR_Y

	- New UI Commands
        - set_control_par_arr()
        - set_control_par_str_arr()

* Improved

    - KSP commands signature behavior  (hover)
    - Syntax highlighting (KSP commands)

## Version 0.0.2

* Autocomplete supported
* Fixed file extension ( Added ".txt")
* Tweek some files.

## Version 0.0.1

### 1st release. ( Based on KONTAKT 5.6 )

Following features supported

* Syntax hilighting
* Snippets
	* Callbacks
	* Events
	* Commands
	* Built-in Variables
