/* =========================================================================

    Obfuscator.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.obfuscator;

import java.util.ArrayList;

import net.rkoubou.kspparser.analyzer.AnalyzeErrorCounter;
import net.rkoubou.kspparser.analyzer.Argument;
import net.rkoubou.kspparser.analyzer.BasicEvaluationAnalyzerTemplate;
import net.rkoubou.kspparser.analyzer.Callback;
import net.rkoubou.kspparser.analyzer.CallbackWithArgs;
import net.rkoubou.kspparser.analyzer.Command;
import net.rkoubou.kspparser.analyzer.CommandArgument;
import net.rkoubou.kspparser.analyzer.EvaluationUtility;
import net.rkoubou.kspparser.analyzer.MessageManager;
import net.rkoubou.kspparser.analyzer.PreProcessorSymbol;
import net.rkoubou.kspparser.analyzer.SymbolCollector;
import net.rkoubou.kspparser.analyzer.SymbolDefinition;
import net.rkoubou.kspparser.analyzer.SymbolDefinition.SymbolType;
import net.rkoubou.kspparser.analyzer.UIType;
import net.rkoubou.kspparser.analyzer.UserFunction;
import net.rkoubou.kspparser.analyzer.Variable;
import net.rkoubou.kspparser.javacc.generated.ASTAdd;
import net.rkoubou.kspparser.javacc.generated.ASTArrayIndex;
import net.rkoubou.kspparser.javacc.generated.ASTAssignment;
import net.rkoubou.kspparser.javacc.generated.ASTBlock;
import net.rkoubou.kspparser.javacc.generated.ASTCallCommand;
import net.rkoubou.kspparser.javacc.generated.ASTCallUserFunctionStatement;
import net.rkoubou.kspparser.javacc.generated.ASTCallbackDeclaration;
import net.rkoubou.kspparser.javacc.generated.ASTCaseCondition;
import net.rkoubou.kspparser.javacc.generated.ASTCommandArgumentList;
import net.rkoubou.kspparser.javacc.generated.ASTDiv;
import net.rkoubou.kspparser.javacc.generated.ASTIfStatement;
import net.rkoubou.kspparser.javacc.generated.ASTLiteral;
import net.rkoubou.kspparser.javacc.generated.ASTLogicalNot;
import net.rkoubou.kspparser.javacc.generated.ASTMod;
import net.rkoubou.kspparser.javacc.generated.ASTMul;
import net.rkoubou.kspparser.javacc.generated.ASTNeg;
import net.rkoubou.kspparser.javacc.generated.ASTNot;
import net.rkoubou.kspparser.javacc.generated.ASTPreProcessorDefine;
import net.rkoubou.kspparser.javacc.generated.ASTPreProcessorIfDefined;
import net.rkoubou.kspparser.javacc.generated.ASTPreProcessorIfUnDefined;
import net.rkoubou.kspparser.javacc.generated.ASTPreProcessorUnDefine;
import net.rkoubou.kspparser.javacc.generated.ASTRefVariable;
import net.rkoubou.kspparser.javacc.generated.ASTRootNode;
import net.rkoubou.kspparser.javacc.generated.ASTSelectStatement;
import net.rkoubou.kspparser.javacc.generated.ASTStrAdd;
import net.rkoubou.kspparser.javacc.generated.ASTSub;
import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclaration;
import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclarator;
import net.rkoubou.kspparser.javacc.generated.ASTWhileStatement;
import net.rkoubou.kspparser.javacc.generated.Node;
import net.rkoubou.kspparser.javacc.generated.SimpleNode;

/**
* オブファスケーター実行クラス
*/
public class Obfuscator extends BasicEvaluationAnalyzerTemplate
{

    // ソースコード生成バッファ
    protected final StringBuilder outputCode = new StringBuilder( 1024 * 1024 * 32 );

    // 局所的にのみ使用することを前提としたワークエリア
    private final SymbolDefinition tempSymbol = new SymbolDefinition();

    /**
     * ctor
     */
    public Obfuscator( ASTRootNode rootNode, SymbolCollector symbolCollector )
    {
        super( rootNode, symbolCollector );
    }

    /**
     * オブファスケートの実行
     */
    @Override
    public void analyze() throws Exception
    {
        // オブファスケート：ユーザー定義のシンボル
        ShortSymbolGenerator.reset();
        variableTable.obfuscate();
        userFunctionTable.obfuscate();
        // KSPスクリプト生成
        outputCode.delete( 0, outputCode.length() );
        astRootNode.jjtAccept( this, null );
    }

