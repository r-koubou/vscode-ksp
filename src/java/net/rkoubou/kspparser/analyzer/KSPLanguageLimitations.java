/* =========================================================================

    KSPLanguageLimitations.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.util.KSPParserProperties;

/**
 * KSPの言語仕様、バグなどに起因するパラメータ類の限界値の定義
 */
public class KSPLanguageLimitations
{
    /** 定義ファイルパス */
    static private final String PROPERTIES_PATH = "ksp_limitations.properties";

    /** コールバック・ユーザー関数の行数オーバーフローのしきい値 */
    static public int OVERFLOW_LINES;

     /** 配列変数宣言時の要素数の上限 */
    static public int MAX_KSP_ARRAY_SIZE;

    static
    {
        try
        {
            KSPParserProperties p = new KSPParserProperties( PROPERTIES_PATH );
            OVERFLOW_LINES     = p.getInt( "ksp.overflow.lines", 4950 );
            MAX_KSP_ARRAY_SIZE = p.getInt( "ksp.array.size", 32768 );
        }
        catch( Throwable e )
        {
            e.printStackTrace();
            System.exit( 1 );
        }
    }

    /**
     * ctor.
     */
    private KSPLanguageLimitations(){}

}