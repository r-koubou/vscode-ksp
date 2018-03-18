/* =========================================================================

    AnalyzerOption.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

/**
 * VM起動時のVM引数からパーサの動作を動的に指定、管理するクラス
 */
public class AnalyzerOption
{
    /** VM引数 -D 未使用変数、意味解析を行わず、構文解析のみ実行するかどうか */
    static public final String SYSTEM_PROPERTY_PARSE_ONLY = "kspparser.parseonly";
    /** 意味解析を行わず、構文解析のみ実行するかどうか */
    static final public boolean parseonly;

    /** VM引数 -D より厳密なチェックを行うかどうか */
    static public final String SYSTEM_PROPERTY_STRICT = "kspparser.strict";
    /** 厳密なチェックを行うかどうか */
    static final public boolean strict;

    /** VM引数 -D 未使用変数、ユーザー定義関数を警告扱いにするかどうか */
    static public final String SYSTEM_PROPERTY_WARNING_UNUSED = "kspparser.unused";
    /** 未使用変数、ユーザー定義関数を警告扱いにするかどうか */
    static final public boolean unused;

    /** VM引数 -D オブファスケートを行うかどうか */
    static public final String SYSTEM_PROPERTY_OBFUSCATE = "kspparser.obfuscate";
    /** オブファスケートを行うかどうか */
    static final public boolean obfuscate;

    /**
     * static initializer
     */
    static
    {
        parseonly = getBoolean( SYSTEM_PROPERTY_PARSE_ONLY, "false" );
        strict    = getBoolean( SYSTEM_PROPERTY_STRICT, "false" );
        unused    = strict || getBoolean( SYSTEM_PROPERTY_WARNING_UNUSED, "false" );
        obfuscate = getBoolean( SYSTEM_PROPERTY_OBFUSCATE, "false" );
    }

    /**
     * 指定システムプロパティから boolean値を取得する
     */
    static private boolean getBoolean( String key, String defaultValue )
    {
        String v = System.getProperty( key, defaultValue ).toLowerCase();
        return v.equals( "true" );
    }

    /**
     * ctor
     */
    private AnalyzerOption(){}

}