    /**
     * 生成されたKSPスクリプトの取得
     */
    public String getGeneratedScript()
    {
        return outputCode.toString();
    }

//--------------------------------------------------------------------------
// ユーティリティ
//--------------------------------------------------------------------------

    /**
     * 出力コードに改行を挿入する
     */
    protected void appendEOL()
    {
        outputCode.append( "\n" );
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
        final Variable v = variableTable.search( ((Variable)data).name );

        if( v.isConstant() )
        {
            return true;
        }

        outputCode.append( "declare " );

        if( v.isPolyphonicVariable() )
        {
            outputCode.append( "polyphonic " );
        }

        if( v.isUIVariable() )
        {
            declareUIVariableImpl( node, v, data );
        }
        else if( v.isArray() )
        {
            declareArrayVariableImpl( node, v, data, false );
        }
        else if( node.jjtGetNumChildren() > 0 )
        {
            declarePrimitiveVariableImpl( node, v, data );
        }
        else
        {
            // 宣言のみ
            // const も付与されないので変数参照時の値は変数名
            v.value = v.obfuscatedName;
            outputCode.append( v );
        }
        appendEOL();
        return null;
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
        outputCode.append( v )
        .append( "[" )
        .append( v.arraySize )
        .append( "]" )
        .append( ":=(");

        //--------------------------------------------------------------------------
        // 初期値代入
        //--------------------------------------------------------------------------
        if( forceSkipInitializer || node.jjtGetNumChildren() != 2 )
        {
            // 初期値代入なし
            return false;
        }

        final SimpleNode initializer    = (SimpleNode)node.jjtGetChild( 1 ).jjtGetChild( 0 );
        final int length                = initializer.jjtGetNumChildren();
        for( int i = 0; i < length; i++ )
        {
            final SimpleNode expr = (SimpleNode)initializer.jjtGetChild( i );//.jjtAccept( this, jjtVisitorData );
            if( expr.jjtGetNumChildren() > 0 )
            {
                outputCode.append( v.value );
            }
            else
            {
                outputCode.append( ((SimpleNode)expr.jjtAccept( this, jjtVisitorData )).symbol.value );
            }

            if( i < length - 1 )
            {
                outputCode.append( "," );
            }
        }
        outputCode.append( ")" );
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
        outputCode.append( uiType.name ).append( " " );

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

        Node uiInitializer = node.jjtGetChild( 0 ).jjtGetChild( 0 );

        for( int i = 0; i < uiInitializer.jjtGetNumChildren(); i++ )
        {
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
                        if( param.type == t )
                        {
                            outputCode.append( n.symbol.value );
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
                        if( var.type == t )
                        {
                            outputCode.append( var.value );
                            break SEARCH;
                        }
                        break;
                    }
                }
            } //~for( int t : uiType.initilzerTypeList )
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

        final SimpleNode initializer = (SimpleNode)node.jjtGetChild( 0 );
        final SimpleNode expr        = (SimpleNode)initializer.jjtGetChild( 0 );

        // 初期値代入。畳み込みで有効な値が格納される
        if( expr.symbol.symbolType != SymbolType.Command )
        {
            outputCode.append( v ).append( ":=" );
            expr.jjtAccept( this, jjtVisitorData );
        }
        return true;

    }

//--------------------------------------------------------------------------
// コールバック本体
//--------------------------------------------------------------------------

    /**
     *
     */
    @Override
    public Object visit( ASTCallbackDeclaration node, Object data )
    {
        Callback callback = callbackTable.search( node.symbol.name );
        outputCode.append( "on " ).append( node.symbol.name );

        if( callback != null && callback instanceof CallbackWithArgs )
        {
            // コールバック引数リストあり
            final CallbackWithArgs c          = (CallbackWithArgs)callback;
            final ArrayList<Argument> argList = c.argList;
            final int listSize = argList.size();

            outputCode.append( "(" );
            for( int i = 0; i < listSize; i++ )
            {
                Argument a  = argList.get( i );
                String name = SymbolDefinition.toKSPTypeCharacter( SymbolDefinition.getKSPTypeFromVariableName( a.name )  ) +
                              ShortSymbolGenerator.getSymbolFromOrgName( a.name );

                outputCode.append( name );
                if( i < listSize - 1 )
                {
                    outputCode.append( "," );
                }
            }
            outputCode.append( ")" );
        }
        appendEOL();

        defaultVisit( node, data );

        outputCode.append( "end on" );
        appendEOL();

        return null;
    }


//--------------------------------------------------------------------------
// 式
//--------------------------------------------------------------------------

