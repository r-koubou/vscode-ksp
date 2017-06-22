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
        PreprocessorSymbol,
    };

    /** シンボルの種類 */
    public SymbolType symbolType = SymbolType.Unknown;
    /** シンボルテーブルインデックス値 */
    public int index = -1;
    /** アクセス識別フラグ（ある場合に使用。未使用の場合は0） */
    public int accessFlag = 0;
    /** 実行環境で予約済みのシンボルかどうか） */
    public boolean reserved = false;
    /** 識別子名 */
    public String name = null;
    /** accessFlagにACCESS_ATTR_UIが含まれている場合のUIタイプの識別子名 */
    public String uiTypeName = null;
    /** 定義した行番号 */
    public int line = 0;
    /** 定義した行中の列 */
    public int colmn = 0;
    /** 戻り値型（KSPでは文法上存在しないので、現在は使用しない） */
    public int returnType = TYPE_NONE;
    /** 代入式ノードで使用する */
    public AssignOprator oprator = AssignOprator.NULL;

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
        dest.accessFlag     = src.accessFlag;
        dest.reserved       = src.reserved;
        dest.name           = src.name;
        dest.uiTypeName     = src.uiTypeName;
        dest.line           = src.line;
        dest.colmn          = src.colmn;
        dest.returnType     = src.returnType;
        dest.oprator        = src.oprator;
    }

    // /**
    //  * このシンボルが変数である場合、値をセットする
    //  * @param type AnalyzerConstants.TYPE_####
    //  * @param value 値となるトークン文字列
    //  * @see AnalyzerConstants
    //  */
    // public void setVariableValue( int type, String value )
    // {
    //     this.type  = type;

    //     if( type == TYPE_INT )
    //     {
    //         this.value = new Integer( Long.decode( value ).intValue() );
    //     }
    //     else if( type == TYPE_REAL )
    //     {
    //         this.value = new Double( Double.parseDouble( value ) );
    //     }
    //     else if( type == TYPE_STRING )
    //     {
    //         value = value.replaceAll( "\\\\t",    "\t" );
    //         value = value.replaceAll( "\\\\n",    "\n" );
    //         value = value.replaceAll( "\\\\r",    "\r" );
    //         value = value.replaceAll( "\\\\\"",   "\"" );
    //         value = value.replaceAll( "\\{2}",    "\\" );
    //         this.value = value;
    //     }
    // }

}
