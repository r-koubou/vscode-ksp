/* =========================================================================

    Callback.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.javacc.generated.ASTCallbackDeclaration;

/**
 * コールバック（引数なし）の中間表現を示す
 */
public class Callback extends SymbolDefinition
{

    /** 元となるASTノード */
    public final ASTCallbackDeclaration astNode;

    /** 多重宣言を許可するかどうか(例：on ui_controlなどは複数宣言可能) */
    public boolean allowDuplicate;

    /** 外部定義ファイルからの取り込みとの組み合わせで使用する(allowDuplicate) */
    public boolean declared = false;

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
