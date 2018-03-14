# Native Instruments KONTAKT Script for VS Code

<small>※日本語の説明は英語の後に書いています</small>

## Features

* Syntax highlighting
* Autocomplete
* Snippet
* Hover
* Go to Definition
* Find symbols and find references in script
* Syntax validation
* Rename Identifiers

## Syntax validation

* Syntax Analysis
* Semantic Analysis
    - Validate array size at declared
    - Validate argument for all KSP command, UI initializer
    - Validate unused variable / user function

    etc.

    `Default is "disabled".`
    You can change setting at Preferences -> Settings (Part of "KONTAKT Script Processor (KSP)" ).

### About Syntax validation

* You need to install Java 1.6 (or higher).

    `Recomended: 1.8 or higher to work it`

* `[NOTE] Although the parser will attempt as much error detection as possible, if the grammar of the your script content deviates from the KSP specification, it will not function properly.`

## KSP Compatibility

- KONTAKT 5.7.x

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

* シンタックスハイライト
* 補完入力
* スニペット
* ホバー表示
* 宣言箇所へのジャンプ
* スクリプト内のシンボル参照箇所の検索
* 文法解析機能
* リネーム機能
    - シンボルを右クリック後、コンテキストメニューから選択
    - F2

## 文法解析機能

* 文法解析
* 意味解析（論理チェック）
    - 配列変数宣言時の要素数をチェック
    - コマンドや、UI変数へ渡すパラメータのチェック
    - 未使用の変数、ユーザー定義関数の検出

    など

    `初期設定ではこの機能はオフになっています。`
    使用する際は、設定画面で設定を変えて下さい。
    (Preferences->Settings内、"KONTAKT Script Processor (KSP)" )

### 文法解析機能について

この機能を使用するには Java 1.6 以上が必要です。

`推奨: 1.8 以上`

* `[注意]パーサーは可能な限り多くのエラー検出を試みますが、記述されているスクリプトの文法がKSP仕様から逸脱していると、正しく機能しません。`

## KSP互換

- KONTAKT 5.7.x のKSPに対応しています

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
