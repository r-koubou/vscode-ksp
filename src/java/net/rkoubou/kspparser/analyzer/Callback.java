/* =========================================================================

    Callback.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.util.ArrayList;
import java.util.Arrays;

import net.rkoubou.kspparser.javacc.generated.ASTCallbackDeclaration;

/**
 * コールバックの中間表現を示す
 */
public class Callback extends SymbolDefinition
{

    public final ASTCallbackDeclaration astNode;

    /**
     * Ctor.
     */
    public Callback( ASTCallbackDeclaration node )
    {
        copy( node.symbol, this );
        this.astNode = node;
        this.symbolType = SymbolType.Callback;
    }
}
