/* =========================================================================

    AnalyzeErrorCounter.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.io.PrintStream;

/**
 * 解析中のエラー件数収集クラス
 */
public class AnalyzeErrorCounter
{

    /** 解析中に検知したエラー総数 */
    static private int errorCount = 0;

    /** 解析中に検知した警告総数 */
    static private int warningCount = 0;

    /**
     * ctor
     */
    private AnalyzeErrorCounter()
    {}

    /**
     * エラー総数を＋１
     */
    static public void e()
    {
        errorCount++;
    }

    /**
     * 警告総数を＋１
     */
    static public void w()
    {
        warningCount++;
    }

    /**
     * エラー・警告総数を０にリセットする
     */
    static public void reset()
    {
        errorCount   = 0;
        warningCount = 0;
    }

    /**
     * エラー総数を取得する
     */
    static public int countE()
    {
        return errorCount;
    }

    /**
     * エラー総数が１つ以上あるかどうか
     */
    static public boolean hasError()
    {
        return errorCount > 0;
    }

    /**
     * 警告総数を取得する
     */
    static public int countW()
    {
        return errorCount;
    }

    /**
     * エラー総数が１つ以上あるかどうか
     */
    static public boolean hasWarning()
    {
        return warningCount > 0;
    }

    /**
     * 結果を文字列形式で表現する
     */
    static public void dump( PrintStream p )
    {
        if( hasError() )
        {
            p.println( errorCount + " Error(s)" );
        }
        if( hasWarning() )
        {
            p.println( warningCount + " Warning(s)" );
        }
    }
}
