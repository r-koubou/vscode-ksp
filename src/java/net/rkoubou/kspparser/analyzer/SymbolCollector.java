/* =========================================================================

    SymbolCollection.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.javacc.generated.KSPParserDefaultVisitor;
import net.rkoubou.kspparser.javacc.generated.KSPParserTreeConstants;
import net.rkoubou.kspparser.javacc.generated.Node;
import net.rkoubou.kspparser.analyzer.MessageManager.Level;
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

    /**
     * ctor.
     */
    public SymbolCollector( ASTRootNode node )
    {
        this.rootNode = node;
    }

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

/*
        [node]
        VariableDeclaration
            -> VariableDeclarator
                -> [VariableInitializer]
                    -> Expression
*/
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
        // 変数名の検証（型チェックは意味解析フェーズで実行）
        {
            SymbolDefinition d = node.symbol;
            if( !currentCallBack.symbol.name.equals( "init" ) )
            {
                MessageManager.println( "error.variable.declared.oninit", Level.ERROR, node.symbol );
            }
            if( !variableTable.add( node ) )
            {
                MessageManager.println( "error.variable.already.declared", Level.ERROR, node.symbol );
            }
        }
        return ret;
    }

}
