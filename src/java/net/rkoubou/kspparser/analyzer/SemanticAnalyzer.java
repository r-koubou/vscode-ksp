/* =========================================================================

    SemanticAnalyzer.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.util.ArrayList;

import net.rkoubou.kspparser.analyzer.SymbolDefinition.SymbolType;
import net.rkoubou.kspparser.javacc.generated.ASTAdd;
import net.rkoubou.kspparser.javacc.generated.ASTAnd;
import net.rkoubou.kspparser.javacc.generated.ASTArrayIndex;
import net.rkoubou.kspparser.javacc.generated.ASTArrayInitializer;
import net.rkoubou.kspparser.javacc.generated.ASTAssignment;
import net.rkoubou.kspparser.javacc.generated.ASTCallCommand;
import net.rkoubou.kspparser.javacc.generated.ASTCallUserFunctionStatement;
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
import net.rkoubou.kspparser.javacc.generated.ASTLogicalNot;
import net.rkoubou.kspparser.javacc.generated.ASTMod;
import net.rkoubou.kspparser.javacc.generated.ASTMul;
import net.rkoubou.kspparser.javacc.generated.ASTNeg;
import net.rkoubou.kspparser.javacc.generated.ASTNot;
import net.rkoubou.kspparser.javacc.generated.ASTNotEqual;
import net.rkoubou.kspparser.javacc.generated.ASTPreProcessorDefine;
import net.rkoubou.kspparser.javacc.generated.ASTPreProcessorUnDefine;
import net.rkoubou.kspparser.javacc.generated.ASTRefVariable;
import net.rkoubou.kspparser.javacc.generated.ASTSelectStatement;
import net.rkoubou.kspparser.javacc.generated.ASTStrAdd;
import net.rkoubou.kspparser.javacc.generated.ASTSub;
import net.rkoubou.kspparser.javacc.generated.ASTUserFunctionDeclaration;
import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclaration;
import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclarator;
import net.rkoubou.kspparser.javacc.generated.ASTVariableInitializer;
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
     * 与えられた式が条件ステートメント(if,while等)内で実行されているかどうかを判定する
     * BOOL演算子はこの状況下でしか使用出来ないKSP仕様
     */
    protected boolean isInConditionalStatement( Node expr )
    {
        Node p = expr.jjtGetParent();
        while( p != null )
        {
            if( p == null )
            {
                return false;
            }
            switch( p.getId() )
            {
                case JJTIFSTATEMENT:
                case JJTWHILESTATEMENT:
                case JJTSELECTSTATEMENT:
                    return true;
            }
            p = p.jjtGetParent();
        }
        return false;
    }

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

    /**
     * 現在のパース中のユーザー定義関数を取得する
     */
    protected ASTUserFunctionDeclaration getCurrentUserFunction( Node child )
    {
        ASTUserFunctionDeclaration ret = null;
        while( true )
        {
            Node p = child.jjtGetParent();
            if( p == null )
            {
                return null;
            }
            if( p.getId() == JJTUSERFUNCTIONDECLARATION )
            {
                ret = (ASTUserFunctionDeclaration)p;
                break;
            }
            child = p;
        }
        return ret;
    }

    /**
     * 与えられた式ノードから定数値を算出する（畳み込み）
     * 定数値が含まれていない場合はその時点で処理を終了、nullを返す。
     * @param calc 低数値カウント時の再帰処理用。最初のノード時のみ 0 を渡す
     */
    protected Integer evalConstantIntValue( SimpleNode expr, int calc )
    {
        int ret = 0;

        //--------------------------------------------------------------------------
        // リテラル・変数
        //--------------------------------------------------------------------------
        if( expr.jjtGetNumChildren() == 0 )
        {
            switch( expr.getId() )
            {
                case JJTLITERAL:
                {
                    return (Integer)expr.jjtGetValue();
                }
                case JJTREFVARIABLE:
                {
                    Variable v = variableTable.search( expr.symbol.name );
                    if( v == null )
                    {
                        MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_NOT_DECLARED, expr.symbol );
                        AnalyzeErrorCounter.e();
                        return null;
                    }
                    else if( !v.isConstant() || !v.isInt() )
                    {
                        return null;
                    }
                    return (Integer)v.value;
                }
            }
        }
        //--------------------------------------------------------------------------
        // ２項演算子
        //--------------------------------------------------------------------------
        else if( expr.jjtGetNumChildren() == 2 )
        {
            SimpleNode exprL = (SimpleNode)expr.jjtGetChild( 0 );
            SimpleNode exprR = (SimpleNode)expr.jjtGetChild( 1 );
            Integer numL = evalConstantIntValue( exprL, calc );
            if( numL == null )
            {
                return null;
            }
            Integer numR = evalConstantIntValue( exprR, numL );
            if( numR == null )
            {
                return null;
            }
            switch( expr.getId() )
            {
                case JJTADD:            return numL + numR;
                case JJTSUB:            return numL - numR;
                case JJTMUL:            return numL * numR;
                case JJTDIV:            return numL / numR;
                case JJTMOD:            return numL % numR;
                case JJTINCLUSIVEOR:    return numL | numR;
                case JJTAND:            return numL & numR;
                default:
                    return null;
            }
        }
        //--------------------------------------------------------------------------
        // 単項演算子
        //--------------------------------------------------------------------------
        else if( expr.jjtGetNumChildren() == 1 )
        {
            SimpleNode exprL = (SimpleNode)expr.jjtGetChild( 0 );
            Integer numL = evalConstantIntValue( exprL, calc );
            if( numL == null )
            {
                return null;
            }
            switch( expr.getId() )
            {
                case JJTNEG:            return -numL;
                case JJTNOT:            return ~numL;
                case JJTLOGICALNOT:     return numL != 0 ? 0 : 1; // 0=false, 1=true としている
                default:
                    return null;
            }

        }

        return ret;
    }

    /**
     * 与えられた式ノードから定数値を算出する（畳み込み）
     * 定数値が含まれていない場合はその時点で処理を終了、nullを返す。
     * @param calc 低数値カウント時の再帰処理用。最初のノード時のみ 0 を渡す
     */
    protected Double evalConstantRealValue( SimpleNode expr, double calc )
    {
        double ret = 0;

        //--------------------------------------------------------------------------
        // リテラル・変数
        //--------------------------------------------------------------------------
        if( expr.jjtGetNumChildren() == 0 )
        {
            switch( expr.getId() )
            {
                case JJTLITERAL:
                {
                    return (Double)expr.jjtGetValue();
                }
                case JJTREFVARIABLE:
                {
                    Variable v = variableTable.search( expr.symbol.name );
                    if( v == null )
                    {
                        MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_NOT_DECLARED, expr.symbol );
                        AnalyzeErrorCounter.e();
                        return null;
                    }
                    else if( !v.isConstant() || !v.isReal() )
                    {
                        return null;
                    }
                    return (Double)v.value;
                }
            }
        }
        //--------------------------------------------------------------------------
        // ２項演算子
        //--------------------------------------------------------------------------
        else if( expr.jjtGetNumChildren() == 2 )
        {
            SimpleNode exprL = (SimpleNode)expr.jjtGetChild( 0 );
            SimpleNode exprR = (SimpleNode)expr.jjtGetChild( 1 );
            Double numL = evalConstantRealValue( exprL, calc );
            if( numL == null )
            {
                return null;
            }
            Double numR = evalConstantRealValue( exprR, numL );
            if( numR == null )
            {
                return null;
            }
            switch( expr.getId() )
            {
                case JJTADD:            return numL + numR;
                case JJTSUB:            return numL - numR;
                case JJTMUL:            return numL * numR;
                case JJTDIV:            return numL / numR;
                case JJTMOD:            return numL % numR;
                default:
                    return null;
            }
        }
        //--------------------------------------------------------------------------
        // 単項演算子
        //--------------------------------------------------------------------------
        else if( expr.jjtGetNumChildren() == 1 )
        {
            SimpleNode exprL = (SimpleNode)expr.jjtGetChild( 0 );
            Double numL = evalConstantRealValue( exprL, calc );
            if( numL == null )
            {
                return null;
            }
            switch( expr.getId() )
            {
                case JJTNEG:            return -numL;
                default:
                    return null;
            }
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
/*
        VariableDeclaration
            -> VariableDeclarator [arrayindex] -> <expr>
                -> [###Initializer]
                    -> (expr)+
*/
        return node.jjtGetChild( 0 ).jjtAccept( this, variableTable.search( node.symbol.name ) );
    }

    /**
     * 変数宣言(変数名、初期値代入)
     * @param data Variableインスタンス
     */
    @Override
    public Object visit( ASTVariableDeclarator node, Object data )
    {
/*
        VariableDeclarator [arrayindex] -> <expr>
            -> [###Initializer]
                -> (expr)+
*/
        final Object ret    = defaultVisit( node, data );
        final Variable v    = (Variable)data;
        boolean result      = false;

        if( v.isUIVariable() )
        {
            result = declareUIVariableImpl( node, v, data );
        }
        else if( v.isArray() )
        {
            result = declareArrayVariableImpl( node, v, data, false );
        }
        else if( node.jjtGetNumChildren() > 0 )
        {
            result = declarePrimitiveVariableImpl( node, v, data );
        }
        else
        {
            // 宣言のみ
            result = true;
        }

        if( result )
        {
            v.status = VariableState.INITIALIZED;
        }

        return ret;
    }

    /**
     * 配列型宣言の実装
     */
    protected boolean declareArrayVariableImpl( ASTVariableDeclarator node, Variable v, Object jjtVisitorData, boolean forceSkipInitializer )
    {
/*
            -> VariableDeclarator
                -> <arrayindex> -> <expr> : [0]
                    -> [ := VariableInitializer ] : [1]
                        -> ArrayInitializer : [1][0]
                            -> ( <expr> (, <expr>)* ) : [1][1]
*/

        //--------------------------------------------------------------------------
        // 型チェック(UI変数チェック経由でこのメソッドも呼び出される)
        //--------------------------------------------------------------------------
        if( !v.isArray() )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_NOTARRAY, v );
            AnalyzeErrorCounter.e();
            return false;
        }

        //--------------------------------------------------------------------------
        // 要素数宣言
        //--------------------------------------------------------------------------
        if( node.jjtGetNumChildren() == 0 || node.jjtGetChild( 0 ).getId() != JJTARRAYINDEX )
        {
            // 配列要素数の式がない
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_NOT_ARRAYSIZE, v );
            AnalyzeErrorCounter.e();
            return false;
        }

        final SimpleNode arraySizeNode = (SimpleNode)node.jjtGetChild( 0 );
        Integer size;

        for( int i = 0; i < arraySizeNode.jjtGetNumChildren(); i++ )
        {
            SimpleNode n = ((SimpleNode)arraySizeNode.jjtGetChild( i ));

            size = evalConstantIntValue( n, 0 );

            if( size == null || size <= 0 )
            {
                // 要素数が不明、または 0 以下
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_ARRAYSIZE, v );
                AnalyzeErrorCounter.e();
                return false;
            }

            v.arraySize = size;

        }

        //--------------------------------------------------------------------------
        // 初期値代入
        //--------------------------------------------------------------------------
        if( forceSkipInitializer || node.jjtGetChild( 0 ).jjtGetNumChildren() == 0 )
        {
            // 初期値代入なし
            // int なら 0 フィルなど初期値で埋まるので初期化したものとみなす
            v.status = VariableState.INITIALIZED;
            return false;
        }

        final ASTArrayInitializer initializer    = (ASTArrayInitializer)node.jjtGetChild( 1 ).jjtGetChild( 0 );
        for( int i = 0; i < initializer.jjtGetNumChildren(); i++ )
        {
            final SimpleNode expr       = (SimpleNode)initializer.jjtGetChild( i );
            final SymbolDefinition eval = (SymbolDefinition) expr.symbol;
            if( ( v.type & TYPE_MASK ) != ( eval.type & TYPE_MASK ) )
            {
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_ARRAYINITILIZER, v, ""+i );
                AnalyzeErrorCounter.e();
            }
        }
        v.status = VariableState.INITIALIZED;
        return true;
    }

    /**
     * UI型宣言の実装
     */
    protected boolean declareUIVariableImpl( ASTVariableDeclarator node, Variable v, Object jjtVisitorData )
    {
/*
            -> VariableDeclarator
                -> [ := UIInitializer ]
                    -> (expr)+
*/
        UIType uiType = uiTypeTable.search( v.uiTypeName );
        if( uiType == null )
        {
            // KSP（data/symbol/uitypes.txt）で未定義のUIタイプ
            // シンボル収集フェーズで警告出力済みなので何もしない
            v.status = VariableState.INITIALIZED;
            return false;
        }

        //--------------------------------------------------------------------------
        // ui_#### が求める型と変数の型のチェック
        //--------------------------------------------------------------------------
        if( v.type != uiType.uiValueType )
        {
            System.out.println( "UI : " + v.name + "@" + uiType.name + " require " + Variable.getTypeName( uiType.uiValueType ) );
            AnalyzeErrorCounter.e();
            return false;
        }

        //--------------------------------------------------------------------------
        // ui_#### が配列型の場合、要素数宣言のチェック
        //--------------------------------------------------------------------------
        if( Variable.isArray( uiType.uiValueType ) )
        {
            if( !declareArrayVariableImpl( node, v, jjtVisitorData, true ) )
            {
                return false;
            }
        }

        //--------------------------------------------------------------------------
        // 初期値代入式チェック
        //--------------------------------------------------------------------------
        if( !uiType.initializerRequired )
        {
            // 初期化不要
            v.status = VariableState.INITIALIZED;
            return false;
        }
        if( node.jjtGetNumChildren() == 0 )
        {
            System.out.println( "UI: require initializer" );
            AnalyzeErrorCounter.e();
            return false;
        }

        Node uiInitializer = node.jjtGetChild( 0 ).jjtGetChild( 0 );
        if( uiInitializer.getId() != JJTUIINITIALIZER )
        {
            System.out.println( v.name + " : Invalid Expression " + uiInitializer );
            return false;
        }
        if( uiInitializer.jjtGetNumChildren() != uiType.initilzerTypeList.length )
        {
            // 引数の数が一致していない
            System.out.println( "UI: not compatible arguments for initializer : " + uiInitializer.jjtGetNumChildren() + " / " + uiType.initilzerTypeList.length + " : " + uiType.name );
            AnalyzeErrorCounter.e();
            return false;
        }

        for( int i = 0; i < uiInitializer.jjtGetNumChildren(); i++ )
        {
            boolean found = false;
            SimpleNode n  = (SimpleNode)uiInitializer.jjtGetChild( i );
            SymbolDefinition param = n.symbol;
            int nid = n.getId();

            // 四則演算等は文法解析時でクリアしているので値だけに絞る
            if( nid != JJTLITERAL && nid != JJTREFVARIABLE )
            {
                continue;
            }

SEARCH:
            for( int t : uiType.initilzerTypeList )
            {
                switch( nid )
                {
                    //--------------------------------------------------------------------------
                    // リテラル
                    //--------------------------------------------------------------------------
                    case JJTLITERAL:
                    {
                        if( n.symbol.type == t )
                        {
                            found = true;
                            break SEARCH;
                        }
                    }
                    break;
                    //--------------------------------------------------------------------------
                    // const 指定ありの変数
                    //--------------------------------------------------------------------------
                    case JJTREFVARIABLE:
                    {
                        Variable var = variableTable.search( n.symbol.name );
                        if( var == null )
                        {
                            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_NOT_DECLARED, n.symbol );
                            AnalyzeErrorCounter.e();
                            break;
                        }
                        if( !var.isConstant() )
                        {
                            System.out.println( "Constant value only this expression" );
                            AnalyzeErrorCounter.e();
                        }
                        if( var.type == t )
                        {
                            found = true;
                            break SEARCH;
                        }
                        break;
                    }
                    //--------------------------------------------------------------------------
                    // 上記以外の式は無効
                    //--------------------------------------------------------------------------
                    default:
                    {
                        System.out.println( "Invalid Expression : " + n );
                        AnalyzeErrorCounter.e();
                    }
                    break;
                }
                if( !Variable.isConstant( param.accessFlag ) )
                {
                    System.out.println( "UI : Constant value only in ui initializer : " + v.name + " @ " + n );
                    AnalyzeErrorCounter.e();
                }
            } //~for( int t : uiType.initilzerTypeList )

            if( !found )
            {
                // イニシャライザ: 型の不一致
                System.out.println( "not compatible for initializer" );
                AnalyzeErrorCounter.e();
                break;
            }
        }
        return true;
    }

    /**
     * プリミティブ型宣言の実装
     */
    protected boolean declarePrimitiveVariableImpl( ASTVariableDeclarator node, Variable v, Object jjtVisitorData )
    {
/*
            -> VariableDeclarator
                -> [ := <VariableInitializer> ]
                    -> <expr>
*/
        if( node.jjtGetNumChildren() == 0 )
        {
            // 初期値代入なし
            return true;
        }

        if( node.jjtGetChild( 0 ).getId() != JJTVARIABLEINITIALIZER )
        {
            System.out.println( "invalid expression" );
            return false;
        }

        final ASTVariableInitializer initializer = (ASTVariableInitializer)node.jjtGetChild( 0 );
        final SimpleNode expr                    = (SimpleNode)initializer.jjtGetChild( 0 );
        final SymbolDefinition eval              = (SymbolDefinition) expr.symbol;

        // 型の不一致
        if( v.type != eval.type )
        {
            System.out.println( "Primitive variable : incompatible type" );
            AnalyzeErrorCounter.e();
            return false;
        }

        // 定数宣言している場合は有効な値が格納される
        v.setValue( expr.jjtGetValue() );
        v.status = VariableState.INITIALIZED;
        return true;

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
/*
                 :=
                 +
                 |
            +----+----+
            |         |
   0: <variable>   1:<expr>
*/

        final SimpleNode exprL      = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
        final SimpleNode exprR      = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, data );
        final SymbolDefinition symL = exprL.symbol;
        final SymbolDefinition symR = exprR.symbol;
        Variable variable;

        if( exprL.getId() != JJTREFVARIABLE )
        {
            System.out.println( "NOT Variable : " + symL.name + " - " + exprL );
            AnalyzeErrorCounter.e();
            return exprL;
        }

        variable = variableTable.search( symL.name );
        if( variable == null )
        {
            // exprL 評価内で変数が見つけられなかった
            return exprL;
        }

        if( variable.isConstant( ) )
        {
            System.out.println( "Constant : " + variable.name );
            AnalyzeErrorCounter.e();
            return exprL;
        }
        // 配列要素への格納もあるので配列ビットをマスクさせている
        if( ( variable.type & TYPE_MASK ) != ( symR.type & TYPE_MASK ) )
        {
            System.out.println( ":= not compatible type : " + symL.name + ":=" + symR.type );
            AnalyzeErrorCounter.e();
            return exprL;
        }

        variable.status = VariableState.INITIALIZED;
        return exprL;
    }

    /**
     * 二項演算子の評価
     * @param node 演算子ノード
     * @param intOnly 評価可能なのは整数型のみかどうか（falseの場合は浮動小数も対象）
     * @param booleanOp 演算子はブール演算子かどうか
     * @param jjtAcceptData jjtAcceptメソッドのdata引数
     * @return SimpleNodeインスタンス（データ型を格納した評価結果。エラー時は TYPE_VOID が格納される）
     */
    public SimpleNode evalBinaryOperator( SimpleNode node, boolean intOnly, boolean booleanOp, Object jjtAcceptData )
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
        SimpleNode ret = new SimpleNode( node.getId() );
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
     * 単項演算子の評価
     * @param node 演算子ノード
     * @param intOnly 評価可能なのは整数型のみかどうか（falseの場合は浮動小数も対象）
     * @param booleanOp 演算子はブール演算子かどうか
     * @param jjtAcceptData jjtAcceptメソッドのdata引数
     * @return データ型を格納した評価結果。エラー時は TYPE_VOID が格納される
     */
    public SimpleNode evalSingleOperator( SimpleNode node, boolean intOnly, boolean booleanOp, Object jjtAcceptData )
    {
/*
             <operator>
                 +
                 |
                 +
              <expr>
*/

        final SimpleNode expr       = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, jjtAcceptData );
        final SymbolDefinition symL = expr.symbol;
        int type                    = intOnly ? TYPE_INT : symL.type;

        // 上位ノードの型評価式用
        SimpleNode ret = new SimpleNode( node.getId() );
        SymbolDefinition.copy( node.symbol, ret.symbol );

        // 式が数値型と一致している必要がある
        if( !Variable.isNumeral( type ) )
        {
            System.out.println( "Single operator: Cannot apply to not number type" );
            AnalyzeErrorCounter.e();
            ret.symbol.type = TYPE_VOID;
            ret.symbol.name = Variable.toKSPTypeCharacter( TYPE_VOID );
        }
        else
        {
            int t = booleanOp ? TYPE_BOOL : type;
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
        SimpleNode ret = new SimpleNode( node.getId() );
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
        SimpleNode ret = new SimpleNode( node.getId() );
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
        return evalBinaryOperator( node, true, false, data );
    }

    /**
     * 論理和
     */
    @Override
    public Object visit( ASTAnd node, Object data )
    {
        return evalBinaryOperator( node, true, false, data );
    }

    /**
     * 比較 (=)
     */
    @Override
    public Object visit( ASTEqual node, Object data )
    {
        return evalBinaryOperator( node, false, true, data );
    }

    /**
     * 比較 (#)
     */
    @Override
    public Object visit( ASTNotEqual node, Object data )
    {
        return evalBinaryOperator( node, false, true, data );
    }

    /**
     * 不等号(<)
     */
    @Override
    public Object visit( ASTLT node, Object data )
    {
        return evalBinaryOperator( node, false, true, data );
    }

    /**
     * 不等号(>)
     */
    @Override
    public Object visit( ASTGT node, Object data )
    {
        return evalBinaryOperator( node, false, true, data );
    }

    /**
     * 不等号(<=)
     */
    @Override
    public Object visit( ASTLE node, Object data )
    {
        return evalBinaryOperator( node, false, true, data );
    }

    /**
     * 不等号(>=)
     */
    @Override
    public Object visit( ASTGE node, Object data )
    {
        return evalBinaryOperator( node, false, true, data );
    }

    /**
     * 加算(+)
     */
    @Override
    public Object visit( ASTAdd node, Object data )
    {
        return evalBinaryOperator( node, false, false, data );
    }

    /**
     * 減算(-)
     */
    @Override
    public Object visit( ASTSub node, Object data )
    {
        return evalBinaryOperator( node, false, false, data );
    }

    /**
     * 文字列連結
     */
    @Override
    public Object visit( ASTStrAdd node, Object data )
    {
        // 上位ノードの型評価式用
        SimpleNode ret = new SimpleNode( node.getId() );
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

        // 左辺、右辺どちらか一方が文字列である必要がある（KONTAKT内で暗黙の型変換が作動する）
        if( !Variable.isString( typeL ) && !Variable.isString( typeR ) )
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
        return evalBinaryOperator( node, false, false, data );
    }

    /**
     * 除算(/)
     */
    @Override
    public Object visit( ASTDiv node, Object data )
    {
        return evalBinaryOperator( node, false, false, data );
    }

    /**
     * 余算(mod)
     */
    @Override
    public Object visit( ASTMod node, Object data )
    {
        return evalBinaryOperator( node, false, false, data );
    }

    /**
     * 単項マイナス(-)
     */
    @Override
    public Object visit( ASTNeg node, Object data )
    {
        return evalSingleOperator( node, false, false, data );
    }

    /**
     * 単項NOT(not)
     */
    @Override
    public Object visit( ASTNot node, Object data )
    {
        return evalSingleOperator( node, false, false, data );
    }

    /**
     * 単項論理否定(not)
     */
    @Override
    public Object visit( ASTLogicalNot node, Object data )
    {
        //--------------------------------------------------------------------------
        // 条件評価ステートメントでしか使えない
        //--------------------------------------------------------------------------
        if( !isInConditionalStatement( node ) )
        {
            System.out.println( "LNOT: Cannot assign in Conditional Statement" );
            AnalyzeErrorCounter.e();
        }
        return evalSingleOperator( node, false, true, data );
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
/*
        <variable> [ 0:<arrayindex> ]
*/
        // 上位ノードの型評価式用
        SimpleNode ret = new SimpleNode( JJTREFVARIABLE );
        SymbolDefinition.copy( node.symbol, ret.symbol );

        //--------------------------------------------------------------------------
        // 宣言済みかどうか
        //--------------------------------------------------------------------------
        Variable v = variableTable.search( node.symbol.name );
        if( v == null )
        {
            // 宣言されていない変数
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_NOT_DECLARED, node.symbol );
            AnalyzeErrorCounter.e();
            return ret;
        }
