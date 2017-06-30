/* =========================================================================

    SemanticAnalyzer.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.util.ArrayList;

import net.rkoubou.kspparser.analyzer.SymbolDefinition.SymbolType;
import net.rkoubou.kspparser.javacc.generated.ASTAdd;
import net.rkoubou.kspparser.javacc.generated.ASTAnd;
import net.rkoubou.kspparser.javacc.generated.ASTAssignment;
import net.rkoubou.kspparser.javacc.generated.ASTCallCommand;
import net.rkoubou.kspparser.javacc.generated.ASTCallbackDeclaration;
import net.rkoubou.kspparser.javacc.generated.ASTCommandArgumentList;
import net.rkoubou.kspparser.javacc.generated.ASTConditionalAnd;
import net.rkoubou.kspparser.javacc.generated.ASTConditionalOr;
import net.rkoubou.kspparser.javacc.generated.ASTDiv;
import net.rkoubou.kspparser.javacc.generated.ASTEqual;
import net.rkoubou.kspparser.javacc.generated.ASTGE;
import net.rkoubou.kspparser.javacc.generated.ASTGT;
import net.rkoubou.kspparser.javacc.generated.ASTInclusiveOr;
import net.rkoubou.kspparser.javacc.generated.ASTLE;
import net.rkoubou.kspparser.javacc.generated.ASTLT;
import net.rkoubou.kspparser.javacc.generated.ASTLiteral;
import net.rkoubou.kspparser.javacc.generated.ASTMod;
import net.rkoubou.kspparser.javacc.generated.ASTMul;
import net.rkoubou.kspparser.javacc.generated.ASTNotEqual;
import net.rkoubou.kspparser.javacc.generated.ASTPreProcessorDefine;
import net.rkoubou.kspparser.javacc.generated.ASTPreProcessorUnDefine;
import net.rkoubou.kspparser.javacc.generated.ASTRefVariable;
import net.rkoubou.kspparser.javacc.generated.ASTStrAdd;
import net.rkoubou.kspparser.javacc.generated.ASTSub;
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
     * 二項演算子の評価
     * @param node 演算子ノード
     * @param intOnly 評価可能なのは整数型のみかどうか（falseの場合は浮動小数も対象）
     * @param booleanOp 演算子はブール演算子かどうか
     * @param jjtAcceptData jjtAcceptメソッドのdata引数
     * @return データ型を格納した評価結果。エラー時は TYPE_VOID が格納される
     */
    public SimpleNode evalbinaryOperator( SimpleNode node, boolean intOnly, boolean booleanOp, Object jjtAcceptData )
    {
/*
             <operator>
                 +
                 |
            +----+----+
            |         |
        0: <expr>   1:<expr>
*/

        final SimpleNode exprL      = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, jjtAcceptData );
        final SimpleNode exprR      = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, jjtAcceptData );
        final SymbolDefinition symL = exprL.symbol;
        final SymbolDefinition symR = exprR.symbol;
        int typeL = intOnly ? TYPE_INT : symL.type;
        int typeR = intOnly ? TYPE_INT : symR.type;

        // 上位ノードの型評価式用
        SimpleNode ret = new SimpleNode( JJTLITERAL );
        SymbolDefinition.copy( node.symbol, ret.symbol );

        // 数値型かつ左辺と右辺の型が一致している必要がある
        if( ( !Variable.isNumeral( typeL ) || !Variable.isNumeral( typeR ) ) || typeL != typeR )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_BINOPR_DIFFERENT, node.symbol );
            AnalyzeErrorCounter.e();
            ret.symbol.type = TYPE_VOID;
            ret.symbol.name = Variable.toKSPTypeCharacter( TYPE_VOID );
        }
        else
        {
            int t = booleanOp ? TYPE_BOOL : typeL;
            ret.symbol.name = Variable.toKSPTypeCharacter( t );
            ret.symbol.type = t;
        }

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
        final SimpleNode cond1      = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, data );
        final SymbolDefinition sym0 = cond0.symbol;
        final SymbolDefinition sym1 = cond1.symbol;

        // 上位ノードの型評価式用
        SimpleNode ret = new SimpleNode( JJTLITERAL );
        SymbolDefinition.copy( node.symbol, ret.symbol );

        if( !Variable.isBoolean( sym0.type ) || !Variable.isBoolean( sym1.type ) )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_COND_BOOLEAN, node.symbol );
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
        final SimpleNode cond1      = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, data );
        final SymbolDefinition sym0 = cond0.symbol;
        final SymbolDefinition sym1 = cond1.symbol;

        // 上位ノードの型評価式用
        SimpleNode ret = new SimpleNode( JJTLITERAL );
        SymbolDefinition.copy( node.symbol, ret.symbol );

        if( !Variable.isBoolean( sym0.type ) || !Variable.isBoolean( sym1.type ) )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_COND_BOOLEAN, node.symbol );
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
     * 論理積
     */
    @Override
    public Object visit( ASTInclusiveOr node, Object data )
    {
        return evalbinaryOperator( node, true, false, data );
    }

    /**
     * 論理和
     */
    @Override
    public Object visit( ASTAnd node, Object data )
    {
        return evalbinaryOperator( node, true, false, data );
    }

    /**
     * 比較 (=)
     */
    @Override
    public Object visit( ASTEqual node, Object data )
    {
        return evalbinaryOperator( node, false, true, data );
    }

    /**
     * 比較 (#)
     */
    @Override
    public Object visit( ASTNotEqual node, Object data )
    {
        return evalbinaryOperator( node, false, true, data );
    }

    /**
     * 不等号(<)
     */
    @Override
    public Object visit( ASTLT node, Object data )
    {
        return evalbinaryOperator( node, false, true, data );
    }

    /**
     * 不等号(>)
     */
    @Override
    public Object visit( ASTGT node, Object data )
    {
        return evalbinaryOperator( node, false, true, data );
    }

    /**
     * 不等号(<=)
     */
    @Override
    public Object visit( ASTLE node, Object data )
    {
        return evalbinaryOperator( node, false, true, data );
    }

    /**
     * 不等号(>=)
     */
    @Override
    public Object visit( ASTGE node, Object data )
    {
        return evalbinaryOperator( node, false, true, data );
    }

    /**
     * 加算(+)
     */
    @Override
    public Object visit( ASTAdd node, Object data )
    {
        return evalbinaryOperator( node, false, false, data );
    }

    /**
     * 減算(-)
     */
    @Override
    public Object visit( ASTSub node, Object data )
    {
        return evalbinaryOperator( node, false, false, data );
    }

    /**
     * 文字列連結
     */
    @Override
    public Object visit( ASTStrAdd node, Object data )
    {
        // 上位ノードの型評価式用
        SimpleNode ret = new SimpleNode( JJTLITERAL );
        SymbolDefinition.copy( node.symbol, ret.symbol );

        //--------------------------------------------------------------------------
        // ＊初期値代入式では使用できない
        //--------------------------------------------------------------------------
        {
            Node p = node.jjtGetParent();
            while( p != null )
            {
                if( p.getId() == JJTVARIABLEDECLARATOR )
                {
                    System.out.println( "&: Cannot use on variable initilizer" );
                    AnalyzeErrorCounter.e();
                    ret.symbol.type = TYPE_VOID;
                    ret.symbol.name = Variable.toKSPTypeCharacter( TYPE_VOID );
                    return ret;
                }
                p = p.jjtGetParent();
            }
        }

        final SimpleNode exprL      = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
        final SimpleNode exprR      = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, data );
        final SymbolDefinition symL = exprL.symbol;
        final SymbolDefinition symR = exprR.symbol;
        int typeL = symL.type;
        int typeR = symR.type;

        // 数値型かつ左辺と右辺の型が一致している必要がある
        if( ( !Variable.isString( typeL ) || !Variable.isString( typeR ) ) || typeL != typeR )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_BINOPR_DIFFERENT, node.symbol );
            AnalyzeErrorCounter.e();
            ret.symbol.type = TYPE_VOID;
            ret.symbol.name = Variable.toKSPTypeCharacter( TYPE_VOID );
        }
        else
        {
            ret.symbol.name = Variable.toKSPTypeCharacter( TYPE_STRING );
            ret.symbol.type = TYPE_STRING;
        }
        return ret;
    }

    /**
     * 乗算(*)
     */
    @Override
    public Object visit( ASTMul node, Object data )
    {
        return evalbinaryOperator( node, false, false, data );
    }

    /**
     * 除算(/)
     */
    @Override
    public Object visit( ASTDiv node, Object data )
    {
        return evalbinaryOperator( node, false, false, data );
    }

    /**
     * 余算(mod)
     */
    @Override
    public Object visit( ASTMod node, Object data )
    {
        return evalbinaryOperator( node, false, false, data );
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
