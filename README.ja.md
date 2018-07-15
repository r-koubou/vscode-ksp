![](https://vsmarketplacebadge.apphb.com/version/rkoubou.ksp.svg
) ![](https://vsmarketplacebadge.apphb.com/installs-short/rkoubou.ksp.svg
) <small>[English](https://github.com/r-koubou/vscode-ksp/blob/master/README.md)</small>

# Language support for NI KONTAKT(TM) Script Processor (KSP)

## 機能

* アウトラインビュー**(New)**
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

VSCode標準のアウトラインビューに対応し、使用可能になりました。

> v0.5.3 から、独自に実装していたカスタムビューからVSCodeの標準アウトラインビューへ移行しました。

![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/outline_vscode.png)

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

## オブファスケート (BETA)

**ベータ版のため、動作保証外**

コマンドパレットから実行できます

### [実行方法]

1. スクリプファイルを開く
2. 言語モードを'ksp'に設定する
3. コマンドパレットを開き、'ksp' とタイプする
4. オブファスケーターを選択する

![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/obfuscate_01.gif)

またはエディタ内で右クリックで項目を選択

![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/obfuscate_ctx_ja.png)

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


## スクリーンショット

* シンタックスハイライト

    ![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/syntaxhilghting.png)

* 文法チェック(*オプション)

    ![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/syntaxparser.gif)

* 自動補完

    ![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/completion.gif)

* スニペット

    ![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/snippet.gif)

* ホバー

    ![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/hover.png)

* Go to Definition

    ![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/goto1.png)

    ![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/goto2.png)

## TODO

* 適宜調整

## ソースコード

[githubリポジトリ](https://github.com/r-koubou/vscode-ksp)

### ライセンス

[MIT License](https://github.com/r-koubou/vscode-ksp/blob/master/LICENSE)

## KSP文法解析プログラムのソースコード

[githubリポジトリ](https://github.com/r-koubou/KSPSyntaxParser)

## 作成者

あーる

* Twitter: [@rz_devel](https://twitter.com/rz_devel)
* GitHub:  [https://github.com/r-koubou/](https://github.com/r-koubou/)

## KONTAKT について

KONTAKT is registered trademarks of Native Instruments GmbH.

[https://www.native-instruments.com/](https://www.native-instruments.com/)
