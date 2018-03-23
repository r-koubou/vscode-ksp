/* =========================================================================

    Obfuscator.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.obfuscator;

import net.rkoubou.kspparser.analyzer.BasicEvaluationAnalyzerTemplate;
import net.rkoubou.kspparser.analyzer.Command;
import net.rkoubou.kspparser.analyzer.SymbolCollector;
import net.rkoubou.kspparser.analyzer.SymbolDefinition.SymbolType;
import net.rkoubou.kspparser.analyzer.UIType;
import net.rkoubou.kspparser.analyzer.UserFunction;
import net.rkoubou.kspparser.analyzer.Variable;
import net.rkoubou.kspparser.javacc.generated.ASTAdd;
import net.rkoubou.kspparser.javacc.generated.ASTAnd;
import net.rkoubou.kspparser.javacc.generated.ASTArrayIndex;
import net.rkoubou.kspparser.javacc.generated.ASTArrayInitializer;
import net.rkoubou.kspparser.javacc.generated.ASTAssignment;
import net.rkoubou.kspparser.javacc.generated.ASTBlock;
import net.rkoubou.kspparser.javacc.generated.ASTCallCommand;
import net.rkoubou.kspparser.javacc.generated.ASTCallUserFunctionStatement;
import net.rkoubou.kspparser.javacc.generated.ASTCallbackArgumentList;
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
import net.rkoubou.kspparser.javacc.generated.ASTPreProcessorIfDefined;
import net.rkoubou.kspparser.javacc.generated.ASTPreProcessorIfUnDefined;
import net.rkoubou.kspparser.javacc.generated.ASTPreProcessorUnDefine;
import net.rkoubou.kspparser.javacc.generated.ASTPrimitiveInititalizer;
import net.rkoubou.kspparser.javacc.generated.ASTRefVariable;
import net.rkoubou.kspparser.javacc.generated.ASTRootNode;
import net.rkoubou.kspparser.javacc.generated.ASTSelectStatement;
import net.rkoubou.kspparser.javacc.generated.ASTStrAdd;
import net.rkoubou.kspparser.javacc.generated.ASTSub;
import net.rkoubou.kspparser.javacc.generated.ASTUIInitializer;
import net.rkoubou.kspparser.javacc.generated.ASTUserFunctionDeclaration;
import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclaration;
import net.rkoubou.kspparser.javacc.generated.ASTVariableInitializer;
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
    VariableDeclaration                     // NOW
            -> ASTVariableInitializer
                -> [
                      ArrayInitializer
                    | UIInitializer
                    | PrimitiveInititalizer
                ]
*/
        Variable v = variableTable.search( node.symbol );
        if( v.isConstant() || ( !v.referenced && !v.reserved ) )
        {
            return node;
        }

        outputCode.append( "declare " );

        if( v.isPolyphonicVariable() )
        {
            outputCode.append( "polyphonic " );
        }

        if( node.jjtGetNumChildren() == 0 )
        {
            // 宣言のみ
            // const も付与されないので変数参照時の値は変数名
            v.value = v.getVariableName();
            outputCode.append( v.getVariableName() );
            appendEOL();
            return node;
        }

        outputCode.append( v.getVariableName() );

        node.jjtGetChild( 0 ).jjtAccept( this, node );
        appendEOL();

        return node;
    }

    /**
     * 変数宣言(+初期値代入)
     */
    @Override
    public Object visit( ASTVariableInitializer node, Object data )
    {
/*
    VariableDeclaration
            -> ASTVariableInitializer   // NOW
                -> [
                      ArrayInitializer
                    | UIInitializer
                    | PrimitiveInititalizer
                ]
*/
        node.childrenAccept( this, data );
        return node;
    }

    /**
     * 配列型宣言の実装
     */
    @Override
    public Object visit( ASTArrayInitializer node, Object data )
    {
/*
    VariableDeclaration
            -> ASTVariableInitializer
                -> [
                        ArrayInitializer        //NOW
                            -> ArrayIndex
                            -> Expression
                            -> (,Expression)*
                    | UIInitializer
                    | PrimitiveInititalizer
                ]
*/
        arrayInitializerImpl( node, data, false );
        return node;
    }

    /**
     * 配列型宣言の実装(詳細).
     * UI変数かつ配列型のケースもあるので外部化
     */
    protected void arrayInitializerImpl( SimpleNode node, Object data, boolean forceSkipInitializer )
    {
/*
    VariableDeclaration
            -> ASTVariableInitializer
                -> [
                        ArrayInitializer        //NOW
                            -> ArrayIndex
                            -> Expression
                            -> (,Expression)*
                    | UIInitializer
                    | PrimitiveInititalizer
                ]
*/
        final Variable v = variableTable.search( ((SimpleNode)node.jjtGetParent().jjtGetParent()).symbol );

        outputCode.append( "[" )
        .append( v.arraySize )
        .append( "]" );

        //--------------------------------------------------------------------------
        // 初期値代入
        //--------------------------------------------------------------------------
        if( forceSkipInitializer || node.jjtGetNumChildren() == 1 )
        {
            // 初期値代入なし
            return;
        }

        outputCode.append( ":=(");

        final int length = node.jjtGetNumChildren();
        for( int i = 1; i < length; i++ )
        {
            node.jjtGetChild( i ).jjtAccept( this, data );
            if( i < length - 1 )
            {
                outputCode.append( "," );
            }
        }

        outputCode.append( ")" );

    }

    /**
     * UI型宣言の実装
     */
    @Override
    public Object visit( ASTUIInitializer node, Object data )
    {
/*
    VariableDeclaration
            -> ASTVariableInitializer
                -> [
                      ArrayInitializer
                    | UIInitializer         // NOW
                        -> [ArrayIndex]
                        -> Expression
                        -> (,Expression)*
                    | PrimitiveInititalizer
                ]
*/
        final Variable v = variableTable.search( ((SimpleNode)node.jjtGetParent().jjtGetParent()).symbol );
        final UIType uiType = uiTypeTable.search( v.uiTypeName );

        outputCode.append( uiType.name ).append( " " );

        //--------------------------------------------------------------------------
        // ui_#### が配列型の場合、要素数宣言までのコード生成
        //--------------------------------------------------------------------------
        if( Variable.isArray( uiType.uiValueType ) )
        {
            arrayInitializerImpl( node, data, true );
            return node;
        }

        //--------------------------------------------------------------------------
        // 初期値代入
        //--------------------------------------------------------------------------
        if( node.jjtGetNumChildren() == 0 )
        {
            // 初期値代入なし
            return node;
        }

        // for のカウンタ初期値の設定
        int i;
        if( node.jjtGetChild( 0 ).getId() == JJTARRAYINDEX )
        {
            // 変数配列型の場合
            // node[ 0 ]        : ArrayIndex
            // node[ 1 ... n ]  : Expression
            i = 1;

        }
        else
        {
            // node[ 0 ... n ]  : Expression
            i = 0;
        }

        final int length = node.jjtGetNumChildren() - i;

        outputCode.append( "(");

        // i は上記で初期化済み
        for( ; i < length; i++ )
        {
            Node n = node.jjtGetChild( i );
            n.jjtAccept( this, data );
            if( i < length - 1 )
            {
                outputCode.append( "," );
            }
        }

        outputCode.append( ")" );

        return node;
    }

        /**
     * プリミティブ型宣言の実装
     */
    @Override
    public Object visit( ASTPrimitiveInititalizer node, Object data )
    {
/*
    VariableDeclaration
            -> ASTVariableInitializer
                -> [
                      ArrayInitializer
                    | UIInitializer
                    | PrimitiveInititalizer     //NOW
                        -> [Expression]
                ]
*/
        final Variable v = variableTable.search( ((SimpleNode)node.jjtGetParent().jjtGetParent()).symbol );

        if( node.jjtGetNumChildren() == 0 )
        {
            // 初期値代入なし
            return node;
        }

        final SimpleNode expr = (SimpleNode)node.jjtGetChild( 0 );

        // 初期値代入。畳み込みで有効な値が格納される
        if( expr.symbol.symbolType != SymbolType.Command )
        {
            outputCode.append( v.getVariableName() ).append( ":=" );
            expr.jjtAccept( this, data );
        }

        return node;
    }

