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
import net.rkoubou.kspparser.javacc.generated.ASTAssignment;
import net.rkoubou.kspparser.javacc.generated.ASTCallCommand;
import net.rkoubou.kspparser.javacc.generated.ASTCallUserFunctionStatement;
import net.rkoubou.kspparser.javacc.generated.ASTCallbackDeclaration;
import net.rkoubou.kspparser.javacc.generated.ASTCaseCondition;
import net.rkoubou.kspparser.javacc.generated.ASTCommandArgumentList;
import net.rkoubou.kspparser.javacc.generated.ASTConditionalAnd;
import net.rkoubou.kspparser.javacc.generated.ASTConditionalOr;
import net.rkoubou.kspparser.javacc.generated.ASTDiv;
import net.rkoubou.kspparser.javacc.generated.ASTEqual;
import net.rkoubou.kspparser.javacc.generated.ASTGE;
import net.rkoubou.kspparser.javacc.generated.ASTGT;
import net.rkoubou.kspparser.javacc.generated.ASTIfStatement;
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
import net.rkoubou.kspparser.javacc.generated.ASTWhileStatement;
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

    // 局所的にのみ使用することを前提としたワークエリア
    private final SymbolDefinition tempSymbol = new SymbolDefinition();

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

        //--------------------------------------------------------------------------
        // 解析後の未使用・み初期化シンボルの洗い出し
        //--------------------------------------------------------------------------

        if( AnalyzerOption.unused )
        {
            for( SymbolDefinition s : variableTable.toArray() )
            {
                Variable v = (Variable)s;
                // 参照
                if( !v.referenced )
                {
                    MessageManager.printlnW( MessageManager.PROPERTY_WARNING_SEMANTIC_UNUSE_VARIABLE, v );
                    AnalyzeErrorCounter.w();
                }
                // 初期化（１度も値が格納されていない）
                // ただし、UI型変数は除外
                if( AnalyzerOption.strict && v.status == VariableState.UNLOADED && !v.isUIVariable() )
                {
                    MessageManager.printlnW( MessageManager.PROPERTY_WARNING_SEMANTIC_VARIABLE_INIT, v );
                    AnalyzeErrorCounter.w();
                }

            }
            for( SymbolDefinition v : userFunctionTable.toArray() )
            {
                if( !v.referenced )
                {
                    MessageManager.printlnW( MessageManager.PROPERTY_WARNING_SEMANTIC_UNUSE_FUNCTION, v );
                    AnalyzeErrorCounter.w();
                }
            }
        }
    }

