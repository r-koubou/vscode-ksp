/* =========================================================================

    ASTKSPNode.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.javacc;

import net.rkoubou.kspparser.analyzer.SymbolDefinition;

import net.rkoubou.kspparser.javacc.generated.Node;
import net.rkoubou.kspparser.javacc.generated.KSPParserTreeConstants;

/**
 * ASTの基底クラス
 */
abstract public class ASTKSPNode implements Node, KSPParserTreeConstants
{
    public final SymbolDefinition symbol = new SymbolDefinition();
}
