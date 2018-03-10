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
            case JJTINCLUSIVEOR:    ret = numL | numR; break;
            case JJTAND:            ret = numL & numR; break;
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
     * @param calc 定数値カウント時の再帰処理用。最初のノード時のみ "" を渡す
     */
    static public String evalConstantStringValue( SimpleNode expr, String calc, VariableTable variableTable )
    {
        String ret = null;

        //--------------------------------------------------------------------------
        // リテラル・変数
        //--------------------------------------------------------------------------
        if( expr.jjtGetNumChildren() == 0 )
        {
            switch( expr.getId() )
            {
                case JJTLITERAL:
                {
                    return (String)expr.jjtGetValue();
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
                    else if( !v.isConstant() || !v.isString() )
                    {
                        return null;
                    }
                    return (String)v.value;
                }
            }
        }
        //--------------------------------------------------------------------------
        // 文字列連結演算子
        //--------------------------------------------------------------------------
        else if( expr.jjtGetNumChildren() == 2 )
        {
            SimpleNode exprL = (SimpleNode)expr.jjtGetChild( 0 );
            SimpleNode exprR = (SimpleNode)expr.jjtGetChild( 1 );
            String numL = evalConstantStringValue( exprL, calc, variableTable );
            if( numL == null )
            {
                return null;
            }
            String numR = evalConstantStringValue( exprR, numL, variableTable );
            if( numR == null )
            {
                return null;
            }
            if( expr.getId() != JJTSTRADD )
            {
                throw new RuntimeException( "Unknown nodeId : " + expr.getId() );
            }
            ret = numL + numR;
            // 演算子ノードに定数フラグと畳み込み後の値を格納
            expr.symbol.setTypeFlag( TYPE_STRING, ACCESS_ATTR_CONST );
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
            ( !Variable.isConstant( symL.accessFlag ) || !Variable.isConstant( symR.accessFlag ) ) )
        {
            return null;
        }

        if( Variable.getPrimitiveType( symL.type ) != Variable.getPrimitiveType( symL.type )  )
        {
            return null;
        }

        switch( Variable.getPrimitiveType( symL.type ) )
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
     * @param stringOp 演算子は文字列連結演算子(&)かどうか
     * @param jjtVisitor jjtAcceptメソッドのvisitor引数
     * @param jjtAcceptData jjtAcceptメソッドのdata引数
     * @param variableTable 変数テーブル
     * @return SimpleNodeインスタンス（データ型を格納した評価結果。エラー時は TYPE_VOID が格納される）
     */
    static public SimpleNode evalBinaryOperator( SimpleNode node, boolean numberOp, boolean booleanOp, boolean stringOp, KSPParserVisitor jjtVisitor, Object jjtAcceptData, VariableTable variableTable )
    {
/*
             <operator>
                 +
                 |
            +----+----+
            |         |
        0: <expr>   1:<expr>
*/

        final SimpleNode exprL      = (SimpleNode)node.jjtGetChild( 0 );
        final SimpleNode exprR      = (SimpleNode)node.jjtGetChild( 1 );
        final SymbolDefinition symL = exprL.symbol;
        final SymbolDefinition symR = exprR.symbol;
        int typeL = symL.type;
        int typeR = symR.type;
        boolean typeCheckResult = true;

        exprR.jjtAccept( jjtVisitor, jjtAcceptData );

        // 上位ノードの型評価式用
        SimpleNode ret = EvaluationUtility.createEvalNode( node, node.getId() );

        // 左辺と右辺の型チェック
        if( numberOp )
        {
            // int と real を個別に判定しているのは、KSP が real から int の暗黙の型変換を持っていないため
            if( Variable.isInt( typeL )  && Variable.isInt( typeR ) )
            {
                ret.symbol.type = TYPE_INT;
            }
            else if( Variable.isReal( typeL )  && Variable.isReal( typeR ) )
            {
                ret.symbol.type = TYPE_REAL;
            }
            else
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
            if( ( typeL & typeR ) == 0 )
            {
                typeCheckResult = false;
            }
        }
        //--------------------------------------------------------------------------
        // 左辺、右辺共にリテラル、定数なら式の結果に定数フラグを反映
        // このノード自体を式からリテラルに置き換える
        //--------------------------------------------------------------------------
        if( SymbolDefinition.isConstant( symL.type ) && SymbolDefinition.isConstant( symR.type ) )
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
            ret.symbol.name       = "";
            node = node.reset( new ASTLiteral( JJTLITERAL ), null, constValue, ret.symbol );
            ret = ret.reset( new ASTLiteral( JJTLITERAL ), null, constValue, ret.symbol );
            //System.out.println( constValue + ", " + node.getClass() + ", " + node.symbol.value );
            //System.out.println( node.symbol.dump() );
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
        // 元のノードに型データ、値のコピー（値の畳み込み用）
        SymbolDefinition.setValue( ret.symbol, node.symbol );
        SymbolDefinition.setTypeFlag( ret.symbol, node.symbol );
        return ret;
    }

    /**
     * evalBinaryOperator のコンビニエンスメソッド
     */
    static public SimpleNode evalBinaryNumberOperator( SimpleNode node, KSPParserVisitor jjtVisitor, Object jjtAcceptData, VariableTable variableTable )
    {
        return evalBinaryOperator( node, true, false, false, jjtVisitor, jjtAcceptData, variableTable );
    }

    /**
     * evalBinaryOperator のコンビニエンスメソッド
     */
    static public SimpleNode evalBinaryBooleanOperator( SimpleNode node, KSPParserVisitor jjtVisitor, Object jjtAcceptData, VariableTable variableTable )
    {
        return evalBinaryOperator( node, false, true, false, jjtVisitor, jjtAcceptData, variableTable );
    }

    /**
     * evalBinaryOperator のコンビニエンスメソッド
     */
    static public SimpleNode evalBinaryStringOperator( SimpleNode node, KSPParserVisitor jjtVisitor, Object jjtAcceptData, VariableTable variableTable )
    {
        return evalBinaryOperator( node, false, false, true, jjtVisitor, jjtAcceptData, variableTable );
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
            ret.symbol.name = Variable.toKSPTypeCharacter( type );
        }
        else
        {
            int t = booleanOp ? TYPE_BOOL : type;
            ret.symbol.name = Variable.toKSPTypeCharacter( t );
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