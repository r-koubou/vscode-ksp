/* =========================================================================

    EvaluationUtility.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.analyzer.SymbolDefinition.SymbolType;
import net.rkoubou.kspparser.javacc.generated.ASTCallbackDeclaration;
import net.rkoubou.kspparser.javacc.generated.ASTLiteral;
import net.rkoubou.kspparser.javacc.generated.ASTUserFunctionDeclaration;
import net.rkoubou.kspparser.javacc.generated.KSPParserTreeConstants;
import net.rkoubou.kspparser.javacc.generated.KSPParserVisitor;
import net.rkoubou.kspparser.javacc.generated.Node;
import net.rkoubou.kspparser.javacc.generated.SimpleNode;

/**
 * 評価式の汎用処理を実装した補助クラス
 */
public class EvaluationUtility implements AnalyzerConstants, KSPParserTreeConstants
{
    /**
     * Ctor.
     */
    private EvaluationUtility(){}

    /**
     * 上位ノードに返す評価式のテンプレを生成する
     */
    static public SimpleNode createEvalNode( SimpleNode src, int nodeId )
    {
        SimpleNode ret = new SimpleNode( nodeId );
        SymbolDefinition.copy( src.symbol, ret.symbol );
        return ret;
    }

//------------------------------------------------------------------------------
// ステートメント・コールバック・ユーザー関数
//------------------------------------------------------------------------------

    /**
     * 与えられた式が条件ステートメント(if,while等)内で実行されているかどうかを判定する
     * BOOL演算子はこの状況下でしか使用出来ないKSP仕様
     */
    static public boolean isInConditionalStatement( Node expr )
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
    static public ASTCallbackDeclaration getCurrentCallBack( Node child )
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
    static public ASTUserFunctionDeclaration getCurrentUserFunction( Node child )
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

//------------------------------------------------------------------------------
// 変数
//------------------------------------------------------------------------------

    /**
     * 指定されたノードが配列変数参照で、且つ添字を含んでいるかどうかを判定する
     * @param varNode 変数参照のノード
     * @param defaultValue varNodeが配列型変数参照のノードでなかった場合の戻り値
     * @return 配列型変数参照のノードの場合は子ノード(ArrayIndx)を持つかどうかの判定結果、そうでない場合は defaultValue
     */
    static public boolean validArraySubscript( SimpleNode varNode, boolean defaultValue )
    {
        if( varNode.getId() != JJTREFVARIABLE )
        {
            return defaultValue;
        }
        if( !varNode.symbol.isArray())
        {
            return defaultValue;
        }

        // RefVariable [ ArrayIndex ]
        return varNode.jjtGetNumChildren() > 0;
    }

//------------------------------------------------------------------------------
// 畳み込み
//------------------------------------------------------------------------------

    /**
     * 与えられた式ノードから定数値を算出する（畳み込み）
     * 定数値が含まれていない場合はその時点で処理を終了、nullを返す。
     * @param calc 定数値カウント時の再帰処理用。最初のノード時のみ 0 を渡す
     */
    static public Integer evalConstantIntValue( SimpleNode exprL, SimpleNode exprR, int operator, int calc, VariableTable variableTable )
    {
        Integer ret = null;
        Integer numL = evalConstantIntValue( exprL, calc, variableTable );
        if( numL == null )
        {
            return null;
        }
        Integer numR = evalConstantIntValue( exprR, numL, variableTable );
        if( numR == null )
        {
            return null;
        }
        //System.out.println( "L:" + numL + ", R:" + numR );
        switch( operator )
        {
            case JJTADD:            ret = numL + numR; break;
            case JJTSUB:            ret = numL - numR; break;
            case JJTMUL:            ret = numL * numR; break;
            case JJTDIV:            ret = numL / numR; break;
            case JJTMOD:            ret = numL % numR; break;
            case JJTBITWISEOR:      ret = numL | numR; break;
            case JJTBITWISEAND:     ret = numL & numR; break;
        }
        if( ret == null )
        {
            throw new RuntimeException( "Unknown nodeId : " + operator );
        }
        return ret;
    }

