/* =========================================================================

    ReservedSymbolManager.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import net.rkoubou.kspparser.analyzer.SymbolDefinition.SymbolType;
import net.rkoubou.kspparser.javacc.generated.ASTCallCommand;
import net.rkoubou.kspparser.javacc.generated.ASTCallbackArgumentList;
import net.rkoubou.kspparser.javacc.generated.ASTCallbackDeclaration;
import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclaration;
import net.rkoubou.kspparser.javacc.generated.KSPParserTreeConstants;

/**
 * data/reserved に配備した予約済み変数、コマンド、コールバック、関数など各種シンボルの定義ファイルからデシリアライズする
 */
public class ReservedSymbolManager implements KSPParserTreeConstants, AnalyzerConstants
{
    /** 定義ファイルの場所 */
    static public final String BASE_DIR;

    /** 定義ファイルのデリミタ */
    static public final String DELIMITER= "\t";

    /** 行コメント文字 */
    static public final String LINE_COMMENT = "#";

    /** split処理で使用する、条件式ORの文字列表現 */
    static public final String SPLIT_COND_OR = "||";

    /** split処理で使用する、条件式NOTの文字列表現 */
    static public final String COND_NOT = "!";

    /** split処理で使用する、条件式ORの正規表現 */
    static public final String REGEX_SPLIT_COND_OR = "\\|\\|";

    /** シングルトンインスタンス */
    static private final ReservedSymbolManager instance = new ReservedSymbolManager();

    /** 予約済みUIタイプ変数 */
    private final HashMap<String,UIType> uiTypes = new HashMap<String,UIType>();

    /** 予約済み変数 */
    private final HashMap<String,Variable> variables = new HashMap<String,Variable>( 512 );

    /** 予約済みコマンド */
    private final HashMap<String,Command> commands = new HashMap<String,Command>( 256 );

    /** 予約済みコールバック */
    private HashMap<String,Callback> callbacks = new HashMap<String,Callback>();

    /**
     * static initializer
     */
    static
    {
        String dir = System.getProperty( SYSTEM_PROPERTY_DATADIR );
        if( dir == null )
        {
            dir = "data/symbols";
        }
        else
        {
            dir += "/symbols";
        }
        BASE_DIR = dir;
    }

    /**
     * ctor
     */
    private ReservedSymbolManager()
    {
    }

    /**
     * 定義ファイルを再読み込み
     */
    public void load() throws IOException
    {
        loadUITypes();
        loadVariables();
        loadCallbacks();
        loadCommands();
    }

    /**
     * インスタンスを取得する
     */
    static public ReservedSymbolManager getManager()
    {
        return instance;
    }

    /**
     * 指定されたUI型テーブルにこのクラスが読み込んだ外部変数を適用する
     */
    public void apply( UITypeTable dest )
    {
        for( String key : uiTypes.keySet() )
        {
            dest.add( uiTypes.get( key ) );
        }
    }

    /**
     * 指定された変数テーブルにこのクラスが読み込んだ外部変数を適用する
     */
    public void apply( VariableTable dest )
    {
        for( String key : variables.keySet() )
        {
            dest.add( variables.get( key ) );
        }
    }

    /**
     * 指定されたコマンドテーブルにこのクラスが読み込んだ外部コールバックを適用する
     */
    public void apply( CommandTable dest )
    {
        for( String key : commands.keySet() )
        {
            dest.add( commands.get( key ) );
        }
    }

    /**
     * 指定されたコールバックテーブルにこのクラスが読み込んだ外部コールバックを適用する
     */
    public void apply( CallbackTable dest )
    {
        for( String name : callbacks.keySet() )
        {
            Callback v = callbacks.get( name );
            dest.add( v, name );
        }
    }

    /**
     * UIタイプの予約済み定義ファイルから UIType クラスインスタンスを生成する
     */
    private void loadUITypes() throws IOException
    {
        File f            = new File( BASE_DIR, "uitypes.txt" );
        BufferedReader br = new BufferedReader( new InputStreamReader( new FileInputStream( f ), "UTF-8" ) );
        try
        {
            String line;
            uiTypes.clear();

            while( ( line = br.readLine() ) != null )
            {
                line = line.trim();
                if( line.startsWith( LINE_COMMENT ) || line.length() == 0 )
                {
                    continue;
                }

                String[] data = line.split( DELIMITER );
                String name                 = data[ 0 ];
                boolean constant            = "Y".equals( data[ 1 ] );
                boolean initializerRequired = "Y".equals( data[ 2 ] );
                int type                    = toVariableType( data[ 3 ] ).type;
                int[] typeList = UIType.EMPTY_INITIALIZER_TYPE_LIST;

                //--------------------------------------------------------------------------
                // 初期値代入式が必須の場合
                //--------------------------------------------------------------------------
                if( data.length >= 5 )
                {
                    typeList = new int[ data.length - 4 ];
                    for( int i = 4, x = 0; i < data.length; i++, x++ )
                    {
                        typeList[ x ] = toVariableType( data[ i ] ).type;
                    }
                }
                UIType ui = new UIType( name, true, type, constant, initializerRequired, typeList );
                uiTypes.put( name, ui );
            }
        }
        finally
        {
            try { br.close(); } catch( Throwable e ) {}
        }
    }


