/* =========================================================================

    KSPParserProperties.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.rkoubou.kspparser.ApplicationConstants;

public class KSPParserProperties
{
    /** プロパティ格納先 */
    protected Properties properties = new Properties();

    /**
     * ctor.
     */
    public KSPParserProperties( String path ) throws IOException
    {
        this( path, ApplicationConstants.DATA_DIR );
    }

    /**
     * ctor.
     */
    public KSPParserProperties( String path, String dir ) throws IOException
    {
        String propertiesPath = path;
        InputStream in        = null;
        if( dir == null )
        {
            dir = ApplicationConstants.DEFAULT_DATADIR;
        }
        propertiesPath = dir + "/" + path;

        try
        {
            properties = UTF8Properties.load( propertiesPath );
        }
        finally
        {
            StreamCloser.close( in );
        }
    }

    /**
     * Propertiesインスタンスを取得する
     */
    public Properties get()
    {
        return properties;
    }

    /**
     * 指定されたキーから値の取得を試みる
     */
    public String getInt( String key, String defaultValue )
    {
        return properties.getProperty( key, defaultValue ).trim();
    }

    /**
     * 指定されたキーの値が整数の場合、値を int として取得を試みる
     */
    public int getInt( String key, int defaultValue )
    {
        String str = properties.getProperty( key, "" ).trim();
        if( str.length() == 0 )
        {
            return defaultValue;
        }
        return Integer.parseInt( str );
    }
}
