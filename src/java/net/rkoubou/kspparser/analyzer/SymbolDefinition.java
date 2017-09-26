/* =========================================================================

    SymbolDefinition.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

/**
 * ASTノード、ノード中に宣言されたシンボルの中間状態を表現する
 */
public class SymbolDefinition implements AnalyzerConstants
{
    public enum SymbolType
    {
        Unknown,
        Callback,
        Command,
        UserFunction,
        Variable,
        Literal,
        Expression,
        PreprocessorSymbol,
    };

    /** シンボルの種類 */
    public SymbolType symbolType = SymbolType.Unknown;
    /** シンボルテーブルインデックス値 */
    public int index = -1;
    /** データ型 */
    public int type = TYPE_NONE;
    /** アクセス識別フラグ（ある場合に使用。未使用の場合は0） */
    public int accessFlag = 0;
    /** 実行環境で予約済みのシンボルかどうか） */
    public boolean reserved = false;
    /** 識別子名 */
    public String name = "";
    /** 意味解析フェーズ中に走査し参照されたかを記録する */
    public boolean referenced = false;
    /** accessFlagにACCESS_ATTR_UIが含まれている場合のUIタイプの識別子名 */
    public String uiTypeName = "";
    /** 値がある場合はその値(Integer,Double,String,int[],double[],String[]) */
    public Object value = null;
    /** 定義した行・列情報 */
    public final Position position = new Position();

    /**
     * Ctor.
     */
    public SymbolDefinition(){}

    /**
     * コピーコンストラクタ
     */
    public SymbolDefinition( SymbolDefinition src )
    {
        SymbolDefinition.copy( src, this );
    }

    /**
     * 値コピー
     */
    static public void copy( SymbolDefinition src, SymbolDefinition dest )
    {
        dest.symbolType     = src.symbolType;
        dest.index          = src.index;
        dest.type           = src.type;
        dest.accessFlag     = src.accessFlag;
        dest.reserved       = src.reserved;
        dest.name           = src.name;
        dest.uiTypeName     = src.uiTypeName;
        dest.value          = src.value;
        dest.position.copy( src.position );
    }
}
