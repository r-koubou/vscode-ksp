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
    private UIType[] uiTypes = new UIType[ 0 ];

    /** 予約済み変数 */
    private Variable[] variables = new Variable[ 0 ];

    /** 予約済みコマンド */
    private Command[] commands = new Command[ 0 ];

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
        loadCommands();
        loadCallbacks();
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
        final UIType[] list = uiTypes;
        for( UIType v : list )
        {
            dest.add( v );
        }
    }

    /**
     * 指定された変数テーブルにこのクラスが読み込んだ外部変数を適用する
     */
    public void apply( VariableTable dest )
    {
        final Variable[] list = variables;
        for( Variable v : list )
        {
            dest.add( v );
        }
    }

    /**
     * 指定されたコマンドテーブルにこのクラスが読み込んだ外部コールバックを適用する
     */
    public void apply( CommandTable dest )
    {
        final Command[] list = commands;

        for( Command v : list )
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
        ArrayList<UIType> newUITypes = new ArrayList<UIType>( 1024 );

        File f            = new File( BASE_DIR, "uitypes.txt" );
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
                String name                 = data[ 0 ].trim();
                boolean constant            = "Y".equals( data[ 1 ].trim() );
                boolean initializerRequired = "Y".equals( data[ 2 ].trim() );
                int type                    = toVariableType( data[ 3 ].trim() );
                int[] typeList = null;

                //--------------------------------------------------------------------------
                // 初期値代入式が必須の場合
                //--------------------------------------------------------------------------
                if( data.length >= 5 )
                {
                    typeList = new int[ data.length - 4 ];
                    for( int i = 4, x = 0; i < data.length; i++, x++ )
                    {
                        typeList[ x ] = toVariableType( data[ i ].trim() );
                    }
                }

                UIType ui = new UIType( name, true, type, constant, initializerRequired, typeList );
                newUITypes.add( ui );
            }
        }
        finally
        {
            try { br.close(); } catch( Throwable e ) {}
        }
        uiTypes = newUITypes.toArray( new UIType[0] );
    }


    /**
     * 変数の予約済み定義ファイルから Variable クラスインスタンスを生成する
     */
    private void loadVariables() throws IOException
    {
        ArrayList<Variable> newVariables = new ArrayList<Variable>( 1024 );

        File f            = new File( BASE_DIR, "variables.txt" );
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

                String[] data               = line.split( DELIMITER );
                int type                    = toVariableType( data[ 0 ].trim() );
                String name                 = Variable.toKSPTypeCharacter( type ) + data[ 1 ].trim();
                boolean availableOnInit     = "Y".equals( data[ 2 ].trim() );

                ASTVariableDeclaration ast  = new ASTVariableDeclaration( JJTVARIABLEDECLARATION );
                ast.symbol.name = name;

                Variable v    = new Variable( ast );
                v.type        = type;
                v.availableOnInit = availableOnInit;    // on init 内で使用可能な変数かどうか。一部のビルトイン定数ではそれを許可していない。
                v.reserved    = true;                   // 予約変数
                v.referenced  = true;                   // 予約変数につき、使用・未使用に関わらず参照済みマーク
                v.status      = VariableState.LOADED;   // 予約変数につき、値代入済みマーク
                newVariables.add( v );
            }
        }
        finally
        {
            try { br.close(); } catch( Throwable e ) {}
        }
        variables = newVariables.toArray( new Variable[0] );
    }

    /**
     * コールバックの予約済み定義ファイルから Variable クラスインスタンスを生成する
     */
    private void loadCommands() throws IOException
    {
        ArrayList<Command> newCommands = new ArrayList<Command>( 256 );

        File f            = new File( BASE_DIR, "commands.txt" );
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

                String[] data  = line.split( DELIMITER );
                int returnType = toVariableType( data[ 0 ].trim() );
                String name    = data[ 1 ].trim();
                boolean hasParenthesis = false;

                //--------------------------------------------------------------------------
                // data[2] 以降：引数を含む場合
                // 引数のAST、変数を生成
                //--------------------------------------------------------------------------
                ArrayList<Argument> args = new ArrayList<Argument>();
                if( data.length >= 3 )
                {
                    hasParenthesis = true;
                    final int len = data.length;
                    for( int i = 2; i < len; i++ )
                    {
                        String typeString = data[ i ];

                        int type = toVariableType( typeString );

                        ASTVariableDeclaration ast = new ASTVariableDeclaration( JJTVARIABLEDECLARATION );
                        ast.symbol.name = "arg"; // 使用・参照しないのでダミー

                        Argument v    = new Argument( ast );
                        v.requireDeclarationOnInit = true;      // 引数の変数が on init で宣言した変数かどうか
                        v.type        = type;
                        v.reserved    = false;                  // KONTAKT内部のビルトインコマンドにつき、非予約変数
                        v.referenced  = true;                   // KONTAKT内部のビルトインコマンドにつき、使用・未使用に関わらず参照済みマーク
                        v.status      = VariableState.LOADED;   // KONTAKT内部のビルトインコマンドにつき、値代入済みマーク
                        if( typeString.startsWith( "@" ) )
                        {
                            v.accessFlag = ACCESS_ATTR_CONST;
                        }
                        args.add( v );
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
                    newItem.hasParenthesis = hasParenthesis;
                    newItem.returnType     = returnType;
                    newItem.symbolType     = SymbolType.Command;
                    newItem.reserved       = true;
                    newCommands.add( newItem );
                }

            } //~while( ( line = br.readLine() ) != null )
        }
        finally
        {
            try { br.close(); } catch( Throwable e ) {}
        }
        commands = newCommands.toArray( new Command[0] );
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
                String name   = data[ 0 ].trim();
                boolean dup   = data[ 1 ].trim().equals( "Y" );

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

                        int type      = toVariableType( typeString );

                        ASTVariableDeclaration ast = new ASTVariableDeclaration( JJTVARIABLEDECLARATION );
                        ast.symbol.name = ""; // シンボル収集時にマージ

                        Argument v    = new Argument( ast );
                        v.requireDeclarationOnInit = requireDeclarationOnInit;  // 引数の変数が on init で宣言した変数かどうか
                        v.type        = type;
                        v.reserved    = true;                   // 予約変数
                        v.referenced  = true;                   // 予約変数につき、使用・未使用に関わらず参照済みマーク
                        v.status      = VariableState.LOADED;   // 予約変数につき、値代入済みマーク
                        args.add( v );
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
    static public int toVariableType( String t )
    {
        t = t.intern();
        if( t == "*" )
        {
            return TYPE_ANY;
        }
        if( t == "V" )
        {
            return TYPE_VOID;
        }
        if( t == "I" )
        {
            return TYPE_INT;
        }
        if( t == "@I" )
        {
            return TYPE_INT;
        }
        if( t == "I[]" )
        {
            return TYPE_INT | TYPE_ATTR_ARRAY;
        }
        if( t == "R" )
        {
            return TYPE_REAL;
        }
        if( t == "@R" )
        {
            return TYPE_REAL;
        }
        if( t == "R[]" )
        {
            return TYPE_REAL | TYPE_ATTR_ARRAY;
        }
        if( t == "S" )
        {
            return TYPE_STRING;
        }
        if( t == "@S" )
        {
            return TYPE_STRING;
        }
        if( t == "S[]" )
        {
            return TYPE_STRING | TYPE_ATTR_ARRAY;
        }
        if( t == "PP" )
        {
            return TYPE_PREPROCESSOR_SYMBOL;
        }
        throw new IllegalArgumentException( "Unknown type : " + t );
    }

    /**
     * Unit test
     */
    static public void main( String[] args ) throws Throwable
    {
        // command: java -classpath ./target/classes/ net.rkoubou.kspparser.analyzer.ReservedSymbolManager

        ReservedSymbolManager mgr = ReservedSymbolManager.getManager();
        mgr.load();
        // for( Variable v : mgr.variables )
        // {
        //     System.out.println( v.name );
        //     //System.out.println( v.toKSPTypeCharacter() );
        // }
    }
}
