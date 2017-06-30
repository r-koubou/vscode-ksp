/* =========================================================================

    SemanticAnalyzer.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.util.ArrayList;

import net.rkoubou.kspparser.analyzer.MessageManager.Level;
import net.rkoubou.kspparser.analyzer.SymbolDefinition.SymbolType;
import net.rkoubou.kspparser.javacc.generated.ASTAnd;
import net.rkoubou.kspparser.javacc.generated.ASTAssignment;
import net.rkoubou.kspparser.javacc.generated.ASTCallCommand;
import net.rkoubou.kspparser.javacc.generated.ASTCallbackDeclaration;
import net.rkoubou.kspparser.javacc.generated.ASTCommandArgumentList;
import net.rkoubou.kspparser.javacc.generated.ASTConditionalAnd;
import net.rkoubou.kspparser.javacc.generated.ASTConditionalOr;
import net.rkoubou.kspparser.javacc.generated.ASTEqual;
import net.rkoubou.kspparser.javacc.generated.ASTInclusiveOr;
import net.rkoubou.kspparser.javacc.generated.ASTLiteral;
import net.rkoubou.kspparser.javacc.generated.ASTPreProcessorDefine;
import net.rkoubou.kspparser.javacc.generated.ASTPreProcessorUnDefine;
import net.rkoubou.kspparser.javacc.generated.ASTRefVariable;
import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclaration;
import net.rkoubou.kspparser.javacc.generated.Node;
import net.rkoubou.kspparser.javacc.generated.SimpleNode;

/**
 * 意味解析実行クラス
 */
public class SemanticAnalyzer extends AbstractAnalyzer
{

    // シンボルテーブル保持インスタンス
    public final UITypeTable uiTypeTable;
    public final VariableTable variableTable;
    public final CallbackTable callbackTable;
    public final CommandTable commandTable;
    public final UserFunctionTable userFunctionTable;
    public final PreProcessorSymbolTable preProcessorSymbolTable;

    /**
     * ctor
     */
    public SemanticAnalyzer( SymbolCollector symbolCollector )
    {
        super( symbolCollector.astRootNode );
        this.uiTypeTable                = symbolCollector.uiTypeTable;
        this.variableTable              = symbolCollector.variableTable;
        this.callbackTable              = symbolCollector.callbackTable;
        this.commandTable               = symbolCollector.commandTable;
        this.userFunctionTable          = symbolCollector.userFunctionTable;
        this.preProcessorSymbolTable    = symbolCollector.preProcessorSymbolTable;
    }

    /**
     * 意味解析の実行
     */
    @Override
    public void analyze() throws Exception
    {
        astRootNode.jjtAccept( this, null );
    }

//--------------------------------------------------------------------------
// ユーティリティ
//--------------------------------------------------------------------------

    /**
     * 現在のパース中のコールバックを取得する
     */
    protected ASTCallbackDeclaration getCurrentCallBack( Node child )
    {
        ASTCallbackDeclaration ret = null;
        while( true )
        {
            Node p = child.jjtGetParent();
            if( p == null )
            {
                return null;
            }
            if( p.getId() == JJTCALLBACKDECLARATION )
            {
                ret = (ASTCallbackDeclaration)p;
                break;
            }
            child = p;
        }
        return ret;
    }

//--------------------------------------------------------------------------
// 変数宣言
//--------------------------------------------------------------------------

    /**
     * 変数宣言
     */
    @Override
    public Object visit( ASTVariableDeclaration node, Object data)
    {
        final Object ret        = defaultVisit( node, data );
        final Variable variable = variableTable.search( node.symbol.name );
        return ret;
    }

//--------------------------------------------------------------------------
// 式
//--------------------------------------------------------------------------

    /**
     * 代入式
     */
    @Override
    public Object visit( ASTAssignment node, Object data )
    {
        final Object ret        = defaultVisit( node, data );
        final Variable variable = variableTable.search( node.symbol.name );
        return ret;
    }

