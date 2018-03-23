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
public class MessageManager implements AnalyzerConstants
{

    //--------------------------------------------------------------------------
    // Lexical
    //--------------------------------------------------------------------------
    static public final String PROPERTY_ERROR_SYNTAX                        = "error.syntax";

    //--------------------------------------------------------------------------
    // General
    //--------------------------------------------------------------------------
    static public final String PROPERTY_ERROR_GENERAL_SYMBOL_PREFIX_NUMERIC = "error.general.symbol.prefix.numeric";

    //--------------------------------------------------------------------------
    // Variable
    //--------------------------------------------------------------------------
    static public final String PROPERTY_ERROR_VARIABLE_ONINIT           = "error.variable.declared.oninit";
    static public final String PROPERTY_ERROR_VARIABLE_DECLARED         = "error.variable.already.declared";
    static public final String PROPERTY_ERROR_VARIABLE_RESERVED         = "error.variable.reserved";
    static public final String PROPERTY_ERROR_VARIABLE_PREFIX_RESERVED  = "error.variable.reserved.prefix";
    static public final String PROPERTY_WARN_UI_VARIABLE_UNKNOWN        = "warning.variable.ui.unknown";

    //--------------------------------------------------------------------------
    // PreProcessor
    //--------------------------------------------------------------------------
    static public final String PROPERTY_WARN_PREPROCESSOR_UNKNOWN_DEF  = "warn.preprocessor.unknown.defined";
    static public final String PROPERTY_WARN_PREPROCESSOR_ALREADY_DEF  = "warn.preprocessor.already.defined";

    //--------------------------------------------------------------------------
    // Callback
    //--------------------------------------------------------------------------
    static public final String PROPERTY_ERROR_CALLBACK_DECLARED         = "error.callback.already.declared";
    static public final String PROPERTY_WARN_CALLBACK_UNKNOWN           = "warning.callback.unknown";

    //--------------------------------------------------------------------------
    // Command
    //--------------------------------------------------------------------------
    static public final String PROPERTY_WARN_COMMAND_UNKNOWN            = "warning.command.unknown";

    //--------------------------------------------------------------------------
    // User Function
    //--------------------------------------------------------------------------
    static public final String PROPERTY_ERROR_FUNCTION_DECLARED         = "error.userfunction.already.declared";
    static public final String PROPERTY_ERROR_USERFUNCTION_NOT_DECLARED = "error.userfunction.not.declared";

    //--------------------------------------------------------------------------
    // Semantic Analyzer
    //--------------------------------------------------------------------------
    static public final String PROPERTY_ERROR_SEMANTIC_EXPRESSION_INVALID       = "error.semantic.expression.invalid";
    static public final String PROPERTY_ERROR_SEMANTIC_VARIABLE_NOT_DECLARED    = "error.semantic.variable.not.declared";
    static public final String PROPERTY_WARNING_SEMANTIC_VARIABLE_INIT          = "warning.semantic.variable.init";
    static public final String PROPERTY_ERROR_SEMANTIC_COMMAND_NOT_ALLOWED      = "error.semantic.command.not.allowed";
    static public final String PROPERTY_WARNING_SEMANTIC_COMMAND_UNKNOWN        = "warning.semantic.command.unknown";
    static public final String PROPERTY_ERROR_SEMANTIC_COMMAND_ARGCOUNT         = "error.semantic.command.argcount";
    static public final String PROPERTY_ERROR_SEMANTIC_INCOMPATIBLE_ARG         = "error.semantic.incompatible.arg";
    static public final String PROPERTY_ERROR_SEMANTIC_COND_BOOLEAN             = "error.semantic.cond.boolean";
    static public final String PROPERTY_ERROR_SEMANTIC_BINOPR_DIFFERENT         = "error.semantic.binopr.different";
    static public final String PROPERTY_ERROR_SEMANTIC_EXPRESSION_CONSTANTONLY  = "error.semantic.expression.constantonly";
    static public final String PROPERTY_ERROR_SEMANTIC_VARIABLE_REQUIRED_INITIALIZER = "error.semantic.variable.required.initializer";
    static public final String PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_INITIALIZER = "error.semantic.variable.invalid.initializer";
    static public final String PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_STRING_INITIALIZER = "error.semantic.variable.invalid.string.initializer";
    static public final String PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_INITIALIZER_TYPE = "error.semantic.variable.invalid.initializer.type";
    static public final String PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_INITIALIZER_STRINGADD = "error.semantic.variable.invalid.initializer.stringadd";
    static public final String PROPERTY_ERROR_SEMANTIC_VARIABLE_NOTARRAY        = "error.semantic.variable.notarray";
    static public final String PROPERTY_ERROR_SEMANTIC_VARIABLE_DECLARE_CONST = "error.semantic.variable.declare.const";
    static public final String PROPERTY_ERROR_SEMANTIC_VARIABLE_NOT_ARRAYSIZE   = "error.semantic.variable.not.arraysize";
    static public final String PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_ARRAYSIZE = "error.semantic.variable.invalid.arraysize";
    static public final String PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_MAXARRAYSIZE = "error.semantic.variable.invalid.maxarraysize";
    static public final String PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_ARRAYINITILIZER = "error.semantic.variable.invalid.arrayinitilizer";
    static public final String PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_UITYPE  = "error.semantic.variable.invalid.uitype";
    static public final String PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_UIINITIALIZER_COUNT = "error.semantic.variable.invalid.uiinitializer.count";
    static public final String PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_UIINITIALIZER_TYPE = "error.semantic.variable.invalid.uiinitializer.type";
    static public final String PROPERTY_ERROR_SEMANTIC_ASSIGN_NOTVARIABLE       = "error.semantic.assign.notvariable";
    static public final String PROPERTY_ERROR_SEMANTIC_ASSIGN_CONSTVARIABLE     = "error.semantic.assign.constvariable";
    static public final String PROPERTY_ERROR_SEMANTIC_ASSIGN_TYPE_NOTCOMPATIBLE = "error.semantic.assign.type.notcompatible";
    static public final String PROPERTY_ERROR_SEMANTIC_SINGLE_OPERATOR_NUMONLY  = "error.semantic.single.operator.numonly";
    static public final String PROPERTY_ERROR_SEMANTIC_SINGLE_OPERATOR_LNOT     = "error.semantic.single.operator.lnot";
    static public final String PROPERTY_ERROR_SEMANTIC_ARRAY_ELEMENT_INTONLY    = "error.semantic.array.element.intonly";
    static public final String PROPERTY_WARNING_SEMANTIC_CASEVALUE              = "warning.semantic.casevalue";
    static public final String PROPERTY_ERROR_SEMANTIC_CASEVALUE_CONSTONLY      = "error.semantic.casevalue.constonly";
    static public final String PROPERTY_ERROR_SEMANTIC_CONDITION_INVALID        = "error.semantic.condition.invalid";

