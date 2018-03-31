/* =========================================================================

    KSPParserOptions.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.options;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

/**
 * パーサー動作オプションの定義
 */
public class KSPParserOptions
{
    /** 解析対象のスクリプトファイルパス  */
    @Option( name = "-h",aliases = "--help", usage = "print commandline options" )
    public boolean usage;

    /** 解析対象のスクリプトファイルパス  */
    @Argument( index = 0, metaVar = "source", usage = "ksp script file path" )
    public String sourceFile;

    /** 解析後、ファイルを出力する場合に使用するスクリプトファイルパス  */
    @Option( name = "-o", aliases = "--output", usage = "output file path when --obfuscate is enabled" )
    public String outputFile;

    /** 意味解析を行わず、構文解析のみ実行するかどうか */
    @Option( name = "-p", aliases = "--parseonly", usage = "syntax analysis only" )
    public Boolean parseonly = false;

    /** 厳密なチェックを行うかどうか */
    @Option( name = "-s", aliases = "--strict", usage = "strict mode" )
    public Boolean strict = false;

    /** 未使用変数、ユーザー定義関数を警告扱いにするかどうか */
    @Option( name = "-u", aliases = "--unused", usage = "scan unused variable and function" )
    public Boolean unused = false;

    /** オブファスケートを行うかどうか */
    @Option( name = "-O", aliases = "--obfuscate", usage = "obfuscate script" )
    public Boolean obfuscate = false;

    /** 全てのユーザー定義関数をインライン展開するかどうか */
    @Option( name = "-OI", aliases = "--inline-userfunction", usage = "inline user definition user functions will be expanded (require --obfuscate)" )
    public Boolean inlineUserFunction = false;

}