//--------------------------------------------------------------------------
// ユーティリティ
//--------------------------------------------------------------------------

    /**
     * 上位ノードに返す評価式のテンプレを生成する
     */
    public SimpleNode createEvalNode( SimpleNode src, int nodeId )
    {
        SimpleNode ret = new SimpleNode( nodeId );
        SymbolDefinition.copy( src.symbol, ret.symbol );
        return ret;
    }

    /**
     * 与えられた式が条件ステートメント(if,while等)内で実行されているかどうかを判定する
     * BOOL演算子はこの状況下でしか使用出来ないKSP仕様
     */
    protected boolean isInConditionalStatement( Node expr )
    {
        Node p = expr.jjtGetParent();
        while( p != null )
        {
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

    /**
     * 与えられた式ノードから定数値を算出する（畳み込み）
     * 定数値が含まれていない場合はその時点で処理を終了、nullを返す。
     */
    protected Boolean evalConstantBooleanValue( SimpleNode node )
    {
        SimpleNode exprL = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, null );
        SimpleNode exprR = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, null );
        SymbolDefinition symL = exprL.symbol;
        SymbolDefinition symR = exprR.symbol;
        Boolean ret = null;

        if( ( symL.type != symR.type ) ||
            ( !Variable.isConstant( symL.accessFlag ) || !Variable.isConstant( symR.accessFlag ) ) )
        {
            return null;
        }

        ret = false;
        if( Variable.getPrimitiveType( symL.type ) != Variable.getPrimitiveType( symL.type )  )
        {
            return null;
        }

        switch( Variable.getPrimitiveType( symL.type ) )
        {
            case TYPE_INT:
            {
                Integer intL = evalConstantIntValue( exprL,  0 );
                Integer intR = evalConstantIntValue( exprR,  0 );

                if( intL != null && intR != null )
                {
                    switch( node.getId() )
                    {
                        case JJTEQUAL:      ret = intL == intR; break;
                        case JJTNOTEQUAL:   ret = intL != intR; break;
                        case JJTGT:         ret = intL > intR;  break;
                        case JJTLT:         ret = intL < intR;  break;
                        case JJTGE:         ret = intL >= intR; break;
                        case JJTLE:         ret = intL <= intR; break;
                    }
                }
            }
            break;
            case TYPE_REAL:
            {
                Double realL = evalConstantRealValue( exprL,  0 );
                Double realR = evalConstantRealValue( exprR,  0 );
                if( realL != null && realR != null )
                {
                    switch( node.getId() )
                    {
                        case JJTEQUAL:      ret = realL == realR; break;
                        case JJTNOTEQUAL:   ret = realL != realR; break;
                        case JJTGT:         ret = realL > realR;  break;
                        case JJTLT:         ret = realL < realR;  break;
                        case JJTGE:         ret = realL >= realR; break;
                        case JJTLE:         ret = realL <= realR; break;
                    }
                }
            }
            break;
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
            if( v.isConstant() )
            {
                // 定数宣言している場合は初期値代入が必須
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_REQUIRED_INITIALIZER, v ) ;
                AnalyzeErrorCounter.e();
                result = false;
            }
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
        // const 指定のチェック
        //--------------------------------------------------------------------------
        if( v.isConstant() )
        {
            // 配列は const 修飾子を付与できない
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_DECLARE_CONST, v );
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

            if( !Variable.isInt( n.symbol.type ) )
            {
                // 要素数の型が整数以外
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_ARRAYSIZE, v );
                AnalyzeErrorCounter.e();
                return false;
            }

            size = evalConstantIntValue( n, 0 );

            if( size == null || size <= 0 )
            {
                // 要素数が不明、または 0 以下
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_ARRAYSIZE, v );
                AnalyzeErrorCounter.e();
                return false;
            }

            v.arraySize += size;

        }

        if( v.arraySize > MAX_KSP_ARRAY_SIZE )
        {
            // 要素数が上限を超えた or 0
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_MAXARRAYSIZE, v, String.valueOf( MAX_KSP_ARRAY_SIZE ) );
            AnalyzeErrorCounter.e();
            return false;
        }

        //--------------------------------------------------------------------------
        // 初期値代入
        //--------------------------------------------------------------------------
        if( forceSkipInitializer || node.jjtGetNumChildren() != 2 )
        {
            // 初期値代入なし
            v.status = VariableState.UNLOADED;
            return false;
        }

        final SimpleNode initializer    = (SimpleNode)node.jjtGetChild( 1 ).jjtGetChild( 0 );
        if( initializer.getId() != JJTARRAYINITIALIZER )
        {
            // 配列初期化式 ( 0, 1, 2, ...) ではない
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_EXPRESSION_INVALID, initializer.symbol );
            AnalyzeErrorCounter.e();
            return false;
        }

        if( v.isString() )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_STRING_INITIALIZER, v );
            AnalyzeErrorCounter.e();
            return false;
        }

        for( int i = 0; i < initializer.jjtGetNumChildren(); i++ )
        {
            final SimpleNode expr = (SimpleNode)initializer.jjtGetChild( i );
            SymbolDefinition eval = (SymbolDefinition) expr.symbol;

            if( expr.getId() == JJTNEG )
            {
                eval = ( (SimpleNode)expr.jjtGetChild( 0 ) ).symbol;
            }

            if( ( v.type & TYPE_MASK ) != ( eval.type & TYPE_MASK ) )
            {
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_ARRAYINITILIZER, v, String.valueOf( i ) );
                AnalyzeErrorCounter.e();
            }
            else if( !Variable.isConstant( eval.accessFlag ) )
            {
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_EXPRESSION_CONSTANTONLY, eval );
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
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_UITYPE, v, Variable.getTypeName( uiType.uiValueType ), uiType.name );
            AnalyzeErrorCounter.e();
            return false;
        }

        //--------------------------------------------------------------------------
        // const 指定のチェック
        //--------------------------------------------------------------------------
        if( v.isConstant() )
        {
            // ui_#### const 修飾子を付与できない
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_DECLARE_CONST, v );
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
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_REQUIRED_INITIALIZER, v );
            AnalyzeErrorCounter.e();
            return false;
        }

        Node uiInitializer = node.jjtGetChild( 0 ).jjtGetChild( 0 );
        if( uiInitializer.getId() != JJTUIINITIALIZER )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_INITIALIZER, v );
            return false;
        }
        if( uiInitializer.jjtGetNumChildren() != uiType.initilzerTypeList.length )
        {
            // 引数の数が一致していない
            String cnt = String.valueOf( uiInitializer.jjtGetNumChildren() );
            String req = String.valueOf( uiType.initilzerTypeList.length );
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_UIINITIALIZER_COUNT, v, uiType.name, cnt, req );
            AnalyzeErrorCounter.e();
            return false;
        }

        for( int i = 0; i < uiInitializer.jjtGetNumChildren(); i++ )
        {
            boolean found = false;
            SimpleNode n  = (SimpleNode)uiInitializer.jjtGetChild( i );
            SymbolDefinition param = n.symbol;
            int nid = n.getId();
            int argT = 0;

            // 条件式BOOLはエラー対象
            if( nid == JJTCONDITIONALOR || nid == JJTCONDITIONALAND )
            {
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_EXPRESSION_INVALID, n.symbol );
                AnalyzeErrorCounter.e();
                continue;
            }

            // 四則演算等は文法解析時でクリアしているので値だけに絞る
            if( nid != JJTLITERAL && nid != JJTREFVARIABLE )
            {
                continue;
            }

