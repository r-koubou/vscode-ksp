/* =========================================================================

    SymbolCollection.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.javacc.generated.KSPParserDefaultVisitor;
import net.rkoubou.kspparser.javacc.generated.KSPParserTreeConstants;
import net.rkoubou.kspparser.javacc.generated.Node;
import net.rkoubou.kspparser.javacc.generated.SimpleNode;
import net.rkoubou.kspparser.javacc.generated.ASTCallbackDeclaration;
import net.rkoubou.kspparser.javacc.generated.ASTRootNode;
import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclaration;;

/**
 * シンボルテーブル構築クラス
 */
public class SymbolCollector extends KSPParserDefaultVisitor implements AnalyzerConstants, KSPParserTreeConstants
{
    private final ASTRootNode rootNode;
    private final VariableTable variableTable = new VariableTable();

    static private final String[] RESERVED_VARIABLE_PREFIX_LIST =
    {
        "$NI_",
        "$_CONTROL_PAR_",
        "$EVENT_PAR_",
        "$ENGINE_PAR_",
    };

    /**
     * ctor.
     */
    public SymbolCollector( ASTRootNode node )
    {
        this.rootNode = node;
    }

    /**
     * 予約済み変数を定義ファイルから収集する
     */
    private void collectReservedariable()
    {
    }

    /**
     * ユーザースクリプトからシンボルを収集
     */
    public void collect()
    {
        this.rootNode.jjtAccept( this, null );
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
            -> VariableDeclarator
                -> [VariableInitializer]
                    -> Expression
*/
//--------------------------------------------------------------------------
        if( validateVariableImpl( node ) )
        {
            variableTable.add( node );
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
            }
            //--------------------------------------------------------------------------
            // 定義着済みの検査
            //--------------------------------------------------------------------------
            {
                Variable v = variableTable.searchVariable( d.name );
                // NI の予約変数との重複
                if( v != null && v.reserved )
                {
                    MessageManager.printlnE( MessageManager.PROPERTY_ERROR_VARIABLE_RESERVED, d );
                    return false;
                }
                // ユーザー変数との重複
                else if( v != null )
                {
                    MessageManager.printlnE( MessageManager.PROPERTY_ERROR_VARIABLE_DECLARED, d );
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
        return data;
    }

}