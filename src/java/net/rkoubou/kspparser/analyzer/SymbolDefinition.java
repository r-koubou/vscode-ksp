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
    /** シンボルテーブルインデックス値 */
    public int index = 0;
    /** アクセス識別フラグ（ある場合に使用。未使用の場合は0） */
    public int accessFlag = 0;
    /** 識別子名 */
    public String name = null;
    /** 定義した行番号 */
    public int line = 0;
    /** 定義した行中の列 */
    public int colmn = 0;
    /** 型 */
    public int type = TYPE_UNKNOWN;
    /** 値がある場合はその値 */
    public Object value = null;
    /** 戻り値型（KSPでは文法上存在しないので、現在は使用しない） */
    public int returnType = TYPE_UNKNOWN;
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
        dest.index      = src.index;
        dest.accessFlag = src.accessFlag;
        dest.name       = src.name;
        dest.type       = src.type;
        dest.value      = src.value;
        dest.returnType = src.returnType;
    }

    /**
     * このシンボルが変数である場合、値をセットする
     * @param type AnalyzerConstants.TYPE_####
     * @param value 値となるトークン文字列
     * @see AnalyzerConstants
     */
    public void setVariableValue( int type, String value )
    {
        this.type  = type;

        if( type == TYPE_INT )
        {
            this.value = new Integer( Long.decode( value ).intValue() );
        }
        else if( type == TYPE_REAL )
        {
            this.value = new Double( Double.parseDouble( value ) );
        }
        else if( type == TYPE_STRING )
        {
            value = value.replaceAll( "\\\\t",    "\t" );
            value = value.replaceAll( "\\\\n",    "\n" );
            value = value.replaceAll( "\\\\r",    "\r" );
            value = value.replaceAll( "\\\\\"",   "\"" );
            value = value.replaceAll( "\\{2}",    "\\" );
            this.value = value;
        }
    }

    /**
     * 変数名の1文字目の記号から型情報を算出する
     */
    public boolean setTypeFromVariableName()
    {
        this.type = getTypeFromVariableName( this.name );
        return this.type != TYPE_UNKNOWN;
    }

    /**
     * 変数名の1文字目の記号から型情報を算出する
     */
    static public int getTypeFromVariableName( String variableName )
    {
        if( variableName == null || variableName.length() == 0 )
        {
            return TYPE_UNKNOWN;
        }
        char t = variableName.charAt( 0 );
        switch( t )
        {
            case '$': return TYPE_INT;
            case '%': return TYPE_INT | TYPE_ATTR_ARRAY;
            case '~': return TYPE_REAL;
            case '?': return TYPE_REAL | TYPE_ATTR_ARRAY;
            case '@': return TYPE_STRING;
            case '!': return TYPE_STRING | TYPE_ATTR_ARRAY;
            default:
                return TYPE_UNKNOWN;
        }
    }
}