/*
        // 初期化（１度も値が格納されていない）
        if( v.status == VariableState.UNLOADED )
        {
            MessageManager.printlnW( MessageManager.PROPERTY_WARNING_SEMANTIC_VARIABLE_INIT, node.symbol );
            AnalyzeErrorCounter.w();
        }
*/
        // 配列型なら添字チェック
        if( v.isArray() )
        {
            node.jjtGetChild( 0 ).jjtAccept( this, node );
            // 上位ノードの型評価式用
            ret.symbol.type = v.getPrimitiveType();
            ret.symbol.reserved = v.reserved;
            v.referenced = true;
            v.status = VariableState.LOADED;
            return ret;
        }
        // 配列型じゃないのに添え字がある
        else if( node.jjtGetNumChildren() > 0 )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_NOTARRAY, v );
            AnalyzeErrorCounter.e();
        }

        // 上位ノードの型評価式用
        ret.symbol.type = v.getPrimitiveType();
        ret.symbol.reserved = v.reserved;
        v.referenced = true;
        v.status = VariableState.LOADED;

        return ret;
    }

    /**
     * 配列の添え字([])
     * @param data 親ノード
     */
    @Override
    public Object visit( ASTArrayIndex node, Object data )
    {
/*
            parent:<variable>
                    +
                    |
                    +
              [ 0:<expr> ]
*/

        final SimpleNode parent    = (SimpleNode)node.jjtGetParent();
        final SimpleNode expr      = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
        final SymbolDefinition sym = expr.symbol;

        // 上位ノードの型評価式用
        SimpleNode ret = new SimpleNode( JJTREFVARIABLE );
        SymbolDefinition.copy( parent.symbol, ret.symbol );

        // 添え字の型はintのみ
        if( !Variable.isInt( sym.type ) )
        {
            System.out.println( "Array index access : invalid type - " + Variable.toKSPTypeName( sym.type ) );
            AnalyzeErrorCounter.e();
        }
        return ret;
    }

