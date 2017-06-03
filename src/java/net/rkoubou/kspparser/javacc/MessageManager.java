/* =========================================================================

    MessageManager.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.javacc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Properties;

import net.rkoubou.kspparser.javacc.generated.ParseException;

/**
 * パース中処理中のメッセージ処理出力に関するマネージャー。
 * I18Nや変数展開機能を提供する。
 */
public class MessageManager
{

    /** 定義ファイルパス */
    static private final String PROPERTIES_PATH = "data/lang/message";

    /** 変数格納 */
    static private final Properties properties;

    //////////////////////////////////////////////////////////////////////////
    /**
     * static initializer
     */
    static
    {
        properties = new Properties();
        try
        {
            String localizedSuffix = Locale.getDefault().getLanguage();

            //
            // 全言語共通のプロパティをロード
            //
            load( properties, PROPERTIES_PATH + ".properties" );

            //
            // 同じフォルダに +."国コード名(小文字)" があればマージ
            //
            if( localizedSuffix.length() > 0 )
            {
                String path = PROPERTIES_PATH + "_" + localizedSuffix.toLowerCase() + ".properties";
                File file   = new File( path );
                if( file.exists() )
                {
                    Properties p = new Properties();
                    load( p, path );
                    for( String key : p.stringPropertyNames() )
                    {
                        properties.setProperty( key, p.getProperty( key ) );
                    }
                }
            }

        }
        catch( Throwable e )
        {
            e.printStackTrace();
            System.exit( 1 );
        }
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * Loading
     */
    static private void load( Properties dest, String path ) throws IOException
    {
        InputStreamReader reader = null;
        try
        {
            reader = new InputStreamReader( new FileInputStream( path ), "utf-8" );
            dest.load( reader );
        }
        finally
        {
            if( reader != null )
            {
                try { reader.close(); } catch( Throwable e ) {}
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * Ctor.
     */
    private MessageManager(){}

    //////////////////////////////////////////////////////////////////////////
    /**
     * 文法解析中のエラーメッセージを設定ファイル「message.properties」の書式に従い展開
     */
    static public String expand( ParseException src )
    {
        //
        // ${line}        :行
        // ${colmn}       :位置（トークンの開始）
        // ${token}       :該当のトークン
        // ${tokenLen}    :トークンの文字列長
        //
        String message = properties.getProperty( "error.lexical" );
        message = message.replace( "${line}",         "" + src.currentToken.beginLine );
        message = message.replace( "${colmn}",   "" + src.currentToken.beginColumn );
        message = message.replace( "${token}",        src.currentToken.image );
        message = message.replace( "${tokenLen}",     "" + src.currentToken.image.length() );
        return message;
    }

    static public String expand( int errorLine, int errorColumn, char curChar )
    {
        //
        // ${line}        :行
        // ${colmn}       :位置（トークンの開始）
        // ${token}       :該当のトークン
        // ${tokenLen}    :トークンの文字列長
        //
        String message = properties.getProperty( "error.lexical" );
        message = message.replace( "${line}",       "" + errorLine );
        message = message.replace( "${colmn}",      "" + errorColumn );
        message = message.replace( "${token}",      String.valueOf( curChar ) );
        message = message.replace( "${tokenLen}",   "1" );
        return message;
    }
}
