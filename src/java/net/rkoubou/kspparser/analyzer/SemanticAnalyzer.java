/* =========================================================================

    SemanticAnalyzer.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclaration;

/**
 * 意味解析実行クラス
 */
public class SemanticAnalyzer extends AbstractAnalyzer
{

    /** シンボルテーブル保持インスタンス */
    public final SymbolCollector symbolCollector;

    /**
     * ctor
     */
    public SemanticAnalyzer( SymbolCollector symbolCollector )
    {
        super( symbolCollector.astRootNode );
        this.symbolCollector = symbolCollector;
    }

    /**
     * 意味解析の実行
     */
    @Override
    public void analyze() throws Exception
    {
        astRootNode.jjtAccept( this, null );
    }

    /**
     * 変数宣言
     */
    @Override
    public Object visit( ASTVariableDeclaration node, Object data)
    {
        final Object ret = defaultVisit( node, data );
        final VariableTable variableTable = symbolCollector.variableTable;
        final UITypeTable uiTypeTable     = symbolCollector.uiTypeTable;
        final Variable variable           = variableTable.searchVariable( node.symbol.name );
        System.out.println( variable.name );
        return ret;
    }

}
