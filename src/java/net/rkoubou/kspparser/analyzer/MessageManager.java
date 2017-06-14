/* =========================================================================

    MessageManager.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

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

    static public final String PROPERTY_ERROR_LEXICAL                   = "error.lexical";
    static public final String PROPERTY_ERROR_VARIABLE_ONINIT           = "error.variable.declared.oninit";
    static public final String PROPERTY_ERROR_VARIABLE_DECLARED         = "error.variable.already.declared";
    static public final String PROPERTY_ERROR_VARIABLE_RESERVED         = "error.variable.reserved";
    static public final String PROPERTY_ERROR_VARIABLE_PREFIX_RESERVED  = "error.variable.reserved.prefix";
    static public final String PROPERTY_WARN_COMMAND_UNKNOWN            = "warning.command.unknown";

    public enum Level
    {
        ERROR,
        WARNING,         // KONTAKT上のコンパイルでエラーになるかもしれないレベルの警告
        IGNORE_WARNING,  // シュリンクの提示や無視しても動作には支障がないと想定される警告
        IGNORE_COMMAND,  // 定義ファイルに登録されていないコマンドが出現した場合の警告
        IGNORE_VARIABLE, // 定義ファイルに登録されていないビルトイン変数が出現した場合の警告
        INFO,
        DEBUG
    }

    /** 定義ファイルパス */
    static private final String PROPERTIES_PATH;

    /** 変数格納 */
    static private final Properties properties;

    /**
     * static initializer
     */
    static
    {
        PROPERTIES_PATH = System.getProperty( "kspparser.datadir", "data/lang/message" );
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

    /**
     * Ctor.
     */
    private MessageManager(){}

    /**
     * 文法解析中のエラーメッセージを設定ファイル「message.properties」の書式に従い展開
     */
    static public String expand( ParseException src )
    {
        return expand(
            PROPERTY_ERROR_LEXICAL,
            Level.ERROR,
            src.currentToken.beginLine,
            src.currentToken.beginColumn,
            src.currentToken.image.length()
        );
    }

    static public String expand( String propertyKey, Level level, int errorLine, int errorColumn, int tokenLen )
    {
        //
        // ${level}       :エラーレベル
        // ${line}        :行
        // ${colmn}       :位置（トークンの開始）
        // ${tokenLen}    :トークンの文字列長
        //
        if( tokenLen <= 0 )
        {
            tokenLen = Integer.MAX_VALUE;
        }
        String message = properties.getProperty( propertyKey );
        message = message.replace( "${level}",      "" + level.toString() );
        message = message.replace( "${line}",       "" + errorLine );
        message = message.replace( "${colmn}",      "" + errorColumn );
        message = message.replace( "${tokenLen}",   "" + tokenLen );
        return message;
    }

    /**
     * 標準出力に出力する
     */
    static public void println( ParseException src )
    {
        System.out.println( expand( src ) );
    }

    /**
     * 標準出力に出力する
     */
    static public void println( String propertyKey, Level level, int errorLine, int errorColumn, int tokenLen )
    {
        String message = expand( propertyKey, level, errorLine, errorColumn, tokenLen );
        System.out.println( message );
    }

    /**
     * 標準出力に出力する
     */
    static public void println( String propertyKey, Level level, SymbolDefinition symbol )
    {
        String message = expand( propertyKey, level, symbol.line, symbol.colmn, symbol.name.length() );
        message = message.replace( "${symbolname}", symbol.name );
        System.out.println( message );
    }

    /**
     * 標準出力に出力する(レベル：ERROR)
     */
    static public void printlnE( String propertyKey, SymbolDefinition symbol )
    {
        println( propertyKey, Level.ERROR, symbol );
    }

    /**
     * 標準出力に出力する(レベル：WARN)
     */
    static public void printlnW( String propertyKey, SymbolDefinition symbol )
    {
        println( propertyKey, Level.WARNING, symbol );
    }

    /**
     * 標準出力に出力する(レベル：INFO)
     */
    static public void printlnI( String propertyKey, SymbolDefinition symbol )
    {
        println( propertyKey, Level.INFO, symbol );
    }

    /**
     * 標準出力に出力する(レベル：DEBUG)
     */
    static public void printlnD( String propertyKey, SymbolDefinition symbol )
    {
        println( propertyKey, Level.DEBUG, symbol );
    }
}
