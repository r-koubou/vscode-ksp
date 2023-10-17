/* =========================================================================

    SemanticAnalyzer.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.util.ArrayList;

import net.rkoubou.kspparser.analyzer.SymbolDefinition.SymbolType;
import net.rkoubou.kspparser.javacc.generated.ASTArrayIndex;
import net.rkoubou.kspparser.javacc.generated.ASTArrayInitializer;
import net.rkoubou.kspparser.javacc.generated.ASTAssignment;
import net.rkoubou.kspparser.javacc.generated.ASTBlock;
import net.rkoubou.kspparser.javacc.generated.ASTCallCommand;
import net.rkoubou.kspparser.javacc.generated.ASTCallUserFunctionStatement;
import net.rkoubou.kspparser.javacc.generated.ASTCallbackDeclaration;
import net.rkoubou.kspparser.javacc.generated.ASTCaseCondition;
import net.rkoubou.kspparser.javacc.generated.ASTCommandArgumentList;
import net.rkoubou.kspparser.javacc.generated.ASTIfStatement;
import net.rkoubou.kspparser.javacc.generated.ASTPrimitiveInititalizer;
import net.rkoubou.kspparser.javacc.generated.ASTRefVariable;
import net.rkoubou.kspparser.javacc.generated.ASTSelectStatement;
import net.rkoubou.kspparser.javacc.generated.ASTUserFunctionDeclaration;
import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclaration;
import net.rkoubou.kspparser.javacc.generated.ASTVariableInitializer;
import net.rkoubou.kspparser.javacc.generated.ASTWhileStatement;
import net.rkoubou.kspparser.javacc.generated.SimpleNode;
import net.rkoubou.kspparser.options.CommandlineOptions;

/**
 * 意味解析実行クラス
 */
public class SemanticAnalyzer extends BasicEvaluationAnalyzerTemplate
{

    // 局所的にのみ使用することを前提としたワークエリア
    private final SymbolDefinition tempSymbol = new SymbolDefinition();

    /**
     * ctor
     */
    public SemanticAnalyzer( SymbolCollector symbolCollector )
    {
        super( symbolCollector.astRootNode, symbolCollector );
    }