SEARCH:
            for( int t : uiType.initilzerTypeList )
            {
                argT = t;
                switch( nid )
                {
                    //--------------------------------------------------------------------------
                    // リテラル
                    //--------------------------------------------------------------------------
                    case JJTLITERAL:
                    {
                        if( param.type == t )
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
                            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_EXPRESSION_CONSTANTONLY, n.symbol );
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
                        MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_EXPRESSION_INVALID, n.symbol );
                        AnalyzeErrorCounter.e();
                    }
                    break;
                }
                if( !Variable.isConstant( param.accessFlag ) )
                {
                    MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_EXPRESSION_CONSTANTONLY, n.symbol );
                    AnalyzeErrorCounter.e();
                }
            } //~for( int t : uiType.initilzerTypeList )

            if( !found )
            {
                // イニシャライザ: 型の不一致
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_UIINITIALIZER_TYPE, v, String.valueOf( i ), Variable.getTypeName( argT ) );
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
            if( v.isConstant() )
            {
                // 定数宣言している場合は初期値代入が必須
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_REQUIRED_INITIALIZER, v ) ;
                AnalyzeErrorCounter.e();
                return false;
            }
            // 初期値代入なし
            return true;
        }

        final SimpleNode initializer = (SimpleNode)node.jjtGetChild( 0 );
        final SimpleNode expr        = (SimpleNode)initializer.jjtGetChild( 0 );
        SymbolDefinition eval        = (SymbolDefinition) expr.symbol;

        if( initializer.getId() != JJTVARIABLEINITIALIZER )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_EXPRESSION_INVALID, initializer.symbol );
            return false;
        }

        if( expr.getId() == JJTNEG )
        {
            eval = ( (SimpleNode)expr.jjtGetChild( 0 ) ).symbol;
        }

        // 型の不一致
        if( v.type != eval.type )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_INITIALIZER_TYPE, v, Variable.getTypeName( eval.type ), Variable.getTypeName( v.type ) ) ;
            AnalyzeErrorCounter.e();
            return false;
        }

        // 文字列型は初期値代入不可
        if( v.isString() )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_STRING_INITIALIZER, v );
            AnalyzeErrorCounter.e();
            return false;
        }

        // 初期値代入。畳み込みで有効な値が格納される
        Object value = null;
        if( v.isInt() )
        {
            value = evalConstantIntValue( expr, 0 );
        }
        else if( v.isReal() )
        {
            value = evalConstantRealValue( expr, 0 );
        }
        else if( v.isString() )
        {
            // 文字列は初期値代入不可
            value = null;
        }
        v.setValue( value );
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
        int exprLType = TYPE_NONE;
        int exprRType = TYPE_NONE;

        if( exprL.getId() != JJTREFVARIABLE )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_ASSIGN_NOTVARIABLE, exprL.symbol );
            AnalyzeErrorCounter.e();
            return exprL;
        }

        variable = variableTable.search( symL.name );
        if( variable == null )
        {
            // exprL 評価内で変数が見つけられなかった
            return exprL;
        }

        exprLType = variable.type;
        exprRType = exprR.symbol.type;

        if( variable.isConstant( ) )
        {
            SymbolDefinition.copy( exprR.symbol, tempSymbol );
            tempSymbol.name = variable.name;
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_ASSIGN_CONSTVARIABLE, tempSymbol );
            AnalyzeErrorCounter.e();
            return exprL;
        }
        // コマンドコールの戻り値が複数の型を持つなどで暗黙の型変換を要する場合
        // 代入先の変数に合わせる。暗黙の型変換が不可能な場合は、代替としてVOIDを入れる
        if( Variable.hasMultipleType( exprRType ) )
        {
            if( ( exprLType & exprRType ) == 0 )
            {
                exprRType = TYPE_VOID;
            }
            else
            {
                // 暗黙の型変換
                exprRType = exprLType;
            }
        }
        // 配列要素への格納もあるので配列ビットをマスクさせている
        if( ( exprLType & TYPE_MASK ) != ( exprRType & TYPE_MASK ) )
        {
            String vType = Variable.getTypeName( Variable.getPrimitiveType( exprLType ) );
            String aType = Variable.getTypeName( exprRType );
            SymbolDefinition.copy( symR, tempSymbol );
            tempSymbol.name = variable.name;

            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_ASSIGN_TYPE_NOTCOMPATIBLE, tempSymbol, vType, aType );
            AnalyzeErrorCounter.e();
            return exprL;
        }

        variable.status = VariableState.LOADED;
        return exprL;
    }

    /**
     * 二項演算子の評価
     * @param node 演算子ノード
     * @param numberOp 演算子は数値を扱う演算子かどうか
     * @param booleanOp 演算子はブール演算子かどうか
     * @param stringOp 演算子は文字列連結演算子(&)かどうか
     * @param jjtAcceptData jjtAcceptメソッドのdata引数
     * @return SimpleNodeインスタンス（データ型を格納した評価結果。エラー時は TYPE_VOID が格納される）
     */
    public SimpleNode evalBinaryOperator( SimpleNode node, boolean numberOp, boolean booleanOp, boolean stringOp, Object jjtAcceptData )
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
        int typeL = symL.type;
        int typeR = symR.type;
        boolean typeCheckResult = true;

        // 上位ノードの型評価式用
        SimpleNode ret = createEvalNode( node, node.getId() );

        // 左辺と右辺の型チェック
        if( numberOp )
        {
            ret.symbol.type = typeL;
            if( typeL != typeR )
            {
                typeCheckResult = false;
            }
        }
        else if( stringOp )
        {
            ret.symbol.type = TYPE_STRING;
            // どちらか一方の辺が文字列型ならOK（&演算子）
            if( ( !Variable.isString( typeL ) && !Variable.isString( typeR ) ) || node.getId() != JJTSTRADD )
            {
                typeCheckResult = false;
            }
        }
        else if( booleanOp )
        {
            ret.symbol.type = TYPE_BOOL;
            if( typeL != typeR )
            {
                typeCheckResult = false;
            }
        }
        //--------------------------------------------------------------------------
        // 左辺、右辺共にリテラル、定数なら式の結果に定数フラグを反映
        //--------------------------------------------------------------------------
        if( Variable.isConstant( symL.type ) && Variable.isConstant( symR.type ) )
        {
            ret.symbol.accessFlag |= ACCESS_ATTR_CONST;
        }
        //--------------------------------------------------------------------------
        // 型チェック失敗
        //--------------------------------------------------------------------------
        if( !typeCheckResult )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_BINOPR_DIFFERENT, node.symbol );
            AnalyzeErrorCounter.e();
            ret.symbol.type = TYPE_VOID;
            ret.symbol.name = Variable.toKSPTypeCharacter( TYPE_VOID );
        }
        return ret;
    }

    /**
     * evalBinaryOperator のコンビニエンスメソッド
     */
    public SimpleNode evalBinaryNumberOperator( SimpleNode node, Object jjtAcceptData )
    {
        return evalBinaryOperator( node, true, false, false, jjtAcceptData );
    }

    /**
     * evalBinaryOperator のコンビニエンスメソッド
     */
    public SimpleNode evalBinaryBooleanOperator( SimpleNode node, Object jjtAcceptData )
    {
        return evalBinaryOperator( node, false, true, false, jjtAcceptData );
    }

    /**
     * evalBinaryOperator のコンビニエンスメソッド
     */
    public SimpleNode evalBinaryStringOperator( SimpleNode node, Object jjtAcceptData )
    {
        return evalBinaryOperator( node, false, false, true, jjtAcceptData );
    }

    /**
     * 単項演算子の評価
     * @param node 演算子ノード
     * @param intOnly 評価可能なのは整数型のみかどうか（falseの場合は浮動小数も対象）
     * @param booleanOp 演算子はブール演算子かどうか
     * @param jjtAcceptData jjtAcceptメソッドのdata引数
     * @return データ型を格納した評価結果。エラー時は TYPE_VOID が格納される
     */
    public SimpleNode evalSingleOperator( SimpleNode node, boolean numOnly, boolean booleanOp, Object jjtAcceptData )
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
        int type                    = numOnly ? TYPE_NUMERICAL : symL.type;

        // 上位ノードの型評価式用
        SimpleNode ret = createEvalNode( node, node.getId() );

        // 式が数値型と一致している必要がある
        if( numOnly && !Variable.isNumeral( type ) )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_SINGLE_OPERATOR_NUMONLY, expr.symbol );
            AnalyzeErrorCounter.e();
            ret.symbol.type = type;
            ret.symbol.name = Variable.toKSPTypeCharacter( type );
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
        return evalBinaryBooleanOperator( node, data );
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
        return evalBinaryBooleanOperator( node, data );
    }

    /**
     * 論理積
     */
    @Override
    public Object visit( ASTInclusiveOr node, Object data )
    {
        return evalBinaryNumberOperator( node, data );
    }

    /**
     * 論理和
     */
    @Override
    public Object visit( ASTAnd node, Object data )
    {
        return evalBinaryNumberOperator( node, data );
    }

    /**
     * 比較 (=)
     */
    @Override
    public Object visit( ASTEqual node, Object data )
    {
        return evalBinaryBooleanOperator( node, data );
    }

    /**
     * 比較 (#)
     */
    @Override
    public Object visit( ASTNotEqual node, Object data )
    {
        return evalBinaryBooleanOperator( node, data );
    }

    /**
     * 不等号(<)
     */
    @Override
    public Object visit( ASTLT node, Object data )
    {
        return evalBinaryBooleanOperator( node, data );
    }

    /**
     * 不等号(>)
     */
    @Override
    public Object visit( ASTGT node, Object data )
    {
        return evalBinaryBooleanOperator( node, data );
    }

    /**
     * 不等号(<=)
     */
    @Override
    public Object visit( ASTLE node, Object data )
    {
        return evalBinaryBooleanOperator( node, data );
    }

    /**
     * 不等号(>=)
     */
    @Override
    public Object visit( ASTGE node, Object data )
    {
        return evalBinaryBooleanOperator( node, data );
    }

    /**
     * 加算(+)
     */
    @Override
    public Object visit( ASTAdd node, Object data )
    {
        return evalBinaryNumberOperator( node, data );
    }

    /**
     * 減算(-)
     */
    @Override
    public Object visit( ASTSub node, Object data )
    {
        return evalBinaryNumberOperator( node, data );
    }

    /**
     * 文字列連結
     */
    @Override
    public Object visit( ASTStrAdd node, Object data )
    {
        // 上位ノードの型評価式用
        SimpleNode ret = createEvalNode( node, node.getId() );

        //--------------------------------------------------------------------------
        // ＊初期値代入式では使用できない
        //--------------------------------------------------------------------------
        {
            Node p = node.jjtGetParent();
            while( p != null )
            {
                if( p.getId() == JJTVARIABLEDECLARATOR )
                {
                    MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_INITIALIZER_STRINGADD, node.symbol );
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
        return evalBinaryNumberOperator( node, data );
    }

    /**
     * 除算(/)
     */
    @Override
    public Object visit( ASTDiv node, Object data )
    {
        return evalBinaryNumberOperator( node, data );
    }

    /**
     * 余算(mod)
     */
    @Override
    public Object visit( ASTMod node, Object data )
    {
        return evalBinaryNumberOperator( node, data );
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
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_SINGLE_OPERATOR_LNOT, node.symbol );
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
        SimpleNode ret = createEvalNode( node, JJTREFVARIABLE );

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

        // 変数へのアクセスが確定したので、戻り値に変数のシンボル情報をコピー
        SymbolDefinition.copy( v, ret.symbol );

        if( node.jjtGetParent() != null && node.jjtGetParent().getId() == JJTASSIGNMENT )
        {
            // これから代入される予定の変数
            v.status = VariableState.LOADING;
        }

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
            SymbolDefinition.copy( node.symbol, tempSymbol );
            tempSymbol.name = v.name;

            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_NOTARRAY, tempSymbol );
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
        SimpleNode ret = createEvalNode( parent, JJTREFVARIABLE );

        // 添え字の型はintのみ
        if( !Variable.isInt( sym.type ) )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_ARRAY_ELEMENT_INTONLY, expr.symbol );
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

        // 上位ノードの型評価式用
        SimpleNode ret = createEvalNode( node, JJTREFVARIABLE );

        if( cmd == null )
        {
            // ドキュメントに記載のない隠しコマンドの可能性
            // エラーにせず、警告に留める
            // 戻り値不定のため、全てを許可する
            ret.symbol.type = TYPE_ANY;
            MessageManager.printlnW( MessageManager.PROPERTY_WARNING_SEMANTIC_COMMAND_UNKNOWN, node.symbol );
            AnalyzeErrorCounter.w();
            return ret;
        }

        ASTCallbackDeclaration callback = getCurrentCallBack( node );

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
            boolean valid = false;
            // void(引数なしの括弧だけ)ならエラーの対象外
            if( cmd.argList.size() == 1 )
            {
SEARCH:
                for( CommandArgument a1 : cmd.argList )
                {
                    for( Argument a2 : a1.arguments )
                    {
                        if( a2.type != TYPE_ANY && ( a2.type & TYPE_VOID ) != 0 )
                        {
                            valid = true;
                            break SEARCH;
                        }
                    }
                }
            }
            if( !valid )
            {
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_COMMAND_ARGCOUNT, node.symbol );
                AnalyzeErrorCounter.e();
            }
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
                            }
                        }
                        else
                        {
                            // 未知のコマンドなので正しいかどうかの判定が不可能
                            // エラーにせずに警告に留める
                            MessageManager.printlnW( MessageManager.PROPERTY_WARNING_SEMANTIC_COMMAND_UNKNOWN, callCmd.symbol );
                            AnalyzeErrorCounter.w();
                            // 戻り値の型チェックが不可能なのでデータ型は一致したものとみなす
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
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_USERFUNCTION_NOT_DECLARED, node.symbol );
            AnalyzeErrorCounter.e();
            return node;
        }
        f.called = true;
        return node;
    }