    /**
     * 与えられた式ノードから定数値を算出する（畳み込み）
     * 定数値が含まれていない場合はその時点で処理を終了、nullを返す。
     * @param calc 定数値カウント時の再帰処理用。最初のノード時のみ 0 を渡す
     */
    static public Integer evalConstantIntValue( SimpleNode expr, int calc, VariableTable variableTable )
    {
        Integer ret = null;

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
                    Variable v = variableTable.search( expr.symbol );
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
            ret = evalConstantIntValue( exprL, exprR, expr.getId(), calc, variableTable );
            // 演算子ノードに定数フラグと畳み込み後の値を格納
            expr.symbol.setTypeFlag( TYPE_INT, ACCESS_ATTR_CONST );
            expr.symbol.value = ret;
            return ret;
        }
        //--------------------------------------------------------------------------
        // 単項演算子
        //--------------------------------------------------------------------------
        else if( expr.jjtGetNumChildren() == 1 )
        {
            SimpleNode exprL = (SimpleNode)expr.jjtGetChild( 0 );
            Integer numL = evalConstantIntValue( exprL, calc, variableTable );
            if( numL == null )
            {
                return null;
            }
            switch( expr.getId() )
            {
                case JJTNEG:            ret = -numL; break;
                case JJTNOT:            ret = ~numL; break;
                case JJTLOGICALNOT:     ret = numL != 0 ? 0 : 1;  break; // 0=false, 1=true としている
            }
            if( ret == null )
            {
                throw new RuntimeException( "Unknown nodeId : " + expr );
            }
            // 演算子ノードに定数フラグと畳み込み後の値を格納
            expr.symbol.setTypeFlag( TYPE_REAL, ACCESS_ATTR_CONST );
            expr.symbol.value = ret;
        }

