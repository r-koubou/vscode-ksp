/* =========================================================================

    CallbackTable.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.javacc.generated.ASTCallbackDeclaration;;

/**
 * コールバックテーブル
 */
public class CallbackTable extends SymbolTable<ASTCallbackDeclaration, Callback> implements AnalyzerConstants
{

    /**
     * ctor
     */
    public CallbackTable()
    {
        super();
    }

    /**
     * コールバックテーブルへの追加
     */
    @Override
    public boolean add( ASTCallbackDeclaration decl )
    {
        return true;
    }

}
