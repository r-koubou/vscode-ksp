/* =========================================================================

    Argument.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.util.Arrays;

import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclaration;

/**
 * 引数の変数の中間表現を示す
 */
public class Argument extends Variable
{
    /** on init 内で変数宣言されている必要があるかどうか（例：on ui_control コールバック） */
    public boolean requireDeclarationOnInit = false;

    /**
     * Ctor.
     */
    public Argument( ASTVariableDeclaration node )
    {
        super( node );
    }
}
