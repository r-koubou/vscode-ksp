/* =========================================================================

    SymbolCollection.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.javacc.generated.KSPParserDefaultVisitor;
import net.rkoubou.kspparser.javacc.generated.ASTRootNode;
import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclaration;;

/**
 * シンボルテーブル構築クラス
 */
public class SymbolCollector extends KSPParserDefaultVisitor implements AnalyzerConstants
{
    private final ASTRootNode rootNode;

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
        // Test code
        SymbolDefinition d = node.symbol;
        System.out.println( d.name + "(" + (d.type&TYPE_MASK) + ") at " + d.line + "," + d.colmn );

        return ret;
    }

}