//     /**
//      * 条件式 OR
//      */
//     @Override
//     public Object visit( ASTConditionalOr node, Object data )
//     {
// /*
//                  or
//                  +
//                  |
//             +----+----+
//             |         |
//         0: <expr>   1:<expr>
// */
//         SimpleNode exprL = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
//         SimpleNode exprR = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, data );
//         outputCode.append( exprL.symbol.value )
//         .append( " or " )
//         .append( exprR.symbol.value );
//         return EvaluationUtility.evalBinaryBooleanOperator( node, this, data );
//     }

//     /**
//      * 条件式 AND
//      */
//     @Override
//     public Object visit( ASTConditionalAnd node, Object data )
//     {
// /*
//                 and
//                  +
//                  |
//             +----+----+
//             |         |
//         0: <expr>   1:<expr>
// */
//         SimpleNode exprL = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
//         SimpleNode exprR = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, data );
//         outputCode.append( exprL.symbol.value )
//         .append( " and " )
//         .append( exprR.symbol.value );
//         return EvaluationUtility.evalBinaryBooleanOperator( node, this, data );
//     }

//     /**
//      * 論理積
//      */
//     @Override
//     public Object visit( ASTInclusiveOr node, Object data )
//     {
//         SimpleNode exprL = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
//         SimpleNode exprR = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, data );
//         outputCode.append( exprL.symbol.value )
//         .append( " .or. " )
//         .append( exprR.symbol.value );
//         return EvaluationUtility.evalBinaryNumberOperator( node, this, data );
//     }

//     /**
//      * 論理和
//      */
//     @Override
//     public Object visit( ASTAnd node, Object data )
//     {
//         SimpleNode exprL = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
//         SimpleNode exprR = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, data );
//         outputCode.append( exprL.symbol.value )
//         .append( " .and. " )
//         .append( exprR.symbol.value );
//         return EvaluationUtility.evalBinaryNumberOperator( node, this, data );
//     }

//     /**
//      * 比較 (=)
//      */
//     @Override
//     public Object visit( ASTEqual node, Object data )
//     {
//         SimpleNode exprL = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
//         SimpleNode exprR = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, data );
//         outputCode.append( exprL.symbol.value )
//         .append( "=" )
//         .append( exprR.symbol.value );
//         return EvaluationUtility.evalBinaryBooleanOperator( node, this, data );
//     }

//     /**
//      * 比較 (#)
//      */
//     @Override
//     public Object visit( ASTNotEqual node, Object data )
//     {
//         SimpleNode exprL = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
//         SimpleNode exprR = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, data );
//         outputCode.append( exprL.symbol.value )
//         .append( "#" )
//         .append( exprR.symbol.value );
//         return EvaluationUtility.evalBinaryBooleanOperator( node, this, data );
//     }

//     /**
//      * 不等号(<)
//      */
//     @Override
//     public Object visit( ASTLT node, Object data )
//     {
//         SimpleNode exprL = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
//         SimpleNode exprR = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, data );
//         outputCode.append( exprL.symbol.value )
//         .append( "<" )
//         .append( exprR.symbol.value );
//         return EvaluationUtility.evalBinaryBooleanOperator( node, this, data );
//     }

//     /**
//      * 不等号(>)
//      */
//     @Override
//     public Object visit( ASTGT node, Object data )
//     {
//         SimpleNode exprL = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
//         SimpleNode exprR = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, data );
//         outputCode.append( exprL.symbol.value )
//         .append( ">" )
//         .append( exprR.symbol.value );
//         return EvaluationUtility.evalBinaryBooleanOperator( node, this, data );
//     }

//     /**
//      * 不等号(<=)
//      */
//     @Override
//     public Object visit( ASTLE node, Object data )
//     {
//         SimpleNode exprL = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
//         SimpleNode exprR = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, data );
//         outputCode.append( exprL.symbol.value )
//         .append( "<=" )
//         .append( exprR.symbol.value );
//         return EvaluationUtility.evalBinaryBooleanOperator( node, this, data );
//     }

