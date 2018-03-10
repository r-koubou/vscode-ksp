/* =========================================================================

    Variable.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclaration;

/**
 * KSPの値、変数の中間表現を示す
 */
public class Variable extends SymbolDefinition
{
    /** 元となるASTノード */
    public final ASTVariableDeclaration astNode;

    /** 配列型の場合の要素数 */
    public int arraySize = 0;

    /**
     * UI型変数の場合に値がセットされる（シンボル収集フェーズ）
     * 初期値は null
     * @see SymbolCollector
     */
    public UIType uiTypeInfo = null;

    /** コンスタントプールに格納される場合のインデックス番号 */
    public int constantIndex = -1;

    /** on init 内で使用可能な変数かどうか（外部低ファイルから読み込むビルトイン変数用） */
    public boolean availableOnInit = true;

    /** 単項演算子により生成、かつリテラル値 */
    public boolean constantValueWithSingleOperator = false;

    /**
     * Ctor.
     */
    public Variable( ASTVariableDeclaration node )
    {
        copy( node.symbol, this );
        this.astNode    = node;
        this.symbolType = SymbolType.Variable;
    }

    /**
     * 変数の型データからKSP文法の変数名表現に変換する
     */
    @Override
    public String toString()
    {
        if( obfuscatedName != null && obfuscatedName.length() > 0 )
        {
            return toKSPTypeCharacter() + obfuscatedName;
        }
        return toKSPTypeCharacter() + name;
    }
}
