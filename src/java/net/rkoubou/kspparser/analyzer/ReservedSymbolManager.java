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

import net.rkoubou.kspparser.analyzer.CommandArgument.CondType;
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

    /** シングルトンインスタンス */
    static private final ReservedSymbolManager instance = new ReservedSymbolManager();

    /** 予約済みUIタイプ変数 */
    private final ArrayList<UIType> uiTypes = new ArrayList<UIType>();

    /** 予約済み変数 */
    private final ArrayList<Variable> variables = new ArrayList<Variable>( 512 );

    /** 予約済みコマンド */
    private final ArrayList<Command> commands = new ArrayList<Command>( 256 );

    /** 予約済みコールバック */
    private Callback[] callbacks = new Callback[ 0 ];

    /**
     * static initializer
     */
    static
    {
        String dir = System.getProperty( SYSTEM_PROPERTY_DATADIR );
        if( dir == null )
        {
            dir = "data/reserved";
        }
        else
        {
            dir += "/reserved";
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
        for( UIType v : uiTypes )
        {
            dest.add( v );
        }
    }

    /**
     * 指定された変数テーブルにこのクラスが読み込んだ外部変数を適用する
     */
    public void apply( VariableTable dest )
    {
        for( Variable v : variables )
        {
            dest.add( v );
        }
    }

    /**
     * 指定されたコマンドテーブルにこのクラスが読み込んだ外部コールバックを適用する
     */
    public void apply( CommandTable dest )
    {
        for( Command v : commands )
        {
            dest.add( v );
        }
    }

    /**
     * 指定されたコールバックテーブルにこのクラスが読み込んだ外部コールバックを適用する
     */
    public void apply( CallbackTable dest )
    {
        final Callback[] list = callbacks;

        for( Callback v : list )
        {
            if( v instanceof CallbackWithArgs )
            {
                dest.addWithArgs( (CallbackWithArgs)v );
            }
            else
            {
                dest.add( v );
            }
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
                int[] typeList = null;

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
                uiTypes.add( ui );
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
                String name                 = Variable.toKSPTypeCharacter( v.type ) + data[ 1 ];
                boolean availableOnInit     = "Y".equals( data[ 2 ] );

                v.name              = name;
                v.availableOnInit   = availableOnInit;    // on init 内で使用可能な変数かどうか。一部のビルトイン定数ではそれを許可していない。
                v.reserved          = true;                   // 予約変数
                v.referenced        = true;                   // 予約変数につき、使用・未使用に関わらず参照済みマーク
                v.status            = VariableState.LOADED;   // 予約変数につき、値代入済みマーク
                variables.add( v );
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

                String[] data  = line.split( DELIMITER );
                int returnType = toVariableType( data[ 0 ] ).type;
                String name    = data[ 1 ];
                String availableCallbackScope = data[ 2 ];
                boolean hasParenthesis = false;

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
                    ast.symbol.name = name;
                    newItem = new Command( ast );

                    if( args.size() > 0 )
                    {
                        newItem.argList.addAll( args );
                    }
                    newItem.hasParenthesis          = hasParenthesis;
                    newItem.returnType              = returnType;
                    newItem.symbolType              = SymbolType.Command;
                    newItem.reserved                = true;
                    newItem.availableCallbackScope  = availableCallbackScope;
                    commands.add( newItem );
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
        ArrayList<Callback> newCallbacks = new ArrayList<Callback>();

        File f            = new File( BASE_DIR, "callbacks.txt" );
        BufferedReader br = new BufferedReader( new InputStreamReader( new FileInputStream( f ), "UTF-8" ) );
        try
        {
            String line;
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
                        a.name        = ""; // シンボル収集時にマージ
                        a.requireDeclarationOnInit = requireDeclarationOnInit;  // 引数の変数が on init で宣言した変数かどうか
                        a.reserved    = true;                   // 予約変数
                        a.referenced  = true;                   // 予約変数につき、使用・未使用に関わらず参照済みマーク
                        a.status      = VariableState.LOADED;   // 予約変数につき、値代入済みマーク
                        args.add( a );
                    }
                }
                //--------------------------------------------------------------------------
                // コールバックのAST、変数を生成
                //--------------------------------------------------------------------------
                {
                    Callback newItem;
                    ASTCallbackDeclaration ast = new ASTCallbackDeclaration( JJTCALLBACKDECLARATION );
                    ast.symbol.name = name;
                    if( args.size() > 0 )
                    {
                        ASTCallbackArgumentList astList = new ASTCallbackArgumentList( JJTCALLBACKARGUMENTLIST );
                        for( Argument a : args )
                        {
                            astList.args.add( a.name );
                        }
                        ast.jjtAddChild( astList, 0 );
                        newItem = new CallbackWithArgs( ast );
                    }
                    else
                    {
                        newItem = new Callback( ast );
                    }
                    newItem.symbolType     = SymbolType.Callback;
                    newItem.reserved       = true;
                    newItem.declared       = false;
                    newItem.allowDuplicate = dup;
                    newCallbacks.add( newItem );
                }

            } //~while( ( line = br.readLine() ) != null )
        }
        finally
        {
            try { br.close(); } catch( Throwable e ) {}
        }
        callbacks = newCallbacks.toArray( new Callback[0] );
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
            type = TYPE_ANY;
        }
        else if( t == "*[]" )
        {
            type = TYPE_ANY | TYPE_ATTR_ARRAY;
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
            for( UIType ui : this.uiTypes )
            {
                if( ui.name.equals( t ) )
                {
                    uiTypeInfo  = ui;
                    accessFlag |= ACCESS_ATTR_UI;
                    found = true;
                    break;
                }
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

        ret.name       = "tmp";
        ret.type       = type;
        ret.accessFlag = accessFlag;
        ret.uiTypeInfo = uiTypeInfo;

        return ret;
    }

    /**
     * 型識別文字から引数の値に変換する(コマンド引数で複数の型を扱う場合)
     */
    public CommandArgument toVariableTypeForArgument( String t )
    {
        t = t.intern();
        CommandArgument ret;
        ArrayList<Variable> args          = new ArrayList<Variable>();
        CommandArgument.CondType condType = CondType.None;

        String[] andCond = t.split( "&&" );
        String[] orCond  = t.split( "\\|\\|" );

        //--------------------------------------------------------------------------
        // A かつ B かつ .... n の場合
        //--------------------------------------------------------------------------
        if( andCond.length >= 2 )
        {
            condType = CondType.And;
            for( String i : andCond )
            {
                if( i.indexOf( "||" ) >= 0 )
                {
                    continue;
                }
                Variable v = toVariableType( i );
                args.add( v );
            }
        }
        //--------------------------------------------------------------------------
        // A または B または .... n の場合
        //--------------------------------------------------------------------------
        else if( orCond.length >= 2 )
        {
            condType = CondType.Or;
            for( String i : orCond )
            {
                if( i.indexOf( "&&" ) >= 0 )
                {
                    continue;
                }
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
            v.reserved                  = false;                    // KONTAKT内部のビルトインコマンドにつき、非予約変数
            v.referenced                = true;                     // KONTAKT内部のビルトインコマンドにつき、使用・未使用に関わらず参照済みマーク
            v.status                    = VariableState.LOADED;     // KONTAKT内部のビルトインコマンドにつき、値代入済みマーク
            if( v.uiTypeInfo != null )
            {
                v.uiTypeName = v.uiTypeInfo.name;
            }
        }

        ret = new CommandArgument( args );
        ret.condType = condType;
        return ret;
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