//     /**
//      * 不等号(>=)
//      */
//     @Override
//     public Object visit( ASTGE node, Object data )
//     {
//         SimpleNode exprL = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
//         SimpleNode exprR = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, data );
//         outputCode.append( exprL.symbol.value )
//         .append( ">=" )
//         .append( exprR.symbol.value );
//         return EvaluationUtility.evalBinaryBooleanOperator( node, this, data );
//     }

    /**
     * 2項演算ノードのソースコード生成
     */
    public void appendBinaryOperatorNode( SimpleNode node, SimpleNode eval, Object data, String operator )
    {
        System.out.println( "+ : " + node.symbol.isConstant() );
        // 畳み込みが無理な場合は式を出力
        if( !node.symbol.isConstant() )
        {
            SimpleNode exprL = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
            SimpleNode exprR = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, data );
            outputCode.append( exprL.symbol.value )
            .append( operator )
            .append( exprR.symbol.value );
        }
        // ノードの処理終了状態：畳み込み済みの定数値を出力
        else if( node.symbol.state != SymbolState.LOADING )
        {
            outputCode.append( eval.symbol.value );
        }
    }

    /**
     * 加算(+)
     */
    @Override
    public Object visit( ASTAdd node, Object data )
    {
        SimpleNode ret = EvaluationUtility.evalBinaryNumberOperator( node, this, data, variableTable );
        appendBinaryOperatorNode( node, ret, data, "+" );
        return ret;
    }

    /**
     * 減算(-)
     */
    @Override
    public Object visit( ASTSub node, Object data )
    {
        SimpleNode ret = EvaluationUtility.evalBinaryNumberOperator( node, this, data, variableTable );
        appendBinaryOperatorNode( node, ret, data, "-" );
        return ret;
    }

    /**
     * 文字列連結
     */
    @Override
    public Object visit( ASTStrAdd node, Object data )
    {
        SimpleNode ret = EvaluationUtility.evalBinaryStringOperator( node, this, data, variableTable );

        // 畳み込みが無理な場合は式を出力
        if( !node.symbol.isConstant() )
        {
            SimpleNode exprL = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
            SimpleNode exprR = (SimpleNode)node.jjtGetChild( 1 ).jjtAccept( this, data );
            outputCode.append( exprL.symbol.value )
            .append( "&" )
            .append( exprR.symbol.value );
        }
        return ret;
    }

    /**
     * 乗算(*)
     */
    @Override
    public Object visit( ASTMul node, Object data )
    {
        SimpleNode ret = EvaluationUtility.evalBinaryNumberOperator( node, this, data, variableTable );
        appendBinaryOperatorNode( node, ret, data, "*" );
        return ret;
    }

    /**
     * 除算(/)
     */
    @Override
    public Object visit( ASTDiv node, Object data )
    {
        SimpleNode ret = EvaluationUtility.evalBinaryNumberOperator( node, this, data, variableTable );
        appendBinaryOperatorNode( node, ret, data, "/" );
        return ret;
    }

    /**
     * 余算(mod)
     */
    @Override
    public Object visit( ASTMod node, Object data )
    {
        SimpleNode ret = EvaluationUtility.evalBinaryNumberOperator( node, this, data, variableTable );
        appendBinaryOperatorNode( node, ret, data, " mod " );
        return ret;
    }

    /**
     * 単項演算ノードのソースコード生成
     */
    public void appendSingleOperatorNode( SimpleNode node, SimpleNode eval, Object data, String operator )
    {
        // 畳み込みが無理な場合は式を出力
        if( !node.symbol.isConstant() )
        {
            SimpleNode expr = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
            outputCode
            .append( operator )
            .append( expr.symbol.value );
        }
        // 畳み込み済みの定数値を出力
        else
        {
            outputCode.append( eval.symbol.value );
        }
    }

    /**
     * 単項マイナス(-)
     */
    @Override
    public Object visit( ASTNeg node, Object data )
    {
        SimpleNode ret = EvaluationUtility.evalSingleOperator( node, false, false, this, data, variableTable );
        appendSingleOperatorNode( node, ret, data, "-" );
        return ret;
    }

    /**
     * 単項NOT(not)
     */
    @Override
    public Object visit( ASTNot node, Object data )
    {
        SimpleNode ret = EvaluationUtility.evalSingleOperator( node, false, false, this, data, variableTable );
        appendSingleOperatorNode( node, ret, data, " .not. " );
        return ret;
    }

    /**
     * 単項論理否定(not)
     */
    @Override
    public Object visit( ASTLogicalNot node, Object data )
    {
        SimpleNode ret = EvaluationUtility.evalSingleOperator( node, false, true, this, data, variableTable );
        appendSingleOperatorNode( node, ret, data, "not " );
        return ret;
    }

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

        SimpleNode exprL      = (SimpleNode)node.jjtGetChild( 0 );
        SimpleNode exprR      = (SimpleNode)node.jjtGetChild( 1 );
        SymbolDefinition symL = exprL.symbol;

        Variable variable = variableTable.search( symL.name );
        System.out.println( exprL );
        System.out.println( exprR );
        outputCode.append( variable )
        .append( ":=" );

        exprR.childrenAccept( this, data );

        appendEOL();
        return exprL;
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
        SimpleNode ret = EvaluationUtility.createEvalNode( node, JJTREFVARIABLE );

        //--------------------------------------------------------------------------
        // 宣言済みかどうか
        //--------------------------------------------------------------------------
        Variable v = variableTable.search( node.symbol.name );

        // 変数へのアクセスが確定したので、戻り値に変数のシンボル情報をコピー
        SymbolDefinition.copy( v, ret.symbol );

        // ユーザー定義定数なら展開
        if( v.isConstant() && !v.reserved )
        {
            outputCode.append( v.value );
        }
        else
        {
            outputCode.append( v.name );
            // 配列型なら添字チェック
            if( v.isArray() )
            {
                // 上位ノードの型評価式用
                if( node.jjtGetNumChildren() > 0 )
                {
                    // 添え字がある
                    // 要素へのアクセスであるため、配列ビットフラグを外したプリミティブ型として扱う
                    ret.symbol.type = v.getPrimitiveType();
                    node.jjtGetChild( 0 ).jjtAccept( this, node );
                }
                else
                {
                    // 添え字が無い
                    // 配列変数をコマンドの引数に渡すケース
                    // 配列型としてそのまま扱う
                    ret.symbol.type = v.type;
                }
                ret.symbol.reserved = v.reserved;
            }
            return ret;
        }
        // 上位ノードの型評価式用
        ret.symbol.type = v.getPrimitiveType();
        ret.symbol.reserved = v.reserved;
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
        SimpleNode ret = EvaluationUtility.createEvalNode( parent, JJTREFVARIABLE );

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
        SimpleNode ret = EvaluationUtility.createEvalNode( node, JJTREFVARIABLE );
        ret.symbol.symbolType = SymbolType.Command;

        if( cmd == null )
        {
            // ドキュメントに記載のない隠しコマンドの可能性
            // エラーにせず、警告に留める
            // 戻り値不定のため、全てを許可する
            ret.symbol.type = TYPE_MULTIPLE;
            MessageManager.printlnW( MessageManager.PROPERTY_WARNING_SEMANTIC_COMMAND_UNKNOWN, node.symbol );
            AnalyzeErrorCounter.w();
            return ret;
        }

        ASTCallbackDeclaration callback = EvaluationUtility.getCurrentCallBack( node );

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
                    for( Argument a2 : a1.get() )
                    {
                        if( a2.type != TYPE_MULTIPLE && ( a2.type & TYPE_VOID ) != 0 )
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

                // 評価式が変数だった場合のための変数情報への参照
                Variable userVar        = variableTable.search( symbol.name );

                // 引数毎に複数のデータ型が許容される仕様のため照合
                for( Argument arg : argList.get( i ).get() )
                {
                    //--------------------------------------------------------------------------
                    // 型指定なし（全ての型を許容する）
                    //--------------------------------------------------------------------------
                    if( arg.type == TYPE_ALL )
                    {
                        valid = true;
                        break;
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
                        if( arg.type == symbol.type )
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
                        if( ( arg.type & type ) != 0 &&
                            ( arg.type & TYPE_ATTR_MASK ) == ( type & TYPE_ATTR_MASK ) )
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
        f.referenced = true;
        return node;
    }

//--------------------------------------------------------------------------
// ステートメント
//--------------------------------------------------------------------------

    /**
     * スコープ（コールバック・ユーザー定義関数のルート）
     */
    @Override
    public Object visit( ASTBlock node, Object data )
    {
        return node.childrenAccept( this, data );
    }

    /**
     * if 条件式の評価
     */
    @Override
    public Object visit( ASTIfStatement node, Object data )
    {
/*
        if
            -> <expr>
            -> <block>
            :
        | else
            -> <block>
*/
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
        // <block>
        node.childrenAccept( this, data );
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
            Integer caseValue1   = EvaluationUtility.evalConstantIntValue( caseCond1, 0, variableTable );
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
                    caseValue2 = EvaluationUtility.evalConstantIntValue( caseCond2, 0, variableTable );
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
/*
        while
            -> <expr>
            -> <block>
*/

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
        // <block>
        node.childrenAccept( this, data );
        return cond;
    }

    /**
     * リテラル定数参照
     * @return node自身
     */
    @Override
    public Object visit( ASTLiteral node, Object data )
    {
        outputCode.append( node.symbol.value );
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
        if( preProcessorSymbolTable.search( node.symbol.name ) == null )
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
        if( preProcessorSymbolTable.search( node.symbol.name ) == null )
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

    /**
     * 生成されたコードを返す
     */
    @Override
    public String toString()
    {
        return outputCode.toString();
    }
}