    //--------------------------------------------------------------------------
    // サジェスト
    //--------------------------------------------------------------------------
    static public final String PROPERTY_WARNING_SEMANTIC_UNUSE_VARIABLE         = "warning.semantic.unuse.variable";
    static public final String PROPERTY_WARNING_SEMANTIC_UNUSE_FUNCTION         = "warning.semantic.unuse.function";
    static public final String PROPERTY_WARNING_TOOMUCH_LINECOUNT               = "warning.toomuch.linecount";
    static public final String PROPERTY_WARNING_SEMANTIC_INFO_VARNAME           = "warning.semantic.info.varname";

    public enum Level
    {
        ERROR,
        WARNING,         // KONTAKT上のコンパイルでエラーになるかもしれないレベルの警告
        IGNORE_WARNING,  // シュリンクの提示や無視しても動作には支障がないと想定される警告
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
        String dir = System.getProperty( SYSTEM_PROPERTY_DATADIR );
        if( dir == null )
        {
            dir = "data/lang";
        }
        else
        {
            dir += "/lang";
        }
        PROPERTIES_PATH = dir + "/message";
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
            PROPERTY_ERROR_SYNTAX,
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
    static public void println( String propertyKey, Level level, SymbolDefinition symbol, String... ext )
    {
        int extIndex = 1;
        String message = expand( propertyKey, level, symbol.position.beginLine, symbol.position.beginColumn, symbol.getName().length() );
        message = message.replace( "${symbolname}", symbol.getName() );
        for( String s : ext )
        {
            message = message.replace( "${ext" + extIndex + "}", s );
            extIndex++;
        }
        System.out.println( message );
    }

    /**
     * 標準出力に出力する(レベル：ERROR)
     */
    static public void printlnE( String propertyKey, SymbolDefinition symbol, String... ext )
    {
        println( propertyKey, Level.ERROR, symbol, ext );
    }

    /**
     * 標準出力に出力する(レベル：WARN)
     */
    static public void printlnW( String propertyKey, SymbolDefinition symbol, String... ext )
    {
        println( propertyKey, Level.WARNING, symbol, ext );
    }

    /**
     * 標準出力に出力する(レベル：INFO)
     */
    static public void printlnI( String propertyKey, SymbolDefinition symbol, String... ext )
    {
        println( propertyKey, Level.INFO, symbol, ext );
    }

    /**
     * 標準出力に出力する(レベル：DEBUG)
     */
    static public void printlnD( String propertyKey, SymbolDefinition symbol, String... ext )
    {
        println( propertyKey, Level.DEBUG, symbol, ext );
    }
}