    /**
     * 意味解析の実行
     */
    @Override
    public void analyze() throws Exception
    {
        astRootNode.jjtAccept( this, null );

        //--------------------------------------------------------------------------
        // 解析後の未使用・未初期化シンボルの洗い出し
        //--------------------------------------------------------------------------

        if( CommandlineOptions.options.unused )
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
                if( CommandlineOptions.options.strict && v.state == SymbolState.UNLOADED && !v.isUIVariable() )
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
        if( CommandlineOptions.options.strict )
        {
            for( SymbolDefinition s : variableTable.toArray() )
            {
                if( REGEX_NUMERIC_PREFIX.matcher( s.getName() ).find() )
                {
                    MessageManager.printlnW( MessageManager.PROPERTY_WARNING_SEMANTIC_INFO_VARNAME, s );
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

        // 初期化式なし
        if( node.jjtGetNumChildren() == 0 )
        {
            final Variable v    = variableTable.search( node.symbol );
            final UIType uiType = uiTypeTable.search( v.uiTypeName );

            // const 修飾子がついているかどうかの検証
            if( v.isConstant() )
            {
                // 変数宣言時に初期化式が必須
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_REQUIRED_INITIALIZER, v );
                AnalyzeErrorCounter.e();
            }
            // UI型変数の初期値代入必須の検証
            else if( v.isUIVariable() )
            {
                if( uiType == null )
                {
                    // KSP（data/symbol/uitypes.txt）で未定義のUIタイプ
                    // シンボル収集フェーズで警告出力済みなので何もしない
                    v.state = SymbolState.INITIALIZED;
                }
                else if( uiType.initializerRequired )
                {
                    // 変数宣言時に初期化式が必須
                    MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_REQUIRED_INITIALIZER, v );
                    AnalyzeErrorCounter.e();
                }
            }

            return node;
        }
        node.childrenAccept( this, node );
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
        // 宣言のみ
        if( node.jjtGetNumChildren() == 0 )
        {
            SimpleNode parent = (SimpleNode)node.jjtGetParent();
            // 定数宣言している場合は初期値代入が必須
            if( parent.symbol.isConstant() )
            {
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_REQUIRED_INITIALIZER, parent.symbol ) ;
                AnalyzeErrorCounter.e();
            }
            return node;
        }

        node.childrenAccept( this, data );
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

        if( node.hasAssign )
        {
            if( v.isUIVariable() )
            {
                /*
                    プリミティブ型変数なのに ui_#### 修飾子がある状態、かつ := がある状態
                    文保解析フェーズでは解決不可能な言語仕様のためここで判定を行っている

                    e.g.:
                    declare ui_label %a := ( 0, 0 )
                */
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SYNTAX, v );
                AnalyzeErrorCounter.e();
                return node;
            }
        }
        else
        {
            if( !v.isUIVariable() )
            {
                /*
                    ui_#### 修飾子が無い、且つ := が無く、UI初期化子である状態
                    文法解析フェーズでは解決不可能な言語仕様のためここで判定を行っている

                    e.g.:
                    declare  %a( 0, 0 )
                */
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SYNTAX, v );
                AnalyzeErrorCounter.e();
                return node;
            }
            uiInitializerImpl( node, v, data );
            return node;
        }

        if( node.jjtGetNumChildren() == 0 )
        {
            if( v.isConstant() )
            {
                // 定数宣言している場合は初期値代入が必須
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_REQUIRED_INITIALIZER, v ) ;
                AnalyzeErrorCounter.e();
                return node;
            }
            // 初期値代入なし
            return node;
        }

        final SimpleNode expr = (SimpleNode)node.jjtGetChild( 0 ).jjtAccept( this, data );
        SymbolDefinition eval = (SymbolDefinition)expr.symbol;

        // const 宣言時にコマンドの戻り値は代入できない（戻り値不定のため、動作保証が出来ない可能性が発生する）
        if( v.isConstant() )
        {
            if( node.hasNode( null, SimpleNode.HasNodeIdCondition, JJTCALLCOMMAND ) )
            {
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_NOCONSTANT_INITIALIZER, v );
                AnalyzeErrorCounter.e();
                return node;
            }
        }

        // 型の不一致
        if( ( v.type & eval.type ) == 0 )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_INITIALIZER_TYPE, v, SymbolDefinition.getTypeName( eval.type ), SymbolDefinition.getTypeName( v.type ) ) ;
            AnalyzeErrorCounter.e();
            return node;
        }

        // 文字列型は初期値代入不可
        if( v.isString() )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_STRING_INITIALIZER, v );
            AnalyzeErrorCounter.e();
            return node;
        }

        // 初期値代入。畳み込みで有効な値が格納される
        if( expr.symbol.symbolType != SymbolType.Command )
        {
            Object value = null;
            if( v.isInt() )
            {
                value = EvaluationUtility.evalConstantIntValue( expr, 0, variableTable );
            }
            else if( v.isReal() )
            {
                value = EvaluationUtility.evalConstantRealValue( expr, 0, variableTable );
            }
            else if( v.isString() )
            {
                // 文字列は初期値代入不可
                value = null;
            }
            v.setValue( value );
        }
        v.state = SymbolState.INITIALIZED;
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
        final Variable v = variableTable.search( ((SimpleNode)node.jjtGetParent().jjtGetParent()).symbol );
        if( node.hasAssign )
        {
            if( v.isUIVariable() )
            {
                /*
                    配列型変数なのに ui_#### 修飾子がある状態、かつ := で初期値代入をしている
                    文保解析フェーズでは解決不可能な言語仕様のためここで判定を行っている

                    e.g.:
                    declare ui_table %t[ 3 ] := ( 0, 0, 0 )
                */
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SYNTAX, v );
                AnalyzeErrorCounter.e();
                return node;
            }
            arrayInitializerImpl( node, data, false );
        }
        else
        {
            if( v.isUIVariable() )
            {
                uiInitializerImpl( node, v, data );
            }
            else
            {
                arrayInitializerImpl( node, data, false );
            }
        }
        return node;
    }

    /**
     * 配列型宣言の実装(詳細).
     * UI変数かつ配列型のケースもあるので外部化
     */
    protected boolean arrayInitializerImpl( SimpleNode node, Object data, boolean forceSkipInitializer )
    {
/*
    VariableDeclaration
            -> ASTVariableInitializer
                -> [
                        ArrayInitializer        //NOW
                            -> ArrayIndex
                            -> Expression
                            -> (,Expression)*
                    | PrimitiveInititalizer
                ]
*/

        final Variable v = variableTable.search( ((SimpleNode)node.jjtGetParent().jjtGetParent()).symbol );
        boolean result   = true;

        //--------------------------------------------------------------------------
        // 型チェック
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

            if( !SymbolDefinition.isInt( n.symbol.type ) )
            {
                // 要素数の型が整数以外
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_ARRAYSIZE, v );
                AnalyzeErrorCounter.e();
                return false;
            }

            size = EvaluationUtility.evalConstantIntValue( n, 0, variableTable );

            if( size == null || size <= 0 )
            {
                // 要素数が不明、または 0 以下
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_ARRAYSIZE, v );
                AnalyzeErrorCounter.e();
                return false;
            }

            v.arraySize += size;

        }

        if( v.arraySize > KSPLanguageLimitations.MAX_KSP_ARRAY_SIZE )
        {
            // 要素数が上限を超えた
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_MAXARRAYSIZE, v, String.valueOf( KSPLanguageLimitations.MAX_KSP_ARRAY_SIZE ) );
            AnalyzeErrorCounter.e();
            return false;
        }

        //--------------------------------------------------------------------------
        // 初期値代入
        //--------------------------------------------------------------------------
        if( forceSkipInitializer || node.jjtGetNumChildren() == 1 )
        {
            // 初期値代入なし
            v.state = SymbolState.UNLOADED;
            return true;
        }

        if( v.isString() )
        {
            // 文字列配列型に初期値代入は出来ない
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_STRING_INITIALIZER, v );
            AnalyzeErrorCounter.e();
            return false;
        }

        for( int i = 1; i < node.jjtGetNumChildren(); i++ )
        {
            final SimpleNode expr = (SimpleNode)node.jjtGetChild( i ).jjtAccept( this, data );
            SymbolDefinition eval = (SymbolDefinition) expr.symbol;

            if( ( v.type & eval.type ) == 0 )
            {
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_ARRAYINITILIZER, v, String.valueOf( i - 1 ) ); // -1: zero origin
                AnalyzeErrorCounter.e();
                result = false;
            }
        }
        v.state = SymbolState.INITIALIZED;
        return result;
    }

    /**
     * UI型宣言の実装
     */
    protected void uiInitializerImpl( SimpleNode initializer, Variable v, Object jjtVisitorData )
    {
/*
    VariableDeclaration
            -> ASTVariableInitializer
                -> [
                        ArrayInitializer
                            -> ArrayIndex
                            -> Expression
                            -> (,Expression)*
                    | UIInitializer             //NOW
                            -> [ ArrayIndex ]
                            -> Expressiom
                            -> (,Expression)*
                    | PrimitiveInititalizer
                ]
*/
        final UIType uiType = uiTypeTable.search( v.uiTypeName );

        if( uiType == null )
        {
            // KSP（data/symbol/uitypes.txt）で未定義のUIタイプ
            // シンボル収集フェーズで警告出力済みなので何もしない
            v.state = SymbolState.INITIALIZED;
            return;
        }

        //--------------------------------------------------------------------------
        // ui_#### が求める型と変数の型のチェック
        //--------------------------------------------------------------------------
        if( v.type != uiType.uiValueType )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_UITYPE, v, SymbolDefinition.getTypeName( uiType.uiValueType ), uiType.name );
            AnalyzeErrorCounter.e();
            return;
        }

        //--------------------------------------------------------------------------
        // const 指定のチェック
        //--------------------------------------------------------------------------
        if( v.isConstant() )
        {
            // ui_#### const 修飾子を付与できない
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_DECLARE_CONST, v );
            AnalyzeErrorCounter.e();
            return;
        }

        //--------------------------------------------------------------------------
        // ui_#### が配列型の場合、要素数宣言のチェック
        //--------------------------------------------------------------------------
        if( SymbolDefinition.isArray( uiType.uiValueType ) )
        {
            if( !arrayInitializerImpl( initializer, jjtVisitorData, true ) )
            {
                return;
            }
        }

        //--------------------------------------------------------------------------
        // 初期値代入式チェック
        //--------------------------------------------------------------------------
        if( !uiType.initializerRequired && initializer.jjtGetNumChildren() > 0)
        {
            // TODO: 仮コード
            // 初期化不要なのに初期化式がある時にエラーとする場合
            // 今後の仕様であってもなくても良い場合の可能性もあるので今はエラーとしない
            // MessageManager.printlnE( /* set error message */ );
            // return;
        }
        if( !uiType.initializerRequired )
        {
            // 初期化不要
            v.state = SymbolState.INITIALIZED;
            return;
        }
        if( initializer.jjtGetNumChildren() < 1 )
        {
            // 配列型なら子は2以上 (ArrayIndex, Expression, ...)
            // そうでない場合なら子1は以上(Expression, ...)
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_REQUIRED_INITIALIZER, v );
            AnalyzeErrorCounter.e();
            return;
        }

        // for のカウンタ初期値の設定
        int i;
        if( initializer.jjtGetChild( 0 ).getId() == JJTARRAYINDEX )
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

        // UI初期化式の引数チェック
        if( initializer.jjtGetNumChildren() - i != uiType.initilzerTypeList.length )
        {
            // 引数の数が一致していない
            String cnt = String.valueOf( initializer.jjtGetNumChildren() - i );
            String req = String.valueOf( uiType.initilzerTypeList.length );
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_UIINITIALIZER_COUNT, v, uiType.name, cnt, req );
            AnalyzeErrorCounter.e();
            return;
        }

        // i は上記で初期化済み
        for( ; i < initializer.jjtGetNumChildren(); i++ )
        {
            boolean found = false;
            SimpleNode n  = (SimpleNode)initializer.jjtGetChild( i ).jjtAccept( this, jjtVisitorData );
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
                        Variable var = variableTable.search( n.symbol );
                        if( var == null )
                        {
                            // コマンドの戻り値の可能性
                            Command cmd = commandTable.search( n.symbol );
                            if( cmd == null )
                            {
                                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_NOT_DECLARED, n.symbol );
                                AnalyzeErrorCounter.e();
                                break;
                            }
                            if( cmd.returnType.contains( t ) )
                            {
                                found = true;
                                break SEARCH;
                            }
                            else
                            {
                                // 戻り値と要求される型の不一致
                                break SEARCH;
                            }
                        }
                        if( var.type == t )
                        {
                            found = true;
                            var.referenced = true;
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

            } //~for( int t : uiType.initilzerTypeList )

            if( !found )
            {
                // イニシャライザ: 型の不一致
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_UIINITIALIZER_TYPE, v, String.valueOf( i ), SymbolDefinition.getTypeName( argT ) );
                AnalyzeErrorCounter.e();
                break;
            }
        }
        // コールバックから参照されるので意図的に参照フラグON
        v.state      = SymbolState.LOADED;
        v.referenced = true;
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

        variable = variableTable.search( symL );
        if( variable == null )
        {
            // exprL 評価内で変数が見つけられなかった
            return exprL;
        }
        variable.referenced = true;
        exprLType = variable.type;
        exprRType = exprR.symbol.type;

        if( variable.isConstant() )
        {
            SymbolDefinition.copy( exprR.symbol, tempSymbol );
            tempSymbol.setName( variable.getName() );
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_ASSIGN_CONSTVARIABLE, tempSymbol );
            AnalyzeErrorCounter.e();
            return exprL;
        }
        // コマンドコールの戻り値が複数の型を持つなどで暗黙の型変換を要する場合
        // 代入先の変数に合わせる。暗黙の型変換が不可能な場合は、代替としてVOIDを入れる
        if( SymbolDefinition.hasMultipleType( exprRType ) )
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
            // 代入先が文字列型なら暗黙の型変換が可能
            // 文字列型以外なら型の不一致 or 条件式は代入不可
            if( !symL.isString() || symR.isBoolean() )
            {
                String vType = SymbolDefinition.getTypeName( SymbolDefinition.getPrimitiveType( exprLType ) );
                String aType = SymbolDefinition.getTypeName( exprRType );
                SymbolDefinition.copy( symR, tempSymbol );
                tempSymbol.setName( variable.getName() );

                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_ASSIGN_TYPE_NOTCOMPATIBLE, tempSymbol, vType, aType );
                AnalyzeErrorCounter.e();
                return exprL;
            }
        }
        variable.state = SymbolState.LOADED;
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
        SimpleNode ret = createEvalNode( node, JJTREFVARIABLE );

        //--------------------------------------------------------------------------
        // 宣言済みかどうか
        //--------------------------------------------------------------------------
        Variable v = variableTable.search( node.symbol );
        if( v == null )
        {
            // 宣言されていない変数
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_NOT_DECLARED, node.symbol );
            AnalyzeErrorCounter.e();
            return ret;
        }
        // 変数へのアクセスが確定したので、AST、戻り値に変数のシンボル情報をコピー
        SymbolDefinition.copy( v, node.symbol, false );
        SymbolDefinition.copy( v, ret.symbol, false );

        if( node.jjtGetParent() != null && node.jjtGetParent().getId() == JJTASSIGNMENT )
        {
            // これから代入される予定の変数
            v.state = SymbolState.LOADING;
        }

        // 配列型なら親ノードに応じて添字の有無検証
        if( node.isNecessaryValidArraySubscribe() && !EvaluationUtility.validArraySubscript( node, false ) )
        {
            // 添字が必須なのに添字がない
            // 変数ノードの場合、宣言部が行番号にあたるので、出現箇所の出現行番号を指定する
            SymbolDefinition sym = new SymbolDefinition( v );
            sym.position.copy( node.symbol.position );

            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_ARRAYSUBSCRIPT, sym );
            AnalyzeErrorCounter.e();
            return ret;
        }

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
            v.referenced = true;
            v.referenceCount++;
            v.state = SymbolState.LOADED;
            return ret;
        }
        // 配列型じゃないのに添え字がある
        else if( node.jjtGetNumChildren() > 0 )
        {
            SymbolDefinition.copy( node.symbol, tempSymbol );
            tempSymbol.setName( v.getName() );

            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_NOTARRAY, tempSymbol );
            AnalyzeErrorCounter.e();
        }

        // 上位ノードの型評価式用
        ret.symbol.type = v.getPrimitiveType();
        ret.symbol.reserved = v.reserved;
        v.referenced = true;
        v.state = SymbolState.LOADED;
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
            parent:<RefVariable>
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
        if( !SymbolDefinition.isInt( sym.type ) )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_ARRAY_ELEMENT_INTONLY, expr.symbol );
            AnalyzeErrorCounter.e();
        }
        // インデックスが定数値で配列サイズ外アクセス（Out of Bounds）
        if( sym.isConstant() && !EvaluationUtility.isInVariableDeclaration( node ) )
        {
            Variable v = variableTable.search( parent.symbol );
            if( v != null && sym.value != null )
            {
                int index = Integer.parseInt( sym.value.toString() );
                int size  = v.arraySize;
                if( ( !v.reserved && ( index < 0 || index >= size ) ) || // ユーザー変数は配列サイズが分かっている
                    ( v.reserved && index < 0 ) )                        // ビルトイン変数は配列サイズが不定なのでネガティブチェックのみ
                {
                    // 行番号を合わせるため、行番号をマージした一時シンボルを生成
                    SymbolDefinition s = new SymbolDefinition( v );
                    s.position.copy( parent.symbol.position );

                    MessageManager.printlnE( MessageManager.PROPERTY_ERROR_SEMANTIC_VARIABLE_INVALID_ARRAYOUTOFBOUNDS, s, String.valueOf( size ), String.valueOf( index ) );
                    AnalyzeErrorCounter.e();
                }
            }
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
        boolean undocumentedCmd = false;

        // 上位ノードの型評価式用
        SimpleNode ret = createEvalNode( node, JJTREFVARIABLE );
        ret.symbol.symbolType = SymbolType.Command;

        if( cmd == null )
        {
            // シンボルテーブルに定義なし
            // ドキュメントに記載のない隠しコマンドの可能性
            // エラーにせず、警告に留める
            // 戻り値不定のため、全てを許可する
            ret.symbol.type = TYPE_MULTIPLE;
            MessageManager.printlnW( MessageManager.PROPERTY_WARNING_SEMANTIC_COMMAND_UNKNOWN, node.symbol );
            AnalyzeErrorCounter.w();
            return ret;
        }

        // 未知のコマンド（戻り値Xタイプ）はチェックをスキップ
        undocumentedCmd = cmd.unknownCommand();
        if( undocumentedCmd )
        {
            // シンボルテーブルに定義はあるけど
            // ドキュメントに記載のない、引数・戻り値不明の隠しコマンド
            // エラーにせず、警告に留める
            // 戻り値不定のため、全てを許可する
            ret.symbol.type = TYPE_MULTIPLE;
            MessageManager.printlnW( MessageManager.PROPERTY_WARN_COMMAND_UNKNOWN, node.symbol );
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
            if( valid == false && cmd.argList.size() == 1 )
            {
SEARCH:
                for( CommandArgument a1 : cmd.argList )
                {
                    for( Argument a2 : a1.arguments )
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
                int accessFlag          = symbol.accessFlag;

                // 評価式が変数だった場合のための変数情報への参照
                Variable userVar        = variableTable.search( symbol );

                // 引数毎に複数のデータ型が許容される仕様のため照合
                for( Argument arg : argList.get( i ).arguments )
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
                            // KONTAKT 6.2 で参照渡しの概念が入った
                            // コマンドが定義している型が CONSTANT 属性を持たない
                            // かつ引数に渡される変数がconstのときはエラー扱い
                            //
                            // 例：detect_rms( id, result )
                            // detect_rms( 0, ~real_val ) OK
                            // detect_rms( 0, ~const_real ) NG constな変数に結果を書き込めない
                            if( !arg.isConstant() )
                            {
                                if( !symbol.isConstant() )
                                {
                                    valid = true;
                                    break;
                                }
                            }
                            else
                            {
                                // 引数の型がconstでもOK
                                valid = true;
                                break;
                            }
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
                            // KONTAKT 6.2 で参照渡しの概念が入った
                            // コマンドが定義している型が CONSTANT 属性を持たないときはエラー扱い
                            //
                            // 例：detect_rms( id, result )
                            // detect_rms( 0, ~real_val ) OK
                            // detect_rms( 0, 0.0 ) NG
                            if( !arg.isConstant() )
                            {
                                if( !SymbolDefinition.isConstant( accessFlag ) )
                                {
                                    valid = true;
                                    break;
                                }
                            }
                            else
                            {
                                // 引数の型がconstでもOK
                                valid = true;
                                break;
                            }
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
     * ユーザー定義関数宣言
     */
    @Override
    public Object visit( ASTUserFunctionDeclaration node, Object data )
    {
        node.childrenAccept( this, data );
        return node;
    }

    /**
     * ユーザー定義関数呼び出し
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
        f.referenceCount++;
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
            if( !SymbolDefinition.isBoolean( cond.symbol.type ) )
            {
                MessageManager.printlnE(
                    MessageManager.PROPERTY_ERROR_SEMANTIC_CONDITION_INVALID,
                    cond.symbol,
                    SymbolDefinition.getTypeName( TYPE_BOOL )
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
            if( !SymbolDefinition.isInt( cond.symbol.type ) || SymbolDefinition.isArray( cond.symbol.type )  )
            {
                MessageManager.printlnE(
                    MessageManager.PROPERTY_ERROR_SEMANTIC_CONDITION_INVALID,
                    cond.symbol,
                    SymbolDefinition.getTypeName( TYPE_INT )
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
            if( !SymbolDefinition.isBoolean( cond.symbol.type ) )
            {
                MessageManager.printlnE(
                    MessageManager.PROPERTY_ERROR_SEMANTIC_CONDITION_INVALID,
                    cond.symbol,
                    SymbolDefinition.getTypeName( TYPE_BOOL )
                );
                AnalyzeErrorCounter.e();
            }
        }
        // <block>
        node.childrenAccept( this, data );
        return cond;
    }
}
