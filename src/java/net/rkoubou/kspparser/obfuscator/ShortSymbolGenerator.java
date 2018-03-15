/* =========================================================================

    ShortSymbolGenerator.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.obfuscator;

import java.util.Hashtable;

/**
 * 難読化を目的とした、KSPのシンボル名を一定の規則に従い生成する
 */
public class ShortSymbolGenerator
{
    /** シンボルに付与する整数の最大桁数 */
    static private final int MAX_NUMBER_INDEX_DIGIT = 4;

    /** 文字列生成用ワークバッファ */
    static private final StringBuilder buffer = new StringBuilder();
    /** シンボルに付与する整数インデックス */
    static private int number;
    /** シンボルに付与する接頭文字([a-z]) */
    static private char prefix;

    /** シンボル名変更前と変更後の履歴を残すテーブル(元の名前: オブファスケート後) */
    static private final Hashtable<String, String> history = new Hashtable<String, String>( 1024 );

    /**
     * static initializer
     */
    static
    {
        reset();
    }

    /**
     * Ctor.
     */
    private ShortSymbolGenerator(){}

    /**
     * 内部カウンタを初期状態にリセットする
     */
    static public void reset()
    {
        number = 0;
        prefix = 'a';
        buffer.delete( 0, buffer.length() );
        history.clear();
    }

    /**
     * [a-z]{1}[0-9]{4}形式の連番割当て文字列を生成する
     * よって、最大で 260000 個までの生成を上限とする
     */
    static public String generate( String orgName )
    {
        buffer.delete( 0, buffer.length() );
        buffer.append( prefix ).append( String.format( "%0" + MAX_NUMBER_INDEX_DIGIT + "d", number ) );
        number++;
        if( Integer.toString( number ).length() > MAX_NUMBER_INDEX_DIGIT )
        {
            number = 0;
            if( prefix == 'z' )
            {
                throw new RuntimeException( "Too many generate strings... (>=" + ( 26 * ( 10 * MAX_NUMBER_INDEX_DIGIT ) ) );
            }
            else
            {
                prefix++;
            }
        }
        String ret = buffer.toString();
        history.put( orgName, ret );
        return ret;
    }

    /**
     * オブファスケート前のシンボル名からオブファスケート後のシンボル名を逆引きする
     */
    static public String getSymbolFromOrgName( String orgName )
    {
        String ret = history.get( orgName );
        if( ret == null )
        {
            ret = generate( orgName );
        }
        return ret;
    }

    /**
     * オブファスケート後のシンボル名からオブファスケート前のシンボル名を逆引きする
     */
    static public String getOrgNameFromSymbol( String symbol )
    {
        for( String k : history.keySet() )
        {
            if( symbol.equals( history.get( k ) ) )
            {
                return k;
            }
        }
        return null;
    }
}
