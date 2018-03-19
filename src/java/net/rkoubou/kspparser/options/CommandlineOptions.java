/* =========================================================================

    CommandlineOptions.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.options;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * コマンドライン引数をパースしオプションを構成する
 */
public class CommandlineOptions
{
    /** コマンドラインオプションを格納している Bean */
    static public final KSPParserOptions options = new KSPParserOptions();

    /**
     * ctor.
     */
    private CommandlineOptions(){}

    /**
     * 渡されたコマンドライン引数を元にオプションを構成する
     */
    static public CmdLineParser setup( String[] args ) throws CmdLineException
    {
        CmdLineParser parser = new CmdLineParser( options );
        parser.parseArgument( args );

        if( options.obfuscate )
        {
            options.strict = false;
        }
        if( options.strict )
        {
            options.unused = true;
        }
        return parser;
    }
}