//--------------------------------------------------------------------------
// コマンドコール
//--------------------------------------------------------------------------

    /**
     * コマンド呼び出し
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

        // 上位ノードの型評価式用
        SimpleNode ret = new SimpleNode( JJTREFVARIABLE );
        SymbolDefinition.copy( node.symbol, ret.symbol );
        for( int t : cmd.returnType.typeList )
        {
            ret.symbol.type |= t;
        }

        //--------------------------------------------------------------------------
        // 実行が許可されているコールバック内での呼び出しかどうか
        // ユーザー定義関数内からのコールはチェックしない
        //--------------------------------------------------------------------------
        if( callback != null )
        {
            if( !cmd.availableCallbackList.containsKey( callback.symbol.name ) )
            {
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_COMMAND_NOT_ALLOWED, node.symbol );
                AnalyzeErrorCounter.e();
                return ret;
            }
        }

        //--------------------------------------------------------------------------
        // 引数の数チェック
        //--------------------------------------------------------------------------
        if( node.jjtGetNumChildren() == 0 && cmd.argList.size() > 0 )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_COMMAND_ARGCOUNT, node.symbol );
            AnalyzeErrorCounter.e();
            return ret;
        }
        else if( node.jjtGetNumChildren() > 0 )
        {
            if( node.jjtGetChild( 0 ).jjtGetNumChildren() != cmd.argList.size() )
            {
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_COMMAND_ARGCOUNT, node.symbol );
                AnalyzeErrorCounter.e();
                return ret;
            }
        }

        // 引数の解析
        node.childrenAccept( this, cmd );

        return ret;
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
               for( Argument arg : argList.get( i ).arguments )
                {
                    //--------------------------------------------------------------------------
                    // 型指定なし（全ての型を許容する）
                    //--------------------------------------------------------------------------
                    if( arg.type == TYPE_ANY )
                    {
                        valid = true;
                    }
                    //--------------------------------------------------------------------------
                    // コマンドがUI属性付き変数を要求
                    //--------------------------------------------------------------------------
                    else if( userVar != null && ( arg.accessFlag & ACCESS_ATTR_UI ) != 0 )
                    {
                        if( userVar.uiTypeInfo == null )
                        {
                            // ui_##### 修飾子が無い変数
                            break;
                        }
                        if( arg.uiTypeName.equals( "ui_*") || arg.uiTypeName.equals( userVar.uiTypeInfo.name ) )
                        {
                            // 要求されている ui_#### と変数宣言時の ui_#### 修飾子が一致
                            valid = true;
                            break;
                        }
                    }
                    //--------------------------------------------------------------------------
                    // ui_#### 修飾子が無い変数
                    //--------------------------------------------------------------------------
                    else if( userVar != null )
                    {
                        if( arg.type == userVar.getPrimitiveType() )
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
                                if( ( t & arg.type ) != 0 )
                                {
                                    valid = true;
                                    break;
                                }
                                System.out.println( Integer.toHexString( t ) + " / " + Integer.toHexString( arg.type ) );
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
                        if( ( arg.type & type ) != 0 )
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
// ユーザー定義関数呼び出し
//--------------------------------------------------------------------------

    /**
     * コマンド呼び出し
     */
    @Override
    public Object visit( ASTCallUserFunctionStatement node, Object data )
    {
        UserFunction f = userFunctionTable.search( node.symbol.name );
        if( f == null )
        {
            System.out.println( "User func tion not declared : " + node.symbol.name );
            AnalyzeErrorCounter.e();
        }
        return node;
    }