        return ret;
    }

    /**
     * 与えられた式ノードから定数値を算出する（畳み込み）
     * 定数値が含まれていない場合はその時点で処理を終了、nullを返す。
     * @param calc 定数値カウント時の再帰処理用。最初のノード時のみ 0 を渡す
     */
    static public Double evalConstantRealValue( SimpleNode exprL, SimpleNode exprR, int operator, double calc, VariableTable variableTable )
    {
        Double ret = null;
        Double numL = evalConstantRealValue( exprL, calc, variableTable );
        if( numL == null )
        {
            return null;
        }
        Double numR = evalConstantRealValue( exprR, numL, variableTable );
        if( numR == null )
        {
            return null;
        }
        switch( operator )
        {
            case JJTADD:            ret = numL + numR; break;
            case JJTSUB:            ret = numL - numR; break;
            case JJTMUL:            ret = numL * numR; break;
            case JJTDIV:            ret = numL / numR; break;
            case JJTMOD:            ret = numL % numR; break;
        }
        if( ret == null )
        {
            throw new RuntimeException( "Unknown nodeId : " + operator );
        }
        return ret;
    }


    /**
     * 与えられた式ノードから定数値を算出する（畳み込み）
     * 定数値が含まれていない場合はその時点で処理を終了、nullを返す。
     * @param calc 定数値カウント時の再帰処理用。最初のノード時のみ 0 を渡す
     */
    static public Double evalConstantRealValue( SimpleNode expr, double calc, VariableTable variableTable )
    {
        Double ret = null;

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
                    Variable v = variableTable.search( expr.symbol );
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
            ret = evalConstantRealValue( exprL, exprR, expr.getId(), calc, variableTable );
            // 演算子ノードに定数フラグと畳み込み後の値を格納
            expr.symbol.setTypeFlag( TYPE_REAL, ACCESS_ATTR_CONST );
            expr.symbol.value = ret;
            return ret;
        }
        //--------------------------------------------------------------------------
        // 単項演算子
        //--------------------------------------------------------------------------
        else if( expr.jjtGetNumChildren() == 1 )
        {
            SimpleNode exprL = (SimpleNode)expr.jjtGetChild( 0 );
            Double numL = evalConstantRealValue( exprL, calc, variableTable );
            if( numL == null )
            {
                return null;
            }
            switch( expr.getId() )
            {
                case JJTNEG: ret = -numL; break;
            }
            if( ret == null )
            {
                throw new RuntimeException( "Unknown nodeId : " + expr.getId() );
            }
            // 演算子ノードに定数フラグと畳み込み後の値を格納
            expr.symbol.setTypeFlag( TYPE_REAL, ACCESS_ATTR_CONST );
            expr.symbol.value = ret;
        }

        return ret;
    }

    /**
     * 与えられた式ノードから定数値を算出する（畳み込み）
     * 定数値が含まれていない場合はその時点で処理を終了、nullを返す。
     */
    static public Boolean evalConstantBooleanValue( SimpleNode node, SimpleNode exprL, SimpleNode exprR, VariableTable variableTable )
    {
        SymbolDefinition symL = exprL.symbol;
        SymbolDefinition symR = exprR.symbol;
        Boolean ret = null;

        if( ( symL.type != symR.type ) ||
            ( !symL.isConstant() || !symR.isConstant() ) )
        {
            return null;
        }

        if( symL.getPrimitiveType() != symR.getPrimitiveType() )
        {
            return null;
        }

        switch( symL.getPrimitiveType() )
        {
            case TYPE_INT:
            {
                Integer intL = evalConstantIntValue( exprL, 0, variableTable );
                Integer intR = evalConstantIntValue( exprR, 0, variableTable );

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
                    if( ret == null )
                    {
                        throw new RuntimeException( "Unknown nodeId : " + node.getId() );
                    }
                    // 演算子ノードに定数フラグと畳み込み後の値を格納
                    node.symbol.setTypeFlag( TYPE_BOOL, ACCESS_ATTR_CONST );
                    node.symbol.value = ret;
                }
            }
            break;
            case TYPE_REAL:
            {
                Double realL = evalConstantRealValue( exprL, 0, variableTable );
                Double realR = evalConstantRealValue( exprR, 0, variableTable );
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
                    if( ret == null )
                    {
                        throw new RuntimeException( "Unknown nodeId : " + node.getId() );
                    }
                    // 演算子ノードに定数フラグと畳み込み後の値を格納
                    node.symbol.setTypeFlag( TYPE_BOOL, ACCESS_ATTR_CONST );
                    node.symbol.value = ret;
                }
            }
            break;
        }

        return ret;
    }

