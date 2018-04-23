/* =========================================================================

    ApplicationConstants.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser;

/**
 * アプリケーション全体に関連する定数の定義
 */
public class ApplicationConstants
{
    /** VM引数 -D dataフォルダの明示的指定時のプロパティ名 */
    static public final String SYSTEM_PROPERTY_DATADIR = "kspparser.datadir";

    /** VM引数 -Dkspparser.datadir がなかった時のデフォルトの dataフォルダの相対パス */
    static public final String DEFAULT_DATADIR = "data";

    /** */
    static public String DATA_DIR;

    /**
     * static initializer
     */
    static
    {
        String dir = System.getProperty( SYSTEM_PROPERTY_DATADIR );
        if( dir == null )
        {
            DATA_DIR = DEFAULT_DATADIR;
        }
        else
        {
            DATA_DIR = dir;
        }
    }

    /**
     * ctor.
     */
    private ApplicationConstants(){}

}