//--------------------------------------------------------------------------
// ステートメント
//--------------------------------------------------------------------------

    /**
     * select~case の case内の評価
     */
    @Override
    public Object visit( ASTSelectStatement node, Object data )
    {
/*

        select
            -> <expr>
            -> <case>
                -> <casecond>
                -> [ to [<expr>] ]
                -> <block>
            -> <case>
                -> <casecond>
                -> [ to [<expr>] ]
                -> <block>
            :
            :
            expr は評価済みなのでここではスルー
*/
        //--------------------------------------------------------------------------
        // case: 整数の定数または定数宣言した変数が有効
        //--------------------------------------------------------------------------
        for( int i = 1; i < node.jjtGetNumChildren(); i++ )
        {
            SimpleNode caseNode  = (SimpleNode)node.jjtGetChild( i );
            SimpleNode caseCond1 = (SimpleNode)caseNode.jjtGetChild( 0 );
            SimpleNode caseCond2 = null;
            Integer caseValue1   = checkCaseConditionImpl( caseCond1 );
            Integer caseValue2   = null;
            SimpleNode blockNode = (SimpleNode)caseNode.jjtGetChild( caseNode.jjtGetNumChildren() - 1 );

            if( caseValue1 == null )
            {
                blockNode.childrenAccept( this , data );
                return node;
            }
            // to <expr>
            if( caseNode.jjtGetNumChildren() >= 2 )
            {
                caseCond2  = (SimpleNode)caseNode.jjtGetChild( 1 );
                caseValue2 = checkCaseConditionImpl( caseCond2 );
            }

            if( caseCond1 != null && caseCond2 != null )
            {
                // A to B のチェック
                // 例
                // case 1000 to 1000 { range の from to が同じ}
                if( caseValue1 == caseValue2 )
                {
                    System.out.println( "case: same value" );
                    AnalyzeErrorCounter.w();
                }
            }
            blockNode.childrenAccept( this , data );
        }
        return node;
    }

    /**
     * casecond のチェック
     */
    protected Integer checkCaseConditionImpl( SimpleNode caseCond )
    {
        Variable v = variableTable.search( caseCond.symbol.name );
        if( v != null && v.isInt() && v.isConstant() )
        {
            return (Integer)v.value;
        }
        else if( v != null )
        {
            System.out.println( "case: Constant value only this expression" );
            AnalyzeErrorCounter.e();
            return null;
        }
        else
        {
            return (Integer)caseCond.symbol.value;
        }
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
