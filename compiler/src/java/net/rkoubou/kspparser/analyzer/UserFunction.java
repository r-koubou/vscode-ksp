/* =========================================================================

    UserFunction.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.javacc.generated.ASTUserFunctionDeclaration;

/**
 * ユーザー定義関数の中間表現を示す
 */
public class UserFunction extends SymbolDefinition
{

    /** 元となるASTノード */
    public final ASTUserFunctionDeclaration astNode;

    /**
     * Ctor.
     */
    public UserFunction( ASTUserFunctionDeclaration node )
    {
        copy( node.symbol, this );
        this.astNode = node;
        this.symbolType = SymbolType.UserFunction;
    }
}
