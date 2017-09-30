/* =========================================================================

    Command.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.util.ArrayList;
import java.util.HashMap;

import net.rkoubou.kspparser.javacc.generated.ASTCallCommand;
import net.rkoubou.kspparser.javacc.generated.KSPParserTreeConstants;

/**
 * コマンドの中間表現を示す
 */
public class Command extends SymbolDefinition implements KSPParserTreeConstants
{
    /** 元となるASTノード */
    public final ASTCallCommand astNode;

    /** 引数リスト */
    public final ArrayList<CommandArgument> argList = new ArrayList<CommandArgument>();

    /** 引数の括弧の有無の判定フラグ */
    public boolean hasParenthesis;

    /** 戻り値型 */
    public final ReturnType returnType = new ReturnType();

    /**
     * 使用可能なコマンドのコールバック（外部低ファイルから読み込むビルトインコマンド用）
     * <p>詳細： https://github.com/r-koubou/KSPSyntaxParser/wiki/External-symbol-definition-file-format#command</p>
     */
    public final HashMap<String,Callback> availableCallbackList = new HashMap<String,Callback>( 32 );

    /**
     * Ctor.
     */
    public Command( ASTCallCommand node )
    {
        super( node.symbol );
        this.astNode = node;
        this.astNode.symbol.symbolType = SymbolType.Command;
    }
}
