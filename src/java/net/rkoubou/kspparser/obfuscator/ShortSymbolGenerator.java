/* =========================================================================

    ShortSymbolGenerator.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.obfuscator;

import java.util.Hashtable;
import java.util.UUID;

/**
 * 難読化を目的とした、KSPのシンボル名を一定の規則に従い生成する
 */
public class ShortSymbolGenerator
{

    /** オブファスケート後のシンボル名の接頭文字 */
    static private final String PREFIX = "_";

    /** 生成するシンボルの最大文字数 */
    static public final int MAX_SYMBOL_LENGTH = 4;

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
        history.clear();
    }

    /**
     * 頭文字に "v"、以降を ユニークな文字列を生成する (桁数: #MAX_SYMBOL_LENGTH )
     */
    static public String generate( String orgName )
    {
        long limit = 0L;
        String uuid = "";
        do
        {
            uuid = UUID.randomUUID().toString();
            uuid = PREFIX + uuid.replaceAll( "-", "" ).substring( 0, MAX_SYMBOL_LENGTH );
        } while( history.containsValue( uuid ) && ++limit < Long.MAX_VALUE );

        if( limit == Long.MAX_VALUE )
        {
            throw new RuntimeException( "Too many symbols declared. Cannot generate any more." );
        }

        history.put( orgName, uuid );
        return uuid;
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