    /**
     * 変数の予約済み定義ファイルから Variable クラスインスタンスを生成する
     */
    private void loadVariables() throws IOException
    {
        File f            = new File( BASE_DIR, "variables.txt" );
        BufferedReader br = new BufferedReader( new InputStreamReader( new FileInputStream( f ), "UTF-8" ) );
        try
        {
            String line;
            variables.clear();

            while( ( line = br.readLine() ) != null )
            {
                line = line.trim();
                if( line.startsWith( LINE_COMMENT ) || line.length() == 0 )
                {
                    continue;
                }

                String[] data               = line.split( DELIMITER );
                Variable v                  = toVariableType( data[ 0 ] );
                String name                 = v.toKSPTypeCharacter() + data[ 1 ];
                boolean availableOnInit     = "Y".equals( data[ 2 ] );

                v.setName( name );
                v.accessFlag        = ACCESS_ATTR_CONST;      // ビルトイン変数に代入を許可させない
                v.availableOnInit   = availableOnInit;        // on init 内で使用可能な変数かどうか。一部のビルトイン定数ではそれを許可していない。
                v.reserved          = true;                   // 予約変数
                v.referenced        = true;                   // 予約変数につき、使用・未使用に関わらず参照済みマーク
                v.state             = SymbolState.LOADED;     // 予約変数につき、値代入済みマーク
                v.value             = v.getDefaultValue();
                variables.put( name, v );
            }
        }
        finally
        {
            try { br.close(); } catch( Throwable e ) {}
        }
    }

    /**
     * コールバックの予約済み定義ファイルから Variable クラスインスタンスを生成する
     */
    private void loadCommands() throws IOException
    {
        File f            = new File( BASE_DIR, "commands.txt" );
        BufferedReader br = new BufferedReader( new InputStreamReader( new FileInputStream( f ), "UTF-8" ) );
        try
        {
            String line;
            commands.clear();

            while( ( line = br.readLine() ) != null )
            {
                line = line.trim();
                if( line.startsWith( LINE_COMMENT ) || line.length() == 0 )
                {
                    continue;
                }

                String[] data               = line.split( DELIMITER );
                String returnType           = data[ 0 ];
                String name                 = data[ 1 ];
                String availableCallback    = data[ 2 ];
                boolean hasParenthesis      = false;

                //--------------------------------------------------------------------------
                // data[3] 以降：引数を含む場合
                // 引数のAST、変数を生成
                //--------------------------------------------------------------------------
                ArrayList<CommandArgument> args = new ArrayList<CommandArgument>();
                if( data.length >= 4 )
                {
                    hasParenthesis = true;
                    final int len = data.length;
                    for( int i = 3; i < len; i++ )
                    {
                        //--------------------------------------------------------------------------
                        // 複数のデータ型を許容するコマンドがあるので単一にせずにリストにストックしていく
                        //--------------------------------------------------------------------------
                        String typeString = data[ i ];
                        args.add( toVariableTypeForArgument( typeString ) );
                    }
                }
                //--------------------------------------------------------------------------
                // コマンドのAST、変数を生成
                //--------------------------------------------------------------------------
                {
                    Command newItem;
                    ASTCallCommand ast = new ASTCallCommand( JJTCALLCOMMAND );
                    ast.symbol.setName( name );
                    newItem = new Command( ast );

                    if( args.size() > 0 )
                    {
                        newItem.argList.addAll( args );
                    }
                    newItem.hasParenthesis          = hasParenthesis;
                    toReturnTypeForCommand( returnType, newItem.returnType );
                    newItem.symbolType              = SymbolType.Command;
                    newItem.reserved                = true;
                    newItem.availableCallbackList.clear();
                    toAvailableCommandOnCallbackList( availableCallback, newItem.availableCallbackList );
                    commands.put( name, newItem );
                }

            } //~while( ( line = br.readLine() ) != null )
        }
        finally
        {
            try { br.close(); } catch( Throwable e ) {}
        }
    }

