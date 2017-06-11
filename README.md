# Native Instruments KONTAKT Script for VS Code

## Features

* Syntax highlighting
* Autocomplete
* Snippet
* Hover
* Go to Definition
    - Ctrl(Command)+Shift+O
    - F12
    - Ctrl(Command)+Click
* Find symbols and find references in script ( *Simplified version )
    - Context menu
    - Shift+F12
* **NEW!: Syntax validation ( ALPHA VERSION!! ). Semantic analysis not yet.**

    Default is **"disabled"**.
    You can change setting at Preferences -> Settings (Part of "KSP NI KONTAKT Script" ).
    
### [NOTE]

* **You need to install Java 1.6 (or higher). Recomended 1.8 or higher to work it.**
    
* **Although the parser will attempt as much error detection as possible, if the grammar of the script content deviates from the KSP specification, it will not function properly.**

## KSP Compatibility

- KONTAKT 5.6.5's KSP syntax

## TODO

* Tweak / bug fix if needed
* If i have time to spare, i'll refactor and add some functions.(maybe at a slow pace)
    * Implement semantic analysis for KSP script

## License

[MIT License](https://github.com/r-koubou/vscode-syntax-for-ksp/blob/master/LICENSE)

## Source Code

You can accsess to [github repository](https://github.com/r-koubou/vscode-syntax-for-ksp)

## About KONTAKT

KONTAKT is registered trademarks of Native Instruments GmbH.
[https://www.native-instruments.com/](https://www.native-instruments.com/)

## Screenshots

* Syntax highlighting

    ![](https://github.com/r-koubou/vscode-syntax-for-ksp/raw/master/images/readme/syntaxhilghting.png)

* Syntax check (*Option)

    ![](https://github.com/r-koubou/vscode-syntax-for-ksp/raw/master/images/readme/syntaxparser.gif)

* Autocomplete

    ![](https://github.com/r-koubou/vscode-syntax-for-ksp/raw/master/images/readme/completion.gif)

* Snippet

    ![](https://github.com/r-koubou/vscode-syntax-for-ksp/raw/master/images/readme/snippet.gif)

* Hover

    ![](https://github.com/r-koubou/vscode-syntax-for-ksp/raw/master/images/readme/hover.png)

* Go to Definition

    ![](https://github.com/r-koubou/vscode-syntax-for-ksp/raw/master/images/readme/goto1.png)

    ![](https://github.com/r-koubou/vscode-syntax-for-ksp/raw/master/images/readme/goto2.png)

----

[Japanese]

## 機能

* シンタックスハイライト
* 補完入力
* スニペット
* Hover
* 宣言箇所へのジャンプ
    - Ctrl(Command)+Shift+O
    - F2
    - Ctrl(Command)+クリック
* スクリプト内のシンボル参照箇所の検索（*簡易版）
    - シンボルを右クリック後、コンテキストメニューから選択
    - Shift+F12
* **新機能： 文法解析機能( ※アルファバージョン ) このバージョンでは、意味解析処理は含んでいません**

    初期設定ではこの機能はオフになっています。
    使用する際は、設定画面で設定を変えて下さい。（Preferences->Settings内、KSP NI KONTAKT Script ） 
    
    **[注意] この機能を使用するには Java 1.6 以上が必要です。推奨： Java 1.8 以上**

## KSP互換

- KONTAKT 5.6.5 のKSPに対応しています

## TODO

* 適宜調整
* もし時間に余裕があれば、スローペースになりますが以下を予定しています。
    * 意味解析の実装

## ソースコード

[githubリポジトリに置いています](https://github.com/r-koubou/vscode-syntax-for-ksp)

## KONTAKT について

KONTAKT is registered trademarks of Native Instruments GmbH.
[https://www.native-instruments.com/](https://www.native-instruments.com/)
