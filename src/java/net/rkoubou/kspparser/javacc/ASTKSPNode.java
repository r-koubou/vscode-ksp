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

    /**
     * このノードが2項演算子かどうかを判定する
     */
    public boolean isBinaryOperator()
    {
        switch( getId() )
        {
            case JJTADD:
            case JJTSTRADD:
            case JJTSUB:
            case JJTMUL:
            case JJTDIV:
            case JJTMOD:
            case JJTINCLUSIVEOR:
            case JJTAND:
                return true;
        }
        return false;
    }

    /**
     * このノードが単項演算子かどうかを判定する
     */
    public boolean isSingleOperator()
    {
        switch( getId() )
        {
            case JJTNEG:
            case JJTNOT:
            case JJTLOGICALNOT:
                return true;
        }
        return false;
    }
}