    /**
     * コールバックの予約済み定義ファイルから Variable クラスインスタンスを生成する
     */
    private void loadCallbacks() throws IOException
    {
        File f            = new File( BASE_DIR, "callbacks.txt" );
        BufferedReader br = new BufferedReader( new InputStreamReader( new FileInputStream( f ), "UTF-8" ) );
        try
        {
            String line;
            callbacks.clear();

            while( ( line = br.readLine() ) != null )
            {
                line = line.trim();
                if( line.startsWith( LINE_COMMENT ) || line.length() == 0 )
                {
                    continue;
                }

                String[] data = line.split( DELIMITER );
                String name   = data[ 0 ];
                boolean dup   = "Y".equals( data[ 1 ] );

                //--------------------------------------------------------------------------
                // data[2] 以降：引数を含む場合
                // 引数のAST、変数を生成
                //--------------------------------------------------------------------------
                ArrayList<Argument> args = new ArrayList<Argument>();
                if( data.length >= 3 )
                {
                    final int len            = data.length;
                    for( int i = 2; i < len; i++ )
                    {
                        String typeString = data[ i ];
                        boolean requireDeclarationOnInit = false;

                        if( typeString.startsWith( "&" ) )
                        {
                            // ui_control など引数==宣言した変数の場合
                            requireDeclarationOnInit = true;
                            typeString =typeString.substring( 1 );
                        }

                        Variable v    = toVariableType( typeString );
                        Argument a    = new Argument( v );
                        a.setName( "<undefined>" );                             // シンボル収集時にマージ
                        a.requireDeclarationOnInit = requireDeclarationOnInit;  // 引数の変数が on init で宣言した変数かどうか
                        a.reserved    = true;                                   // 予約変数
                        a.referenced  = true;                                   // 予約変数につき、使用・未使用に関わらず参照済みマーク
                        a.state       = SymbolState.LOADED;                     // 予約変数につき、値代入済みマーク
                        args.add( a );
                    }
                }
                //--------------------------------------------------------------------------
                // コールバックのAST、変数を生成
                //--------------------------------------------------------------------------
                {
                    Callback newItem;
                    ASTCallbackDeclaration ast = new ASTCallbackDeclaration( JJTCALLBACKDECLARATION );
                    ast.symbol.setName( name );
                    if( args.size() > 0 )
                    {
                        ASTCallbackArgumentList astList = new ASTCallbackArgumentList( JJTCALLBACKARGUMENTLIST );
                        for( Argument a : args )
                        {
                            astList.args.add( a.getName() );
                        }
                        ast.jjtAddChild( astList, 0 );
                        newItem = new Callback( ast );
                    }
                    else
                    {
                        newItem = new Callback( ast );
                    }
                    newItem.setName( name );
                    newItem.symbolType     = SymbolType.Callback;
                    newItem.reserved       = true;
                    newItem.declared       = false;
                    newItem.setAllowDuplicate( dup );
                    callbacks.put( name, newItem );
                }

            } //~while( ( line = br.readLine() ) != null )
        }
        finally
        {
            try { br.close(); } catch( Throwable e ) {}
        }
    }

    /**
     * 型識別文字から Variableクラスのtypeに格納する形式の値に変換する
     */
    public Variable toVariableType( String t )
    {
        Variable ret = new Variable( new ASTVariableDeclaration( JJTVARIABLEDECLARATION ) );
        int type          = TYPE_NONE;
        int accessFlag    = ACCESS_ATTR_NONE;
        UIType uiTypeInfo = null;

        t = t.intern();
        if( t == "*" )
        {
            type = TYPE_ALL;
        }
        else if( t == "*[]" )
        {
            type = TYPE_MULTIPLE | TYPE_ATTR_ARRAY;
        }
        else if( t == "V" )
        {
            type = TYPE_VOID;
        }
        else if( t == "I" || t == "@I" )
        {
            type = TYPE_INT;
        }
        else if( t == "I[]" )
        {
            type = TYPE_INT | TYPE_ATTR_ARRAY;
        }
        else if( t == "R" || t == "@R" )
        {
            type = TYPE_REAL;
        }
        else if( t == "R[]" )
        {
            type = TYPE_REAL | TYPE_ATTR_ARRAY;
        }
        else if( t == "S" || t == "@S" )
        {
            type = TYPE_STRING;
        }
        else if( t == "S[]" )
        {
            type = TYPE_STRING | TYPE_ATTR_ARRAY;
        }
        else if( t == "B" || t == "@B" )
        {
            type = TYPE_BOOL;
        }
        else if( t == "B[]" )
        {
            type = TYPE_BOOL | TYPE_ATTR_ARRAY;
        }
        else if( t == "PP" )
        {
            type = TYPE_PREPROCESSOR_SYMBOL;
        }
        else if( t == "KEY" )
        {
            type = TYPE_KEYID;
        }
        // 全UIタイプを許容する場合
        else if( t.equals( "ui_*" ) )
        {
            uiTypeInfo  = UIType.ANY_UI;
            accessFlag |= ACCESS_ATTR_UI;
        }
        // 指定のUIタイプの場合
        else if( t.startsWith( "ui_" ) )
        {
            boolean found = false;
            if( uiTypes.containsKey( t ) )
            {
                uiTypeInfo  = uiTypes.get( t );
                accessFlag |= ACCESS_ATTR_UI;
                found = true;
            }
            if( !found )
            {
                throw new IllegalArgumentException( "Unknown type : " + t );
            }
        }
        else
        {
            throw new IllegalArgumentException( "Unknown type : " + t );
        }

        if( t.startsWith( "@" ) )
        {
            accessFlag |= ACCESS_ATTR_CONST;
        }

        ret.setName( "tmp" );
        ret.type       = type;
        ret.accessFlag = accessFlag;
        ret.uiTypeInfo = uiTypeInfo;

        return ret;
    }

