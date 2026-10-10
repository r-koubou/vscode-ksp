# KONTAKT Script Processor for VS Code Extension

## KONTAKT Script Processor (KSP) とは

Native Instrument の KONTAKT 用スクリプト言語。

https://docs.native-instruments.com/ni-tech-manuals/ksp-manual/en/welcome-to-ksp


## この VS Code 拡張機能について

言語サーバーを利用して KSP の構文解析や補完、エラーチェックを行う。
文法チェックやLSP による補完などの機能は、言語サーバーによって提供される。

よって、この拡張機能の実装はごくシンプルで、言語サーバーのクライアントとしての役割に徹している。

また、以下についてはLSPを用いずにこのプロジェクト内で定義ファイルを設置している。

- シンタックスハイライトの定義
- スニペットの定義

## 言語サーバー

この拡張機能の開発者が作成した言語サーバーであり、以下の GitHub リポジトリで公開されている。

https://github.com/r-koubou/KSPCompiler

## SublimeKSP で採用されている独自の拡張構文との互換性

https://github.com/nojanath/SublimeKSP

この拡張機能は現時点で SublimeKSP で採用されている[独自の拡張構文](https://github.com/nojanath/SublimeKSP/wiki#extended-script-syntax)との互換は無い。

将来的に対応する場合、言語サーバー側で独自の拡張構文をサポートする予定である。
その場合、この拡張機能での対応事項としてはシンタックスハイライトの定義やスニペットの更新などが考えられる。

## ディレクトリ構成

```
├── .github                         # GitHub 関連の設定ファイルや GitHub Actions ワークフローファイル
├── resources                       # 同梱するソースコード以外のリソース
│   ├── readme                      # README.md で参照するリソースファイル
│   └── icon.png                    # メインアイコンファイル
├── snippets                        # スニペット定義ファイル
│   ├── declaration.json            # 宣言に関するスニペット定義
│   ├── preprocessor.json           # KSP プリプロセッサに関するスニペット定義
│   └── statement.json              # ステートメントに関するスニペット定義
├── src                             # 拡張機能のソースコード
│   ├── command.ts                  # VS Code コマンド関連の処理
│   ├── configurations.ts           # 拡張機能の設定関連の処理
│   ├── constants.ts                # 定数定義
│   ├── dotnet-install.ts           # .NET SDK のインストール処理
│   ├── extension.ts                # 拡張機能のエントリーポイント
│   ├── lsp-client.ts               # 言語サーバークライアント関連の処理
│   └── obfuscator.ts               # コード難読化の処理
├── syntaxes
│   └── ksp.json                    # KSP 用のシンタックスハイライト定義
├── .prettierrc                     # Prettier の設定ファイル
├── .prototools                     # Moonrepo proto の設定ファイル
├── AGENTS.md                       # このファイル
├── CHANGELOG.md                    # 変更履歴
├── CLAUDE.md                       # # Claude Code用のプロジェクト規約・開発コマンド指示書
├── esbuild.js                      # esbuild の設定ファイル
├── eslint.config.mjs               # ESLint の設定ファイル
├── language-configuration.json     # 言語固有の設定ファイル
├── LICENSE                         # ライセンスファイル
├── NOTICE.md                       # 依存しているサードパーティ製ソフトウェアの帰属表記
├── package.json                    # パッケージ設定ファイル
├── pnpm-lock.yaml                  # pnpm のロックファイル
├── pnpm-workspace.yaml             # pnpm ワークスペース設定ファイル
├── README.md                       # プロジェクトの README ファイル
├── renovate.json                   # Renovate の設定ファイル
└── tsconfig.json                   # TypeScript の設定ファイル
```

## 依存ライブラリの方針

vscode や vsce など必須なものやTypeScript 言語に関連する定番ライブラリの最小限にとどめ、その他の依存ライブラリは現状極力追加しない方針であるが、将来新規の機能実装の要件に応じて人間と対話の上、導入の決定を行う。

## ローカル開発環境の構築

### 事前条件

Moonrepo proto がインストールされていること。
https://moonrepo.dev/proto

### 手順

#### 1. ソフトウェアのインストール

```bash
proto install
```

#### 2. 言語サーバーのダウンロードと配備

https://github.com/r-koubou/KSPCompiler/releases/latest から最新のリリースをダウンロードし、 このプロジェクトのルートディレクトリに `language_server` ディレクトリを作成して配置する。

ダウンロード対象ファイルは `KSP-LanguageServer-`から始まるファイルである。
ファイル名に含まれるビルドターゲットの違い（Release/Debug）は言語サーバーのビルド構成を示している。

拡張機能本体の動作確認においては通常 Release ビルドで問題ない。

## CI

このプロジェクトでは GitHub Actions を利用して CI を実行し、拡張機能のビルドを行う。
ファイルは `.github/workflows/build_vscode_extension_####.yml` である。

`build_vscode_extension.yml` がビルド処理の本体、その他のファイル（`build_vscode_extension_####.yml`）はトリガー毎に分かれている。
