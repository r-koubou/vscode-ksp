/* =========================================================================

    PreProcessorSymbol.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.javacc.generated.ASTPreProcessorDefine;

/**
 * プリプロセッサで定義したシンボルの中間表現を示す
 */
public class PreProcessorSymbol extends SymbolDefinition
{

    /** 元となるASTノード */
    public final ASTPreProcessorDefine astNode;

    /**
     * Ctor.
     */
    public PreProcessorSymbol( ASTPreProcessorDefine node )
    {
        copy( node.symbol, this );
        this.astNode = node;
        this.symbolType = SymbolType.PreprocessorSymbol;
    }
}
