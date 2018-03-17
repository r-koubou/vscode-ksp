/* =========================================================================

    BasicEvaluationAnalyzerTemplate.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.analyzer.SymbolDefinition.SymbolType;
import net.rkoubou.kspparser.javacc.generated.ASTAdd;
import net.rkoubou.kspparser.javacc.generated.ASTAnd;
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
import net.rkoubou.kspparser.javacc.generated.ASTPreProcessorIfDefined;
import net.rkoubou.kspparser.javacc.generated.ASTPreProcessorIfUnDefined;
import net.rkoubou.kspparser.javacc.generated.ASTPreProcessorUnDefine;
import net.rkoubou.kspparser.javacc.generated.ASTRootNode;
import net.rkoubou.kspparser.javacc.generated.ASTStrAdd;
import net.rkoubou.kspparser.javacc.generated.ASTSub;
import net.rkoubou.kspparser.javacc.generated.Node;
import net.rkoubou.kspparser.javacc.generated.SimpleNode;

/**
 * 基本的な四則演算の評価処理を実装したテンプレートクラス
 */
abstract public class BasicEvaluationAnalyzerTemplate extends AbstractAnalyzer
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
    public BasicEvaluationAnalyzerTemplate( ASTRootNode rootNode, SymbolCollector symbolCollector )
    {
        super( rootNode );
        this.uiTypeTable                = symbolCollector.uiTypeTable;
        this.variableTable              = symbolCollector.variableTable;
        this.callbackTable              = symbolCollector.callbackTable;
        this.commandTable               = symbolCollector.commandTable;
        this.userFunctionTable          = symbolCollector.userFunctionTable;
        this.preProcessorSymbolTable    = symbolCollector.preProcessorSymbolTable;
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
        return EvaluationUtility.evalBinaryBooleanOperator( node, this, data, variableTable );
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
        return EvaluationUtility.evalBinaryBooleanOperator( node, this, data, variableTable );
    }

    /**
     * 論理積
     */
    @Override
    public Object visit( ASTInclusiveOr node, Object data )
    {
        return EvaluationUtility.evalBinaryNumberOperator( node, this, data, variableTable );
    }

    /**
     * 論理和
     */
    @Override
    public Object visit( ASTAnd node, Object data )
    {
        return EvaluationUtility.evalBinaryNumberOperator( node, this, data, variableTable );
    }

    /**
     * 比較 (=)
     */
    @Override
    public Object visit( ASTEqual node, Object data )
    {
        return EvaluationUtility.evalBinaryBooleanOperator( node, this, data, variableTable );
    }

    /**
     * 比較 (#)
     */
    @Override
    public Object visit( ASTNotEqual node, Object data )
    {
        return EvaluationUtility.evalBinaryBooleanOperator( node, this, data, variableTable );
    }

    /**
     * 不等号(<)
     */
    @Override
    public Object visit( ASTLT node, Object data )
    {
        return EvaluationUtility.evalBinaryBooleanOperator( node, this, data, variableTable );
    }

    /**
     * 不等号(>)
     */
    @Override
    public Object visit( ASTGT node, Object data )
    {
        return EvaluationUtility.evalBinaryBooleanOperator( node, this, data, variableTable );
    }

    /**
     * 不等号(<=)
     */
    @Override
    public Object visit( ASTLE node, Object data )
    {
        return EvaluationUtility.evalBinaryBooleanOperator( node, this, data, variableTable );
    }

    /**
     * 不等号(>=)
     */
    @Override
    public Object visit( ASTGE node, Object data )
    {
        return EvaluationUtility.evalBinaryBooleanOperator( node, this, data, variableTable );
    }

    /**
     * 加算(+)
     */
    @Override
    public Object visit( ASTAdd node, Object data )
    {
        return EvaluationUtility.evalBinaryNumberOperator( node, this, data, variableTable );
    }

    /**
     * 減算(-)
     */
    @Override
    public Object visit( ASTSub node, Object data )
    {
        return EvaluationUtility.evalBinaryNumberOperator( node, this, data, variableTable );
    }

    /**
     * 文字列連結
     */
    @Override
    public Object visit( ASTStrAdd node, Object data )
    {
        // 上位ノードの型評価式用
        SimpleNode ret = EvaluationUtility.createEvalNode( node, node.getId() );

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
                    ret.symbol.setName( Variable.toKSPTypeCharacter( TYPE_VOID ) );
                    return ret;
                }
                p = p.jjtGetParent();
            }
        }

        final SimpleNode exprL      = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
        final SimpleNode exprR      = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, data );
        final SymbolDefinition symL = exprL.symbol;
        final SymbolDefinition symR = exprR.symbol;

        // 左辺、右辺どちらか一方が文字列である必要がある（KONTAKT内で暗黙の型変換が作動する）
        if( !symL.isString() && !symR.isString() )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_BINOPR_DIFFERENT, node.symbol );
            AnalyzeErrorCounter.e();
            ret.symbol.type = TYPE_VOID;
            ret.symbol.setName( Variable.toKSPTypeCharacter( TYPE_VOID ) );
        }
        else
        {
            ret.symbol.setName( Variable.toKSPTypeCharacter( TYPE_STRING ) );
            ret.symbol.type = TYPE_STRING;
            if( symL.isConstant() && symR.isConstant() )
            {
                // 定数、リテラル文字列同士の連結：結合
                String v = symL.value.toString() + symR.value.toString();
                v = v.replaceAll( "\\\"", "" );
                v = '"' + v + '"';
                ret.symbol.addTypeFlag( TYPE_NONE, ACCESS_ATTR_CONST );
                ret.symbol.value = v;
                node.symbol.setValue( ret.symbol.value );
                SymbolDefinition.setTypeFlag( ret.symbol, node.symbol );
            }
        }
        return ret;
    }

    /**
     * 乗算(*)
     */
    @Override
    public Object visit( ASTMul node, Object data )
    {
        return EvaluationUtility.evalBinaryNumberOperator( node, this, data, variableTable );
    }

    /**
     * 除算(/)
     */
    @Override
    public Object visit( ASTDiv node, Object data )
    {
        return EvaluationUtility.evalBinaryNumberOperator( node, this, data, variableTable );
    }

    /**
     * 余算(mod)
     */
    @Override
    public Object visit( ASTMod node, Object data )
    {
        return EvaluationUtility.evalBinaryNumberOperator( node, this, data, variableTable );
    }

    /**
     * 単項マイナス(-)
     */
    @Override
    public Object visit( ASTNeg node, Object data )
    {
        return EvaluationUtility.evalSingleOperator( node, false, false, this, data, variableTable );
    }

    /**
     * 単項NOT(not)
     */
    @Override
    public Object visit( ASTNot node, Object data )
    {
        return EvaluationUtility.evalSingleOperator( node, false, false, this, data, variableTable );
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
        if( !EvaluationUtility.isInConditionalStatement( node ) )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_SINGLE_OPERATOR_LNOT, node.symbol );
            AnalyzeErrorCounter.e();
        }
        return EvaluationUtility.evalSingleOperator( node, false, true, this, data, variableTable );
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
        if( preProcessorSymbolTable.search( node.symbol ) == null )
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
        // 現状のKONTAKTでは未定義のシンボルでもエラーとならないので
        // 「意味解析では何もしない」
        // どのコールバック内でもundef可能なため、動的に呼ばれるコールバックなどは
        // 実行時に初めて解決するケースがある。
        // -> 意味解析だとASTの構造上スクリプトの上の行から下に向けてトラバースする。
        // 判定方法のコードはコメントアウトで以下に残しておく
/*
        if( preProcessorSymbolTable.search( node.symbol.getName() ) == null )
        {
            MessageManager.printlnW( MessageManager.PROPERTY_WARN_PREPROCESSOR_UNKNOWN_DEF, node.symbol );
            AnalyzeErrorCounter.w();
        }
        else
        {
            preProcessorSymbolTable.remove( node );
        }
*/
        return ret;
    }

    /**
     * ifdef
     */
    @Override
    public Object visit( ASTPreProcessorIfDefined node, Object data )
    {
        Object ret = defaultVisit( node, data );
        return ret;
    }

    /**
     * ifndef
     */
    @Override
    public Object visit( ASTPreProcessorIfUnDefined node, Object data )
    {
        Object ret = defaultVisit( node, data );
        return ret;
    }
}