//--------------------------------------------------------------------------
// コールバック本体
//--------------------------------------------------------------------------

    /**
     * コールバック定義
     */
    @Override
    public Object visit( ASTCallbackDeclaration node, Object data )
    {
        /*
            ASTCallbackDeclaration
                [ -> ASTCallbackArgumentList ]
        */

        outputCode.append( "on " ).append( node.symbol.getName() );

        if( node.jjtGetNumChildren() >= 2 )
        {
            // コールバック引数リストあり
            ASTCallbackArgumentList argList = (ASTCallbackArgumentList)node.jjtGetChild( 0 );
            int listSize                    = argList.args.size();
            outputCode.append( "(" );
            for( int i = 0; i < listSize; i++ )
            {
                String arg = argList.args.get( i );
                Variable v  = variableTable.search( arg );
                String name = v.getVariableName();

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

        return node;
    }


//--------------------------------------------------------------------------
// 式
//--------------------------------------------------------------------------

    /**
     * 条件式ノードのソースコード生成
     */
    public void appendConditionalNode( Node node, Object data, String operator )
    {
        node.jjtGetChild( 0 ).jjtAccept( this, data );
        outputCode.append( operator );
        node.jjtGetChild( 1 ).jjtAccept( this, data );
    }

    /**
     * 条件式 OR
     */
    @Override
    public Object visit( ASTConditionalOr node, Object data )
    {
        appendConditionalNode( node, data, " or " );
        return node;
    }

    /**
     * 条件式 AND
     */
    @Override
    public Object visit( ASTConditionalAnd node, Object data )
    {
        appendConditionalNode( node, data, " and " );
        return node;
    }

    /**
     * 論理積
     */
    @Override
    public Object visit( ASTInclusiveOr node, Object data )
    {
        appendBinaryOperatorNode( node, data, " .or. " );
        return node;
    }

    /**
     * 論理和
     */
    @Override
    public Object visit( ASTAnd node, Object data )
    {
        appendBinaryOperatorNode( node, data, " .and. " );
        return node;
    }

    /**
     * 比較 (=)
     */
    @Override
    public Object visit( ASTEqual node, Object data )
    {
        appendConditionalNode( node, data, "=" );
        return node;
    }

    /**
     * 比較 (#)
     */
    @Override
    public Object visit( ASTNotEqual node, Object data )
    {
        appendConditionalNode( node, data, "#" );
        return node;
    }

    /**
     * 不等号(<)
     */
    @Override
    public Object visit( ASTLT node, Object data )
    {
        appendConditionalNode( node, data, "<" );
        return node;
    }

    /**
     * 不等号(>)
     */
    @Override
    public Object visit( ASTGT node, Object data )
    {
        appendConditionalNode( node, data, ">" );
        return node;
    }

    /**
     * 不等号(<=)
     */
    @Override
    public Object visit( ASTLE node, Object data )
    {
        appendConditionalNode( node, data, "<=" );
        return node;
    }

    /**
     * 不等号(>=)
     */
    @Override
    public Object visit( ASTGE node, Object data )
    {
        appendConditionalNode( node, data, ">=" );
        return node;
    }

    /**
     * 2項演算ノードのソースコード生成
     */
    public void appendBinaryOperatorNode( SimpleNode node, Object data, String operator )
    {
        final SimpleNode exprL = (SimpleNode)node.jjtGetChild( 0 );
        final SimpleNode exprR = (SimpleNode)node.jjtGetChild( 1 );

        // 畳み込みが無理な場合は式を出力
        if( !node.symbol.isConstant() )
        {
            // 元のコードの演算子の優先度を担保するため、全て括弧で括る
            final boolean stradd = node.getId() == JJTSTRADD;
            if( !stradd )
            {
                outputCode.append( "(" );
            }

            exprL.jjtAccept( this, data );
            outputCode.append( operator );
            exprR.jjtAccept( this, data );

            if( !stradd )
            {
                outputCode.append( ")" );
            }
        }
        // 畳み込み済みの定数値を出力
        else
        {
            outputCode.append( node.symbol.value );
        }
    }

    /**
     * 加算(+)
     */
    @Override
    public Object visit( ASTAdd node, Object data )
    {
        appendBinaryOperatorNode( node, data, "+" );
        return node;
    }

    /**
     * 減算(-)
     */
    @Override
    public Object visit( ASTSub node, Object data )
    {
        appendBinaryOperatorNode( node, data, "-" );
        return node;
    }

    /**
     * 文字列連結
     */
    @Override
    public Object visit( ASTStrAdd node, Object data )
    {
        appendBinaryOperatorNode( node, data, "&" );
        return node;
    }

    /**
     * 乗算(*)
     */
    @Override
    public Object visit( ASTMul node, Object data )
    {
        appendBinaryOperatorNode( node, data, "*" );
        return node;
    }

    /**
     * 除算(/)
     */
    @Override
    public Object visit( ASTDiv node, Object data )
    {
        appendBinaryOperatorNode( node, data, "/" );
        return node;
    }

    /**
     * 余算(mod)
     */
    @Override
    public Object visit( ASTMod node, Object data )
    {
        appendBinaryOperatorNode( node, data, " mod " );
        return node;
    }

    /**
     * 単項演算ノードのソースコード生成
     */
    public void appendSingleOperatorNode( SimpleNode node, Object data, String operator )
    {
        // 畳み込みが無理な場合は式を出力
        if( !node.symbol.isConstant() )
        {
            SimpleNode expr = (SimpleNode)node.jjtGetChild( 0 );
            outputCode.append( operator );
            expr.jjtAccept( this, data );
        }
        // 畳み込み済みの定数値を出力
        else
        {
            outputCode.append( node.symbol.value );
        }
    }

    /**
     * 単項マイナス(-)
     */
    @Override
    public Object visit( ASTNeg node, Object data )
    {
        appendSingleOperatorNode( node, data, "-" );
        return node;
    }

    /**
     * 単項NOT(not)
     */
    @Override
    public Object visit( ASTNot node, Object data )
    {
        appendSingleOperatorNode( node, data, " .not. " );
        return node;
    }

    /**
     * 単項論理否定(not)
     */
    @Override
    public Object visit( ASTLogicalNot node, Object data )
    {
        appendSingleOperatorNode( node, data, "not " );
        return node;
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

        SimpleNode exprL = (SimpleNode)node.jjtGetChild( 0 );
        SimpleNode exprR = (SimpleNode)node.jjtGetChild( 1 );

        exprL.jjtAccept( this, data );
        outputCode.append( ":=" );
        exprR.jjtAccept( this, data );

        appendEOL();

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
        //--------------------------------------------------------------------------
        // 宣言済みかどうか
        //--------------------------------------------------------------------------
        Variable v = variableTable.search( node.symbol );

        // ユーザー定義定数なら展開
        if( v.isConstant() && !v.reserved )
        {
            outputCode.append( v.value );
        }
        else
        {
            outputCode.append( v.getVariableName() );
            // 配列型なら添字チェック
            if( v.isArray() )
            {
                // 上位ノードの型評価式用
                if( node.jjtGetNumChildren() > 0 )
                {
                    // 添え字がある
                    // 要素へのアクセスであるため、配列ビットフラグを外したプリミティブ型として扱う
                    node.jjtGetChild( 0 ).jjtAccept( this, node );
                }
                else
                {
                    // 添え字が無い
                    // 配列変数をコマンドの引数に渡すケース
                    // 配列型としてそのまま扱う
                }
            }
            return node;
        }
        return node;
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

        Node expr = node.jjtGetChild( 0 );

        outputCode.append( "[" );
        expr.jjtAccept( this, data );
        outputCode.append( "]" );

        return node;
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
        Command cmd = commandTable.search( node.symbol );

        if( cmd == null )
        {
            // ドキュメントに記載のない隠しコマンドの可能性
            // 存在するコマンドとして扱う
            cmd = new Command( node );
        }

        outputCode.append( cmd.getName() ).append( "(" );
        node.childrenAccept( this, data );
        outputCode.append( ")" );

        // このコマンド呼び出しが上位ノードのコマンド引数
        // としてコールされている場合は改行出来ないためチェックをしている
        if( node.jjtGetParent().getId() == JJTBLOCK )
        {
            appendEOL();
        }

        return node;
    }

    /**
     * コマンド引数
     */
    @Override
    public Object visit( ASTCommandArgumentList node, Object data )
    {
        final int childrenNum = node.jjtGetNumChildren();

        //--------------------------------------------------------------------------
        // 引数の出力
        //--------------------------------------------------------------------------
        for( int i = 0; i < childrenNum; i++ )
        {
            node.jjtGetChild( i ).jjtAccept( this, data );
            if( i < childrenNum - 1 )
            {
                outputCode.append( "," );
            }
        }
        return node;
    }

//--------------------------------------------------------------------------
// ユーザー定義関数
//--------------------------------------------------------------------------

    /**
     * ユーザー定義関数宣言
     */
    @Override
    public Object visit( ASTUserFunctionDeclaration node, Object data )
    {
        Node block = node.jjtGetChild( 0 );
        UserFunction func = userFunctionTable.search( node.symbol.getName() );

        if( func.referenced )
        {
            outputCode.append( "function " ).append( func.getName() );
            appendEOL();
            block.jjtAccept( this, data );
            outputCode.append( "end function " );
            appendEOL();
        }

        return node;
    }

    /**
     * ユーザー定義関数呼び出し
     */
    @Override
    public Object visit( ASTCallUserFunctionStatement node, Object data )
    {
        UserFunction func = userFunctionTable.search( node.symbol );
        outputCode.append( "call " ).append( func.getName() );
        appendEOL();
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
            -> <expr>       [0]
            -> <block>      [1]
        | else
            -> <block>      [2]
*/
        //--------------------------------------------------------------------------
        // ifスコープ
        //--------------------------------------------------------------------------
        {
            Node cond = node.jjtGetChild( 0 );
            Node block = node.jjtGetChild( 1 );

            outputCode.append( "if" ).append( "(" );
            // if( <cond> )
            cond.jjtAccept( this, data );
            outputCode.append( ")" );
            appendEOL();

            block.jjtAccept( this, data );
        }
        //--------------------------------------------------------------------------
        // else スコープ
        //--------------------------------------------------------------------------
        if( node.jjtGetNumChildren() > 2 )
        {
            Node block = node.jjtGetChild( 2 );
            outputCode.append( "else" );
            appendEOL();

            block.jjtAccept( this, data );
        }

        outputCode.append( "end if" );
        appendEOL();

        return node;
    }

    /**
     * select~case の評価
     */
    @Override
    public Object visit( ASTSelectStatement node, Object data )
    {
/*

        select
            -> <expr>
            -> <case>
                -> <casecond>
                -> [ to <expr> ]
                -> <block>
            -> <case>
                -> <casecond>
                -> [ to <expr> ]
                -> <block>
            :
            :
*/

        Node cond = node.jjtGetChild( 0 );

        outputCode.append( "select" ).append( "(" );
        cond.jjtAccept( this, data );
        outputCode.append( ")" );
        appendEOL();

        //--------------------------------------------------------------------------
        // case: 整数の定数または定数宣言した変数が有効
        //--------------------------------------------------------------------------
        for( int i = 1; i < node.jjtGetNumChildren(); i++ )
        {
            SimpleNode caseNode  = (SimpleNode)node.jjtGetChild( i );
            SimpleNode caseCond1 = (SimpleNode)caseNode.jjtGetChild( 0 );
            SimpleNode caseCond2 = null;
            SimpleNode blockNode = (SimpleNode)caseNode.jjtGetChild( caseNode.jjtGetNumChildren() - 1 );

            // to <expr>
            if( caseNode.jjtGetNumChildren() >= 2 )
            {
                SimpleNode n = (SimpleNode)caseNode.jjtGetChild( 1 );
                if( n.getId() == JJTCASECONDITION )
                {
                    // to
                    caseCond2 = (SimpleNode)caseNode.jjtGetChild( 1 );
                }
            }

            // case
            outputCode.append( "case " );
            caseCond1.jjtAccept( this, data );

            // to
            if( caseCond2 != null )
            {
                outputCode.append( " to " );
                caseCond2.jjtAccept( this, data );
            }
            appendEOL();

            // block statement
            blockNode.jjtAccept( this , data );
        }

        outputCode.append( "end select" );
        appendEOL();

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
        Node n = node.jjtGetChild( 0 );
        n.jjtAccept( this, data );
        return n;
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
        Node cond = node.jjtGetChild( 0 );
        Node block = node.jjtGetChild( 1 );

        outputCode.append( "while" ).append( "(" );

        // while( <cond> )
        cond.jjtAccept( this, data );
        outputCode.append( ")" );
        appendEOL();

        block.jjtAccept( this, data );

        outputCode.append( "end while" );
        appendEOL();

        return node;
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
     * プリプロセッサ def/undef のコード生成
     */
    protected void appendPreprocessorDefine( SimpleNode node, String keyword )
    {
        outputCode.append( keyword ).append( "(" ).append( node.symbol.getName() ).append( ")" );
        appendEOL();
    }

    /**
     * プリプロセッサシンボル定義
     */
    @Override
    public Object visit( ASTPreProcessorDefine node, Object data )
    {
        appendPreprocessorDefine( node, "SET_CONDITION" );
        return node;
    }

    /**
     * プリプロセッサシンボル破棄
     */
    @Override
    public Object visit( ASTPreProcessorUnDefine node, Object data )
    {
        appendPreprocessorDefine( node, "RESET_CONDITION" );
        return node;
    }

    /**
     * プリプロセッサ ifdef/ifndef のコード生成
     */
    protected void appendPreprocessorCondition( SimpleNode node, String keyword, Object jjtVisitorData )
    {
        Node block = null;
        if( node.jjtGetNumChildren() > 0 )
        {
            block = node.jjtGetChild( 0 );
        }
        outputCode.append( keyword ).append( "(" ).append( node.symbol.getName() ).append( ")" );
        if( block != null )
        {
            block.jjtAccept( this, jjtVisitorData );
        }
        outputCode.append( "END_USE_CODE" );
        appendEOL();
    }

    /**
     * ifdef
     */
    @Override
    public Object visit( ASTPreProcessorIfDefined node, Object data )
    {
        appendPreprocessorCondition( node, "USE_CODE_IF", data );
        return node;
    }

    /**
     * ifndef
     */
    @Override
    public Object visit( ASTPreProcessorIfUnDefined node, Object data )
    {
        appendPreprocessorCondition( node, "USE_CODE_IF_NOT", data );
        return node;
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
