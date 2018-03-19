/* =========================================================================

    KSPParserOptions.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.options;

import org.kohsuke.args4j.Option;

/**
 * パーサー動作オプションの定義
 */
public class KSPParserOptions
{
    /** 解析対象のスクリプトファイルパス  */
    @Option( name = "--source", usage = "[REQUIRED] ksp script file path" )
    public String sourceFile;

    /** 解析後、ファイルを出力する場合に使用するスクリプトファイルパス  */
    @Option( name = "--output", usage = "output file path when --obfuscate is enabled" )
    public String outputFile;

    /** 意味解析を行わず、構文解析のみ実行するかどうか */
    @Option( name = "--parseonly", usage = "syntax analysis only" )
    public Boolean parseonly = false;

    /** 厳密なチェックを行うかどうか */
    @Option( name = "--strict", usage = "strict mode" )
    public Boolean strict = false;

    /** 未使用変数、ユーザー定義関数を警告扱いにするかどうか */
    @Option( name = "--unused", usage = "scan unused variable and function" )
    public Boolean unused = false;

    /** オブファスケートを行うかどうか */
    @Option( name = "--obfuscate", usage = "obfuscate script" )
    public Boolean obfuscate = false;
}