    /**
     * 条件式 OR
     */
    @Override
    public Object visit( ASTConditionalOr node, Object data )
    {
/*
                 or
                 +
                 |
            +----+----+
            |         |
        0: <expr>   1:<expr>
*/
        final SimpleNode cond0      = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
        final SimpleNode cond1      = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, data );;
        final SymbolDefinition sym0 = cond0.symbol;
        final SymbolDefinition sym1 = cond1.symbol;

        if( !Variable.isBoolean( sym0.type ) || !Variable.isBoolean( sym1.type ) )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_COND_BOOLEAN, node.symbol );
            AnalyzeErrorCounter.e();
        }

        return node;
    }

    /**
     * 条件式 AND
     */
    @Override
    public Object visit( ASTConditionalAnd node, Object data )
    {
/*
                and
                 +
                 |
            +----+----+
            |         |
        0: <expr>   1:<expr>
*/
        final SimpleNode cond0      = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
        final SimpleNode cond1      = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, data );;
        final SymbolDefinition sym0 = cond0.symbol;
        final SymbolDefinition sym1 = cond1.symbol;

        if( !Variable.isBoolean( sym0.type ) || !Variable.isBoolean( sym1.type ) )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_COND_BOOLEAN, node.symbol );
            AnalyzeErrorCounter.e();
        }

        return node;
    }

    /**
     * 論理積
     */
    @Override
    public Object visit( ASTInclusiveOr node, Object data )
    {
/*
                .or.
                 +
                 |
            +----+----+
            |         |
        0: <expr>   1:<expr>
*/
        final SimpleNode exprL      = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
        final SimpleNode exprR      = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, data );
        final SymbolDefinition symL = exprL.symbol;
        final SymbolDefinition symR = exprR.symbol;

        if( symL.type != TYPE_INT || symR.type != TYPE_INT )
        {
            System.out.println( "| not match type. " + Variable.getTypeName( symL.name ) + ", " + Variable.getTypeName( symL.name ) );
            AnalyzeErrorCounter.e();
        }

        System.out.println( exprL );
        System.out.println( exprR );

        return node;
    }

    /**
     * 論理和
     */
    @Override
    public Object visit( ASTAnd node, Object data )
    {
/*
               .and.
                 +
                 |
            +----+----+
            |         |
        0: <expr>   1:<expr>
*/
        final SimpleNode exprL      = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
        final SimpleNode exprR      = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, data );
        final SymbolDefinition symL = exprL.symbol;
        final SymbolDefinition symR = exprR.symbol;

        if( symL.type != TYPE_INT || symR.type != TYPE_INT )
        {
            System.out.println( "| not match type. " + Variable.getTypeName( symL.name ) + ", " + Variable.getTypeName( symL.name ) );
            AnalyzeErrorCounter.e();
        }

        System.out.println( exprL );
        System.out.println( exprR );

        return exprL;
    }

    /**
     * 比較 (=)
     */
    @Override
    public Object visit( ASTEqual node, Object data )
    {
/*
                 =
                 +
                 |
            +----+----+
            |         |
        0: <expr>   1:<expr>
*/
        final SimpleNode exprL      = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
        final SimpleNode exprR      = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, data );
        final SymbolDefinition symL = exprL.symbol;
        final SymbolDefinition symR = exprR.symbol;

        // 上位ノードの型評価式用
        SimpleNode ret = new SimpleNode( JJTLITERAL );
        SymbolDefinition.copy( node.symbol, ret.symbol );

        if( !Variable.isNumeral( symL.type ) || !Variable.isNumeral( symR.type ) )
        {
            System.out.println( "= not match type. " + symL.type + ", " + symR.type );
            AnalyzeErrorCounter.e();
            ret.symbol.type = TYPE_VOID;
            ret.symbol.name = Variable.toKSPTypeCharacter( TYPE_VOID );
        }
        else
        {
            ret.symbol.name = Variable.toKSPTypeCharacter( TYPE_BOOL );
            ret.symbol.type = TYPE_BOOL;
        }
        return ret;
    }


    /**
     * リテラル定数参照
     * @return node自身
     */
    @Override
    public Object visit( ASTLiteral node, Object data )
    {
        return node;
    }

    /**
     * 変数参照
     * @return node自身
     */
    @Override
    public Object visit( ASTRefVariable node, Object data )
    {
        //--------------------------------------------------------------------------
        // 宣言済みかどうか
        //--------------------------------------------------------------------------
        Variable v = variableTable.search( node.symbol.name );
        if( v == null )
        {
            // 宣言されていない変数
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_NOT_DECLARED, node.symbol );
            AnalyzeErrorCounter.e();
            return node;
        }
        // 初期化（１度も値が格納されていない）
        if( v.status == VariableState.UNLOADED )
        {
            MessageManager.printlnW( MessageManager.PROPERTY_WARNING_SEMANTIC_VARIABLE_INIT, node.symbol );
            AnalyzeErrorCounter.w();
        }
        return node;
    }