//--------------------------------------------------------------------------
// ステートメント
//--------------------------------------------------------------------------

    /**
     * if 条件式の評価
     */
    @Override
    public Object visit( ASTIfStatement node, Object data )
    {
        SimpleNode cond = ((SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data) );
        //--------------------------------------------------------------------------
        // 条件式がBOOL型でない場合
        //--------------------------------------------------------------------------
        {
            if( !Variable.isBoolean( cond.symbol.type ) )
            {
                MessageManager.printlnE(
                    MessageManager.PROPERTY_ERROR_SEMANTIC_CONDITION_INVALID,
                    cond.symbol,
                    Variable.getTypeName( TYPE_BOOL )
                );
                AnalyzeErrorCounter.e();
            }
        }
        return cond;
    }

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
*/

        //--------------------------------------------------------------------------
        // 条件式が整数型でない場合
        //--------------------------------------------------------------------------
        {
            SimpleNode cond = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
            if( !Variable.isInt( cond.symbol.type ) || Variable.isArray( cond.symbol.type )  )
            {
                MessageManager.printlnE(
                    MessageManager.PROPERTY_ERROR_SEMANTIC_CONDITION_INVALID,
                    cond.symbol,
                    Variable.getTypeName( TYPE_INT )
                );
                AnalyzeErrorCounter.e();
                return node;
            }
        }

        //--------------------------------------------------------------------------
        // case: 整数の定数または定数宣言した変数が有効
        //--------------------------------------------------------------------------
        for( int i = 1; i < node.jjtGetNumChildren(); i++ )
        {
            SimpleNode caseNode  = (SimpleNode)node.jjtGetChild( i );
            SimpleNode caseCond1 = (SimpleNode)caseNode.jjtGetChild( 0 ).jjtAccept( this, data );
            SimpleNode caseCond2 = null;
            Integer caseValue1   = evalConstantIntValue( caseCond1, 0 );
            Integer caseValue2   = null;
            SimpleNode blockNode = (SimpleNode)caseNode.jjtGetChild( caseNode.jjtGetNumChildren() - 1 );

            // 定数値ではない
            if( caseValue1 == null )
            {
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_CASEVALUE_CONSTONLY, caseCond1.symbol );
                AnalyzeErrorCounter.e();

                blockNode.childrenAccept( this , data );
                return node;
            }
            // to <expr>
            if( caseNode.jjtGetNumChildren() >= 2 )
            {
                SimpleNode n = (SimpleNode)caseNode.jjtGetChild( 1 );
                if( n.getId() == JJTCASECONDITION ) // to ではない場合は Block ノード
                {
                    caseCond2  = (SimpleNode)caseNode.jjtGetChild( 1 ).jjtAccept( this, data );
                    caseValue2 = evalConstantIntValue( caseCond2, 0 );
                    if( caseValue2 == null )
                    {
                        // 定数値ではない
                        MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_CASEVALUE_CONSTONLY, caseCond1.symbol );
                        AnalyzeErrorCounter.e();
                        blockNode.childrenAccept( this , data );
                        return node;
                    }
                }
            }
            if( caseValue1 != null && caseValue2 != null )
            {
                // A to B のチェック
                // 例
                // case 1000 to 1000 { range の from to が同じ}
                if( caseValue1.intValue() == caseValue2.intValue() )
                {
                    MessageManager.printlnW( MessageManager.PROPERTY_WARNING_SEMANTIC_CASEVALUE, caseCond1.symbol, caseValue1.toString(), caseValue2.toString() );
                    AnalyzeErrorCounter.w();
                }
            }
            blockNode.childrenAccept( this , data );
        }
        return node;
    }

    /**
     * casecondの評価
     */
    @Override
    public Object visit( ASTCaseCondition node, Object data )
    {
/*
    <casecond>
        -> <expr>
*/
        return (SimpleNode)( node.jjtGetChild( 0 ) );
    }

    /**
     * while 条件式の評価
     */
    @Override
    public Object visit( ASTWhileStatement node, Object data )
    {
        SimpleNode cond = ((SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data) );
        //--------------------------------------------------------------------------
        // 条件式がBOOL型でない場合
        //--------------------------------------------------------------------------
        {
            if( !Variable.isBoolean( cond.symbol.type ) )
            {
                MessageManager.printlnE(
                    MessageManager.PROPERTY_ERROR_SEMANTIC_CONDITION_INVALID,
                    cond.symbol,
                    Variable.getTypeName( TYPE_BOOL )
                );
                AnalyzeErrorCounter.e();
            }
        }
        return cond;
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