    /**
     * 型識別文字から戻り値の値に変換する(コマンドによって複数の戻り値がある)
     */
    public void toReturnTypeForCommand( String t, ReturnType dest )
    {
        t = t.intern();

        String[] orCond  = t.split( REGEX_SPLIT_COND_OR );

        //--------------------------------------------------------------------------
        // A または B または .... n の場合
        //--------------------------------------------------------------------------
        if( orCond.length >= 2 )
        {
            for( String i : orCond )
            {
                dest.typeList.add( toVariableType( i ).type );
            }
        }
        else
        {
            dest.typeList.add( toVariableType( t ).type );
        }
    }

    /**
     * 型識別文字から引数の値に変換する(コマンド引数で複数の型を扱う場合)
     */
    public CommandArgument toVariableTypeForArgument( String t )
    {
        t = t.intern();
        CommandArgument ret;
        ArrayList<Variable> args          = new ArrayList<Variable>();

        String[] orCond  = t.split( REGEX_SPLIT_COND_OR );

        //--------------------------------------------------------------------------
        // A または B または .... n の場合
        //--------------------------------------------------------------------------
        if( orCond.length >= 2 )
        {
            for( String i : orCond )
            {
                Variable v = toVariableType( i );
                args.add( v );
            }
        }
        else
        {
            Variable v = toVariableType( t );
            args.add( v );
        }

        //--------------------------------------------------------------------------
        // 共通の値設定
        //--------------------------------------------------------------------------
        for( int x = 0; x < args.size(); x++ )
        {
            Variable v = args.get( x );
            v.reserved                  = false;                // KONTAKT内部のビルトインコマンドにつき、非予約変数
            v.referenced                = true;                 // KONTAKT内部のビルトインコマンドにつき、使用・未使用に関わらず参照済みマーク
            v.state                     = SymbolState.LOADED;   // KONTAKT内部のビルトインコマンドにつき、値代入済みマーク
            if( v.uiTypeInfo != null )
            {
                v.uiTypeName = v.uiTypeInfo.name;
            }
        }

        ret = new CommandArgument( args );
        return ret;
    }

    /**
     * コマンドテーブル生成用：利用可能なコールバック名の記述を元に、利用可能リストを生成する
     */
    public void toAvailableCommandOnCallbackList( String callbackName, HashMap<String,Callback> dest )
    {
        callbackName      = callbackName.intern();
        String[] orCond   = callbackName.split( REGEX_SPLIT_COND_OR );

        //--------------------------------------------------------------------------
        // 全コールバックで使用可能
        //--------------------------------------------------------------------------
        if( callbackName == "*" )
        {
            dest.putAll( callbacks );
            return;
        }
        //--------------------------------------------------------------------------
        // A または B または .... n の場合
        //--------------------------------------------------------------------------
        if( orCond.length >= 2 )
        {
            for( String i : orCond )
            {
                if( callbacks.containsKey( i ) )
                {
                    dest.put( i, callbacks.get( i ) );
                }
            }
        }
        //--------------------------------------------------------------------------
        // A 以外の場合
        //--------------------------------------------------------------------------
        else if( callbackName.startsWith( COND_NOT ) )
        {
            String exclude = callbackName.substring( 1 );
            for( String key : callbacks.keySet() )
            {
                if( !key.equals( exclude ) )
                {
                    dest.put( key, callbacks.get( key ) );
                }
            }
        }
        //--------------------------------------------------------------------------
        // 単体のコールバック指定
        //--------------------------------------------------------------------------
        else
        {
            if( callbacks.containsKey( callbackName ) )
            {
                dest.put( callbackName, callbacks.get( callbackName ) );
            }
        }
    }

    /**
     * Unit test
     */
    static public void main( String[] args ) throws Throwable
    {
        // command: java -classpath ./target/classes/ net.rkoubou.kspparser.analyzer.ReservedSymbolManager

        ReservedSymbolManager mgr = ReservedSymbolManager.getManager();
        mgr.load();
    }
}
