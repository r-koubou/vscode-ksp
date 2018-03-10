/* =========================================================================

    SymbolCollector.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.io.IOException;

import net.rkoubou.kspparser.javacc.generated.ASTCallbackDeclaration;
import net.rkoubou.kspparser.javacc.generated.ASTRootNode;
import net.rkoubou.kspparser.javacc.generated.ASTUserFunctionDeclaration;
import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclaration;
import net.rkoubou.kspparser.javacc.generated.Node;
import net.rkoubou.kspparser.javacc.generated.SimpleNode;;

/**
 * シンボルテーブル構築クラス
 */
public class SymbolCollector extends AbstractAnalyzer
{
    public final UITypeTable uiTypeTable                            = new UITypeTable();
    public final VariableTable variableTable                        = new VariableTable();
    public final CallbackTable callbackTable                        = new CallbackTable();
    public final CommandTable commandTable                          = new CommandTable();
    public final UserFunctionTable userFunctionTable                = new UserFunctionTable();
    public final PreProcessorSymbolTable preProcessorSymbolTable    = new PreProcessorSymbolTable();

    /**
     * NI が使用を禁止している変数名の接頭文字
     */
    static private final String[] RESERVED_VARIABLE_PREFIX_LIST =
    {
        // From KSP Reference Manual:
        // Please do not create variables with the prefixes below, as these prefixes are used for
        // internal variables and constants
        "$NI_",
        "$CONTROL_PAR_",
        "$EVENT_PAR_",
        "$ENGINE_PAR_",
    };

    /**
     * ctor.
     */
    public SymbolCollector( ASTRootNode node )
    {
        super( node );
    }

    /**
     * 予約済み変数を定義ファイルから収集する
     */
    private void collectReservedariable() throws IOException
    {
        ReservedSymbolManager mgr = ReservedSymbolManager.getManager();
        mgr.load();
        mgr.apply( uiTypeTable );
        mgr.apply( variableTable );
        mgr.apply( callbackTable );
        mgr.apply( commandTable );
    }

    /**
     * ユーザースクリプトからシンボルを収集
     */
    @Override
    public void analyze() throws Exception
    {
        collectReservedariable();
        astRootNode.jjtAccept( this, null );
    }

    /**
     * 変数テーブル構築
     */
    @Override
    public Object visit( ASTVariableDeclaration node, Object data )
    {
        Object ret = defaultVisit( node, data );
//--------------------------------------------------------------------------
/*
    変数
        [node]
        VariableDeclaration
            -> VariableDeclarator [arrayindex]
                -> [VariableInitializer]
                    -> Expression
*/
//--------------------------------------------------------------------------
        if( validateVariableImpl( node ) )
        {
            variableTable.add( node );
            Variable v = variableTable.search( node.symbol.name );
            //--------------------------------------------------------------------------
            // UI変数チェック / 外部定義とのマージ
            //--------------------------------------------------------------------------
            if( v.isUIVariable() )
            {
                String uiName = v.uiTypeName;
                UIType uiType = uiTypeTable.search( uiName );
                if( uiType == null )
                {
                    // NI が定義していないUIの可能性
                    MessageManager.printlnW( MessageManager.PROPERTY_WARN_UI_VARIABLE_UNKNOWN, v );
                    AnalyzeErrorCounter.w();
                }
                else
                {
                    // UI変数に適したデータ型へマージ
                    v.accessFlag = ACCESS_ATTR_UI;
                    // v.type       = uiType.uiValueType; 型は意味解析フェーズでチェック
                    if( uiType.constant )
                    {
                        v.accessFlag |= ACCESS_ATTR_CONST;
                    }
                    // 意味解析フェーズで詳細を参照するため保持
                    v.uiTypeInfo = uiType;
                }
            }
            // プリミティブ型
            else
            {
                // const、poly修飾子は構文解析フェーズで代入済み
                v.type = SymbolDefinition.getKSPTypeFromVariableName( v.name );
            }
        }

        return ret;
    }

    /**
     * 変数、プリプロセッサシンボル収集の共通の事前検証処理
     */
    protected boolean validateVariableImpl( SimpleNode node )
    {
        //--------------------------------------------------------------------------
        // 変数は on init 内でしか宣言できない
        //--------------------------------------------------------------------------
        ASTCallbackDeclaration currentCallBack = null;
        {
            Node n = node.jjtGetParent();
            do
            {
                if( n.getId() == JJTCALLBACKDECLARATION )
                {
                    currentCallBack = (ASTCallbackDeclaration)n;
                    break;
                }
                n = n.jjtGetParent();
            }while( true );
        }
        //--------------------------------------------------------------------------
        // 変数名の検証（型チェックは意味解析フェーズで実行）
        //--------------------------------------------------------------------------
        {
            SymbolDefinition d = node.symbol;
            //--------------------------------------------------------------------------
            // 予約済み（NIが禁止している）接頭語検査
            //--------------------------------------------------------------------------
            {
                for( String n : RESERVED_VARIABLE_PREFIX_LIST )
                {
                    if( d.name.startsWith( n ) )
                    {
                        MessageManager.printlnE( MessageManager.PROPERTY_ERROR_VARIABLE_PREFIX_RESERVED, d );
                        AnalyzeErrorCounter.e();
                        break;
                    }
                }
            }
            //--------------------------------------------------------------------------
            // on init 外での宣言検査
            //--------------------------------------------------------------------------
            if( !currentCallBack.symbol.name.equals( "init" ) )
            {
                MessageManager.printlnE( MessageManager.PROPERTY_ERROR_VARIABLE_ONINIT, d );
                AnalyzeErrorCounter.e();
            }
            //--------------------------------------------------------------------------
            // 定義済みの検査
            //--------------------------------------------------------------------------
            {
                Variable v = variableTable.search( d.name );
                // NI の予約変数との重複
                if( v != null && v.reserved )
                {
                    MessageManager.printlnE( MessageManager.PROPERTY_ERROR_VARIABLE_RESERVED, d );
                    AnalyzeErrorCounter.e();
                    return false;
                }
                // ユーザー変数との重複
                else if( v != null )
                {
                    MessageManager.printlnE( MessageManager.PROPERTY_ERROR_VARIABLE_DECLARED, d );
                    AnalyzeErrorCounter.e();
                    return false;
                }
                // 未定義：新規追加可能
                else
                {
                    return true;
                }
            }
        }
    }

    /**
     * コールバックテーブル構築
     */
    @Override
    public Object visit( ASTCallbackDeclaration node, Object data )
    {
        Object ret = defaultVisit( node, data );

        if( !callbackTable.add( node ) )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_CALLBACK_DECLARED, node.symbol );
            AnalyzeErrorCounter.e();
        }
        return ret;
    }

    /**
     * ユーザー定義関数テーブル構築
     */
    @Override
    public Object visit( ASTUserFunctionDeclaration node, Object data )
    {
        Object ret = defaultVisit( node, data );

        if( !node.symbol.validateNonVariablePrefix() )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_GENERAL_SYMBOL_PREFIX_NUMERIC, node.symbol );
            AnalyzeErrorCounter.e();
            return ret;
        }

        if( !userFunctionTable.add( node ) )
        {
            MessageManager.printlnE( MessageManager.PROPERTY_ERROR_FUNCTION_DECLARED, node.symbol );
            AnalyzeErrorCounter.e();
            return ret;
        }

        return ret;
    }
}