//--------------------------------------------------------------------------
// コマンドコール
//--------------------------------------------------------------------------

    /**
     * コマンド呼び出し
     * @return node 自身
     */
    @Override
    public Object visit( ASTCallCommand node, Object data )
    {
        final Command cmd = commandTable.search( node.symbol.name );

        if( cmd == null )
        {
            // ドキュメントに記載のない隠しコマンドの可能性
            // エラーにせず、警告に留める
            MessageManager.printlnW( MessageManager.PROPERTY_WARNING_SEMANTIC_COMMAND_UNKNOWN, node.symbol );
            AnalyzeErrorCounter.w();
            return node;
        }

        ASTCallbackDeclaration callback = getCurrentCallBack( node );

        //--------------------------------------------------------------------------
        // 実行が許可されているコールバック内での呼び出しかどうか
        //--------------------------------------------------------------------------
        {
            if( !cmd.availableCallbackList.containsKey( callback.symbol.name ) )
            {
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_COMMAND_NOT_ALLOWED, node.symbol );
                AnalyzeErrorCounter.e();
                return node;
            }
        }

        //--------------------------------------------------------------------------
        // 引数の数チェック
        //--------------------------------------------------------------------------
        if( node.jjtGetNumChildren() > 0 )
        {
            if( node.jjtGetChild( 0 ).jjtGetNumChildren() != cmd.argList.size() )
            {
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_COMMAND_ARGCOUNT, node.symbol );
                AnalyzeErrorCounter.e();
                return node;
            }
        }

        // 引数の解析
        node.childrenAccept( this, cmd );

        return node;
    }

    /**
     * コマンド引数
     * @param data 呼び出し元 Command インスタンス
     * @return null
     */
    @Override
    public Object visit( ASTCommandArgumentList node, Object data )
    {
        final int childrenNum = node.jjtGetNumChildren();

        Object ret    = null;
        Command cmd   = null;

        if( !( data instanceof Command ) )
        {
            // ここに到達する時点で意味解析自体のバグ
            return null;
        }

        cmd = (Command)data;
        ArrayList<CommandArgument> argList = cmd.argList;

        //--------------------------------------------------------------------------
        // 引数の型チェック
        //--------------------------------------------------------------------------
        {
            for( int i = 0; i < childrenNum; i++ )
            {
                Object evalValue  = node.jjtGetChild( i ).jjtAccept( this, null );    // 子ノードの評価式結果
                if( evalValue == null )
                {
                    return false;
                }
                boolean valid           = false;
                SimpleNode evalNode     = (SimpleNode)evalValue;
                SymbolDefinition symbol = evalNode.symbol;
                int type                = symbol.type;

                // 評価式が変数だった場合のための参照
                Variable userVar        = variableTable.search( symbol.name );

                // 引数毎に複数のデータ型が許容される仕様のため照合
                //System.out.println("-------- : " + argList.size() + "/" + childrenNum );
                for( Argument arg : argList.get( i ).arguments )
                {
                    //System.out.println( "#ARG: " + arg.type );
                    //--------------------------------------------------------------------------
                    // コマンドがUI属性付き変数を要求
                    //--------------------------------------------------------------------------
                    if( userVar != null && ( arg.accessFlag & ACCESS_ATTR_UI ) != 0 )
                    {
                        if( userVar.uiTypeInfo == null )
                        {
                            // ui_##### 修飾子が無い変数
                            //System.out.println( "Not UI type" + userVar.name );
                            break;
                        }
                        if( arg.uiTypeName.equals( userVar.uiTypeInfo.name ) )
                        {
                            // 要求されている ui_#### と変数宣言時の ui_#### 修飾子が一致
                            //System.out.println( "UI OK" );
                            valid = true;
                            break;
                        }
                    }
                    //--------------------------------------------------------------------------
                    // ui_#### 修飾子が無い変数
                    //--------------------------------------------------------------------------
                    else if( userVar != null )
                    {
                        if( arg.type == userVar.type )
                        {
                            valid = true;
                            break;
                        }
                    }
                    //--------------------------------------------------------------------------
                    // コマンドの戻り値
                    //--------------------------------------------------------------------------
                    else if( evalNode.getId() == JJTCALLCOMMAND )
                    {
                        ASTCallCommand callCmd = (ASTCallCommand)evalNode;
                        Command retCommand = commandTable.search( callCmd.symbol.name );

                        // [nullチェック]
                        // 隠しコマンドやドキュメント化されていない場合に逆引きできない可能性があるため
                        if( retCommand != null )
                        {
                            for( int t : retCommand.returnType.typeList )
                            {
                                // 呼び出したコマンドの戻り値と
                                // このコマンドの求められている引数の型チェック
                                if( t == arg.type )
                                {
                                    valid = true;
                                    break;
                                }
                            }
                        }
                        else
                        {
                            // 未知のコマンドなので正しいかどうかの判定が不可能
                            // エラーにせずに警告に留める
                            MessageManager.printlnW( MessageManager.PROPERTY_WARNING_SEMANTIC_COMMAND_UNKNOWN, callCmd.symbol );
                            AnalyzeErrorCounter.w();
                            valid = true;
                            break;
                        }
                    }
                    //--------------------------------------------------------------------------
                    // リテラル
                    //--------------------------------------------------------------------------
                    else
                    {
                        if( arg.type == type )
                        {
                            valid = true;
                            break;
                        }
                    }
                } // for( Argument arg : a.arguments )

                if( !valid )
                {
                    MessageManager.printlnE(
                        MessageManager.PROPERTY_ERROR_SEMANTIC_INCOMPATIBLE_ARG,
                        node.symbol
                    );
                    AnalyzeErrorCounter.e();
                }

            } //~for( int i = 0; i < childrenNum; i++ )
        }
        return null;
    }

