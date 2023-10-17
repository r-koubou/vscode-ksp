/* =========================================================================

    UTF8Properties.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Java Propertiesファイルを UTF-8で扱うためのラッパー
 */
public class UTF8Properties
{
    /**
     * ctor.
     */
    private UTF8Properties(){}

    /**
     * 指定された入力ストリームのコンテンツがUTF-8 エンコードであることを条件に Properties クラスを生成する
     */
    static public Properties load( InputStream in ) throws IOException
    {
        InputStreamReader reader = new InputStreamReader( in, "utf-8" );
        Properties p = new Properties();
        p.load( reader );
        return p;
    }

    /**
     * 指定されたパスのコンテンツがUTF-8 エンコードであることを条件に Properties クラスを生成する
     */
    static public Properties load( String path ) throws IOException
    {
        return load( new FileInputStream( path ) );
    }
}
