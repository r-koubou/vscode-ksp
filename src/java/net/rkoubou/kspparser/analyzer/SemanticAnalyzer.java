/* =========================================================================

    SemanticAnalyzer.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.javacc.generated.ASTRootNode;
import net.rkoubou.kspparser.javacc.generated.KSPParserDefaultVisitor;
import net.rkoubou.kspparser.javacc.generated.KSPParserTreeConstants;

/**
 * 意味解析実行クラス
 */
public class SemanticAnalyzer extends KSPParserDefaultVisitor implements AnalyzerConstants, KSPParserTreeConstants
{
    public final ASTRootNode rootNode;
    public final UITypeTable uiTypeTable;
    public final VariableTable variableTable;
    public final CallbackTable callbackTable;
    public final UserFunctionTable userFunctionTable;

    /**
     * ctor
     */
     public SemanticAnalyzer( ASTRootNode node, UITypeTable uiTypeTable, VariableTable variableTable, CallbackTable callbackTable, UserFunctionTable userFunctionTable )
     {
         this.rootNode          = node;
         this.uiTypeTable       = uiTypeTable;
         this.variableTable     = variableTable;
         this.callbackTable     = callbackTable;
         this.userFunctionTable = userFunctionTable;
     }
}
