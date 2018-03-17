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
import net.rkoubou.kspparser.javacc.generated.ASTAnd;
import net.rkoubou.kspparser.javacc.generated.ASTArrayIndex;
import net.rkoubou.kspparser.javacc.generated.ASTAssignment;
import net.rkoubou.kspparser.javacc.generated.ASTBlock;
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
        return node.jjtGetChild( 0 ).jjtAccept( this, variableTable.search( node.symbol ) );
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
        final Variable v = variableTable.search( ((Variable)data) );

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
            v.value = v.getVariableName();
            outputCode.append( v.getVariableName() );
        }
        appendEOL();
        return null;
    }

    /**
     * 配列型宣言の実装
     */
    protected void declareArrayVariableImpl( ASTVariableDeclarator node, Variable v, Object jjtVisitorData, boolean forceSkipInitializer )
    {
/*
            -> VariableDeclarator
                -> <arrayindex> -> <expr> : [0]
                    -> [ := VariableInitializer ] : [1]
                        -> ArrayInitializer : [1][0]
                            -> ( <expr> (, <expr>)* ) : [1][1]
*/
        outputCode.append( v.getVariableName() )
        .append( "[" )
        .append( v.arraySize )
        .append( "]" );

        //--------------------------------------------------------------------------
        // 初期値代入
        //--------------------------------------------------------------------------
        if( forceSkipInitializer || node.jjtGetNumChildren() != 2 )
        {
            // 初期値代入なし
            return;
        }

        outputCode.append( ":=(");

        final SimpleNode initializer = (SimpleNode)node.jjtGetChild( 1 ).jjtGetChild( 0 );
        final int length             = initializer.jjtGetNumChildren();
        for( int i = 0; i < length; i++ )
        {
            initializer.jjtGetChild( i ).jjtAccept( this, jjtVisitorData );
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
    protected void declareUIVariableImpl( ASTVariableDeclarator node, Variable v, Object jjtVisitorData )
    {
/*
            -> VariableDeclarator
                -> [ := UIInitializer ]
                    -> (expr)+
*/
        UIType uiType = uiTypeTable.search( v.uiTypeName );
        outputCode.append( uiType.name ).append( " " );

        //--------------------------------------------------------------------------
        // ui_#### が配列型の場合、要素数宣言までのコード生成
        //--------------------------------------------------------------------------
        if( Variable.isArray( uiType.uiValueType ) )
        {
            declareArrayVariableImpl( node, v, jjtVisitorData, true );
        }
        else
        {
            outputCode.append( v.getVariableName() );
        }

        //--------------------------------------------------------------------------
        // 初期値代入
        //--------------------------------------------------------------------------

        if( node.jjtGetNumChildren() == 0 )
        {
            // 初期値代入なし
            return;
        }

        Node initializer = node.jjtGetChild( 0 ).jjtGetChild( 0 );
        if( node.jjtGetNumChildren() >= 2 )
        {
            //[0] == ArrayInitialier なので、初期悪式ノードの参照に切り替える
            initializer = node.jjtGetChild( 1 ).jjtGetChild( 0 );
        }

        final int length = initializer.jjtGetNumChildren();

        if( length == 0 )
        {
            // 配列型変数
            // 初期値代入なし
            return;
        }

        outputCode.append( "(");

        for( int i = 0; i < length; i++ )
        {
            Node n = initializer.jjtGetChild( i );
            n.jjtAccept( this, jjtVisitorData );
            if( i < length - 1 )
            {
                outputCode.append( "," );
            }
        }
        outputCode.append( ")" );
    }

    /**
     * プリミティブ型宣言の実装
     */
    protected void declarePrimitiveVariableImpl( ASTVariableDeclarator node, Variable v, Object jjtVisitorData )
    {
/*
            -> VariableDeclarator
                -> [ := <VariableInitializer> ]
                    -> <expr>
*/
        if( node.jjtGetNumChildren() == 0 )
        {
            // 初期値代入なし
            return;
        }

        final SimpleNode initializer = (SimpleNode)node.jjtGetChild( 0 );
        final SimpleNode expr        = (SimpleNode)initializer.jjtGetChild( 0 );

        // 初期値代入。畳み込みで有効な値が格納される
        if( expr.symbol.symbolType != SymbolType.Command )
        {
            outputCode.append( v.getVariableName() ).append( ":=" );
            expr.jjtAccept( this, jjtVisitorData );
        }
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
        Callback callback = callbackTable.search( node.symbol );
        outputCode.append( "on " ).append( node.symbol.getName() );

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
                String name = SymbolDefinition.toKSPTypeCharacter( SymbolDefinition.getKSPTypeFromVariableName( a.getName() )  ) +
                              ShortSymbolGenerator.getSymbolFromOrgName( a.getName() );

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
        // 畳み込みが無理な場合は式を出力
        if( !node.symbol.isConstant() )
        {
            SimpleNode exprL = (SimpleNode)node.jjtGetChild( 0 );
            SimpleNode exprR = (SimpleNode)node.jjtGetChild( 1 );

            // 元のコードの演算子の優先度を担保するため、全て括弧で括る
            outputCode.append( "(" );

            exprL.jjtAccept( this, data );
            outputCode.append( operator );
            exprR.jjtAccept( this, data );

            outputCode.append( ")" );
        }
        // 畳み込み済みの定数値を出力
        else if( node.symbol.state != SymbolState.LOADING )
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

        SimpleNode exprL      = (SimpleNode)node.jjtGetChild( 0 );
        SimpleNode exprR      = (SimpleNode)node.jjtGetChild( 1 );
        SymbolDefinition symL = exprL.symbol;

        Variable variable = variableTable.search( symL );
        outputCode.append( variable.getVariableName() )
        .append( ":=" );
//System.out.println( ":=" + exprR );
        exprR.jjtAccept( this, data );

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
        Variable v = variableTable.search( node.symbol );

        // 変数へのアクセスが確定したので、戻り値に変数のシンボル情報をコピー
        SymbolDefinition.copy( v, ret.symbol );

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
        final Command cmd = commandTable.search( node.symbol );

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
            if( !cmd.availableCallbackList.containsKey( callback.symbol.getName() ) )
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
                Variable userVar        = variableTable.search( symbol );

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
                        Command retCommand = commandTable.search( callCmd.symbol );

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
        UserFunction f = userFunctionTable.search( node.symbol );
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
TODO プリプロセッサ、コマンド・ユーザー関数コールから再開
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
        if( preProcessorSymbolTable.search( node.symbol ) == null )
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