//--------------------------------------------------------------------------
// プリプロセッサ
//--------------------------------------------------------------------------

    /**
     * プリプロセッサシンボル定義
     */
    @Override
    public Object visit( ASTPreProcessorDefine node, Object data )
    {
        Object ret = defaultVisit( node, data );
        // プリプロセッサなので、既に宣言済みなら上書きもせずそのまま。
        // 複数回宣言可能な KONTAKT 側の挙動に合わせる形をとった。
        {
            ASTPreProcessorDefine decl = new ASTPreProcessorDefine( JJTPREPROCESSORDEFINE );
            SymbolDefinition.copy( node.symbol,  decl.symbol );
            decl.symbol.symbolType = SymbolType.PreprocessorSymbol;

            PreProcessorSymbol v = new PreProcessorSymbol( decl );
            preProcessorSymbolTable.add( v );
        }
        return ret;
    }

    /**
     * プリプロセッサシンボル破棄
     */
    @Override
    public Object visit( ASTPreProcessorUnDefine node, Object data )
    {
        Object ret = defaultVisit( node, data );
        // 宣言されていないシンボルを undef しようとした場合
        if( preProcessorSymbolTable.search( node.symbol.name ) == null )
        {
            MessageManager.printlnW( MessageManager.PROPERTY_WARN_PREPROCESSOR_UNKNOWN_DEF, node.symbol );
            AnalyzeErrorCounter.w();
        }
        return ret;
    }

}
