# Changelog

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

* Small bug fixes release.

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
