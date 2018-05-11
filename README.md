# Language support for NI KONTAKT(TM) Script Processor (KSP)

<small>※日本語の説明は英語の後に書いています</small>

## Features

### NEW

* Outline view

### Other Features

* Obfuscate and Optimize a Script **(BETA)**
* Syntax highlighting
* Autocomplete
* Snippet
* Hover
* Go to Definition
* Find symbols and find references in script
* Syntax validation
* Rename Identifiers

## Outline view

Outline view is available.

![](https://github.com/r-koubou/vscode-ksp/raw/master/images/readme/outline_01.png)

![](https://github.com/r-koubou/vscode-ksp/raw/master/images/readme/outline_02.png)

If you want hide a view, right click -> uncheck from context menu.

![](outlie_off.png)

### If Very high CPU load

Please turn off autorefresh outline function.

You can change setting at Preferences -> **"ksp.obfuscator.inline.function"**

## Syntax validation (BETA)

* Syntax Analysis
* Semantic Analysis
    - Validate array size at declared
    - Validate argument for all KSP command, UI initializer
    - Validate unused variable / user function

    etc.

    `Default is "disabled".`
    You can change setting at Preferences -> Settings (Part of "KONTAKT Script Processor (KSP)" ).

## Obfuscate a Script (-- BETA --)

**OUT OF WARRANTY because it is BETA version.**

Run from command palet

### [How to Run]

1. Open a Script file
2. Set language mode to 'ksp'
3. Open command palette and type 'ksp'
4. Select Obfuscator

![](https://github.com/r-koubou/vscode-ksp/raw/master/images/readme/obfuscate_01.gif)

or Run from context menu in editor.

![](https://github.com/r-koubou/vscode-ksp/raw/master/images/readme/obfuscate_ctx_en.png)

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

## KSP Compatibility

- KONTAKT 5.8.x

## TODO

* Tweak / bug fix if needed

## License

[MIT License](https://github.com/r-koubou/vscode-ksp/blob/master/LICENSE)

## Source Code

[github repository](https://github.com/r-koubou/vscode-ksp)


## Source Code of KSP Syntax Parser Program

[github repository](https://github.com/r-koubou/KSPSyntaxParser)


## Screenshots

* Syntax highlighting

    ![](https://github.com/r-koubou/vscode-ksp/raw/master/images/readme/syntaxhilghting.png)

* Syntax check (*Option)

    ![](https://github.com/r-koubou/vscode-ksp/raw/master/images/readme/syntaxparser.gif)

* Autocomplete

    ![](https://github.com/r-koubou/vscode-ksp/raw/master/images/readme/completion.gif)

* Snippet

    ![](https://github.com/r-koubou/vscode-ksp/raw/master/images/readme/snippet.gif)

* Hover

    ![](https://github.com/r-koubou/vscode-ksp/raw/master/images/readme/hover.png)

* Go to Definition

    ![](https://github.com/r-koubou/vscode-ksp/raw/master/images/readme/goto1.png)

    ![](https://github.com/r-koubou/vscode-ksp/raw/master/images/readme/goto2.png)

## Author

R-Koubou

* Twitter: [@rz_devel](https://twitter.com/rz_devel)
* GitHub:  [https://github.com/r-koubou/](https://github.com/r-koubou/)

## About KONTAKT

KONTAKT is registered trademarks of Native Instruments GmbH.

[https://www.native-instruments.com/](https://www.native-instruments.com/)

----

# Native Instruments KONTAKT Script for VS Code (日本語)

## 機能

### 新機能

* アウトラインビューの追加

### その他機能

* スクリプトのオブファスケートとオプティマイザ **(BETA)**
* シンタックスハイライト
* 補完入力
* スニペット
* ホバー表示
* 宣言箇所へのジャンプ
* スクリプト内のシンボル参照箇所の検索
* 文法解析機能
* リネーム機能

## アウトラインビュー

エクスプローラビューに追加しました。

![](https://github.com/r-koubou/vscode-ksp/raw/master/images/readme/outline_01.png)

![](https://github.com/r-koubou/vscode-ksp/raw/master/images/readme/outline_02.png)

非表示にしたい場合は、右クリック→コンテキストメニューからチェックを外して下さい。

![](outlie_off.png)

### CPU負荷がとても高くなる場合

アウトラインの自動更新機能をオフにしてください。

VSCode の設定から変更できます -> **"ksp.obfuscator.inline.function"**

## 文法解析機能 (BETA)

* 文法解析
* 意味解析（論理チェック）
    - 配列変数宣言時の要素数をチェック
    - コマンドや、UI変数へ渡すパラメータのチェック
    - 未使用の変数、ユーザー定義関数の検出

    など

    `初期設定ではこの機能はオフになっています。`
    使用する際は、設定画面で設定を変えて下さい。
    (Preferences->Settings内、"KONTAKT Script Processor (KSP)" )

## オブファスケート (-- BETA --)

**ベータ版のため、動作保証外**

コマンドパレットから実行できます

### [実行方法]

1. スクリプファイルを開く
2. 言語モードを'ksp'に設定する
3. コマンドパレットを開き、'ksp' とタイプする
4. オブファスケーターを選択する

![](https://github.com/r-koubou/vscode-ksp/raw/master/images/readme/obfuscate_01.gif)

またはエディタ内で右クリックで項目を選択

![](https://github.com/r-koubou/vscode-ksp/raw/master/images/readme/obfuscate_ctx_ja.png)

### 詳細

* 定数、リテラルの展開

~~~
    例：
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

* リネーム
    - ユーザー定義の変数名
    - ユーザー定義の関数名
* シュリンク
    * どこからも使用されていない変数、関数

* ユーザー定義関数のインライン展開

    初期設定ではオフにしています。もし試す場合はVSCodeの設定→ **"ksp.obfuscator.inline.function"** を編集して下さい。


### 文法解析機能について

この機能を使用するには Java 1.6 以上が必要です。

`推奨: 1.8 以上`

* `[注意]パーサーは可能な限り多くのエラー検出を試みますが、記述されているスクリプトの文法がKSP仕様から逸脱していると、正しく機能しません。`

## KSP互換

- KONTAKT 5.8.x のKSPに対応しています

## TODO

* 適宜調整

## ソースコード

[githubリポジトリに置いています](https://github.com/r-koubou/vscode-ksp)

## KSP文法解析プログラムのソースコード

[githubリポジトリに置いています](https://github.com/r-koubou/KSPSyntaxParser)


## 作成者

あーる

* Twitter: [@rz_devel](https://twitter.com/rz_devel)
* GitHub:  [https://github.com/r-koubou/](https://github.com/r-koubou/)

## KONTAKT について

KONTAKT is registered trademarks of Native Instruments GmbH.

[https://www.native-instruments.com/](https://www.native-instruments.com/)