//--------------------------------------------------------------------------
// 式
//--------------------------------------------------------------------------

    /**
     * 二項演算子の評価
     * @param node 演算子ノード
     * @param numberOp 演算子は数値を扱う演算子かどうか
     * @param booleanOp 演算子はブール演算子かどうか
     * @param jjtVisitor jjtAcceptメソッドのvisitor引数
     * @param jjtAcceptData jjtAcceptメソッドのdata引数
     * @param variableTable 変数テーブル
     * @return SimpleNodeインスタンス（データ型を格納した評価結果。エラー時は TYPE_VOID が格納される）
     */
    static public SimpleNode evalBinaryOperator( SimpleNode node, boolean numberOp, boolean booleanOp, KSPParserVisitor jjtVisitor, Object jjtAcceptData, VariableTable variableTable )
    {
/*
             <operator>
                 +
                 |
            +----+----+
            |         |
        0: <expr>   1:<expr>
*/

        final SimpleNode exprL      = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( jjtVisitor, jjtAcceptData );
        final SimpleNode exprR      = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( jjtVisitor, jjtAcceptData );
        final SymbolDefinition symL = exprL.symbol;
        final SymbolDefinition symR = exprR.symbol;
        int typeL = symL.type;
        int typeR = symR.type;
        boolean typeCheckResult = true;

        exprR.jjtAccept( jjtVisitor, jjtAcceptData );

        // 上位ノードの型評価式用
        SimpleNode ret = EvaluationUtility.createEvalNode( node, node.getId() );

        // 型チェック
        if( numberOp )
        {
            // int と real を個別に判定しているのは、KSP が real から int の暗黙の型変換を持っていないため
            if( symL.isInt()  && symR.isInt() )
            {
                ret.symbol.type = TYPE_INT;
            }
            else if( symL.isReal()  && symR.isReal() )
            {
                ret.symbol.type = TYPE_REAL;
            }
            else
            {
                typeCheckResult = false;
            }
        }
        else if( booleanOp )
        {
            ret.symbol.type = TYPE_BOOL;
            if( ( typeL & typeR ) == 0 )
            {
                typeCheckResult = false;
            }
        }
        //--------------------------------------------------------------------------
        // 左辺、右辺共にリテラル、定数なら式の結果に定数フラグを反映
        // このノード自体を式からリテラルに置き換える
        //--------------------------------------------------------------------------
        if( !symL.reserved && !symR.reserved &&
            !symL.isArray() && !symR.isArray() &&
            symL.isConstant() && symR.isConstant() )
        {
            Number constValue = null;
            ret.symbol.accessFlag |= ACCESS_ATTR_CONST;

            if( ret.symbol.isInt() )
            {
                constValue = EvaluationUtility.evalConstantIntValue( node, 0, variableTable );
            }
            else if( ret.symbol.isReal() )
            {
                constValue = EvaluationUtility.evalConstantRealValue( node, 0, variableTable );
            }
            // このノード自体を式からリテラルに置き換える
            ret.symbol.symbolType = SymbolType.Literal;
            ret.symbol.setName( "" );
            node = node.reset( new ASTLiteral( JJTLITERAL ), null, constValue, ret.symbol );
            ret = ret.reset( new ASTLiteral( JJTLITERAL ), null, constValue, ret.symbol );
        }
        //--------------------------------------------------------------------------
        // 型チェック失敗
        //--------------------------------------------------------------------------
        if( !typeCheckResult )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_BINOPR_DIFFERENT, node.symbol );
            AnalyzeErrorCounter.e();
            ret.symbol.type = TYPE_VOID;
            ret.symbol.setName( Variable.toKSPTypeCharacter( TYPE_VOID ) );
        }
        // 元のノードに型データ、値のコピー（値の畳み込み用）
        SymbolDefinition.setValue( ret.symbol, node.symbol );
        SymbolDefinition.setTypeFlag( ret.symbol, node.symbol );
        return ret;
    }

    /**
     * &演算子の評価インプリメンテーション
     * @param node &演算子ノード
     * @param jjtVisitor jjtAcceptメソッドのvisitor引数
     * @param jjtAcceptData jjtAcceptメソッドのdata引数
     * @return SimpleNodeインスタンス（データ型を格納した評価結果。エラー時は TYPE_VOID が格納される）
     */
    static public SimpleNode evalStringAddOperator( SimpleNode node, KSPParserVisitor jjtVisitor, Object jjtAcceptData )
    {
        // 上位ノードの型評価式用
        SimpleNode ret  = EvaluationUtility.createEvalNode( node, node.getId() );
        ret.symbol.type = TYPE_STRING;

        //--------------------------------------------------------------------------
        // ＊初期値代入式では使用できない
        //--------------------------------------------------------------------------
        {
            Node p = node.jjtGetParent();
            while( p != null )
            {
                if( p.getId() == JJTVARIABLEINITIALIZER )
                {
                    MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_INITIALIZER_STRINGADD, node.symbol );
                    AnalyzeErrorCounter.e();
                    ret.symbol.type = TYPE_VOID;
                    ret.symbol.setName( Variable.toKSPTypeCharacter( TYPE_VOID ) );
                    return ret;
                }
                p = p.jjtGetParent();
            }
        }

        final SimpleNode exprL      = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( jjtVisitor, jjtAcceptData );
        final SimpleNode exprR      = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( jjtVisitor, jjtAcceptData );
        final SymbolDefinition symL = exprL.symbol;
        final SymbolDefinition symR = exprR.symbol;

        //----------------------------------------------------------------------
        // KONTAKT内で暗黙の型変換が作動し、文字列型となる
        //----------------------------------------------------------------------

        // 定数、リテラル同士の連結：結合
        if( symL.isConstant() && symR.isConstant() )
        {
            String v = symL.value.toString() + symR.value.toString();
            v = v.replaceAll( "\\\"", "" );
            v = '"' + v + '"';
            ret.symbol.addTypeFlag( TYPE_NONE, ACCESS_ATTR_CONST );
            ret.symbol.value = v;
            node.symbol.setValue( ret.symbol.value );
            SymbolDefinition.setTypeFlag( ret.symbol, node.symbol );
        }
        return ret;
    }

    /**
     * evalBinaryOperator のコンビニエンスメソッド
     */
    static public SimpleNode evalBinaryNumberOperator( SimpleNode node, KSPParserVisitor jjtVisitor, Object jjtAcceptData, VariableTable variableTable )
    {
        return evalBinaryOperator( node, true, false, jjtVisitor, jjtAcceptData, variableTable );
    }

    /**
     * evalBinaryOperator のコンビニエンスメソッド
     */
    static public SimpleNode evalBinaryBooleanOperator( SimpleNode node, KSPParserVisitor jjtVisitor, Object jjtAcceptData, VariableTable variableTable )
    {
        return evalBinaryOperator( node, false, true, jjtVisitor, jjtAcceptData, variableTable );
    }

    /**
     * 単項演算子の評価
     * @param node 演算子ノード
     * @param intOnly 評価可能なのは整数型のみかどうか（falseの場合は浮動小数も対象）
     * @param booleanOp 演算子はブール演算子かどうか
     * @param jjtVisitor jjtAcceptメソッドのvisitor引数
     * @param jjtAcceptData jjtAcceptメソッドのdata引数
     * @param variableTable 変数テーブル
     * @return データ型を格納した評価結果。エラー時は TYPE_VOID が格納される
     */
    static public SimpleNode evalSingleOperator( SimpleNode node, boolean numOnly, boolean booleanOp, KSPParserVisitor jjVisitor, Object jjtAcceptData, VariableTable variableTable )
    {
/*
             <operator>
                 +
                 |
                 +
              <expr>
*/

        final SimpleNode expr       = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( jjVisitor, jjtAcceptData );
        final SymbolDefinition symL = expr.symbol;
        int type                    = numOnly ? TYPE_NUMERICAL : symL.type;

        // 上位ノードの型評価式用
        SimpleNode ret = EvaluationUtility.createEvalNode( node, node.getId() );

        // 式が数値型と一致している必要がある
        if( numOnly && !Variable.isNumeral( type ) )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_SINGLE_OPERATOR_NUMONLY, expr.symbol );
            AnalyzeErrorCounter.e();
            ret.symbol.type = type;
            ret.symbol.setName( Variable.toKSPTypeCharacter( type ) );
        }
        else
        {
            int t = booleanOp ? TYPE_BOOL : type;
            ret.symbol.setName( Variable.toKSPTypeCharacter( t ) );
            ret.symbol.type = t;
        }
        //--------------------------------------------------------------------------
        // リテラル、定数なら式の結果に定数フラグを反映
        //--------------------------------------------------------------------------
        if( expr.symbol.isConstant() )
        {
            ret.symbol.setValue( evalConstantIntValue( node, 0, variableTable ) );
            ret.symbol.addTypeFlag( TYPE_NONE, ACCESS_ATTR_CONST );
        }

        // 元のノードに型データ、値のコピー（値の畳み込み用）
        SymbolDefinition.setValue( ret.symbol, node.symbol );
        SymbolDefinition.setTypeFlag( ret.symbol, node.symbol );
        return ret;
    }
}