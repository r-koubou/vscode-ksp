/* =========================================================================

    Variable.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclaration;

/**
 * 値、変数の中間表現を示す
 */
public class Variable<T> extends SymbolDefinition
{
    public final ASTVariableDeclaration astNode;

    /** 型 */
    public int type = TYPE_UNKNOWN;

    /** 配列型の場合の要素数 */
    public int arraySize;

    /** コンスタントプールに格納される場合のインデックス番号 */
    public int constantIndex = -1;

    /** 値がある場合はその値(Integer,Double,String) */
    public T value = null;

    /** 意味解析フェーズ中に走査し参照されたかを記録する */
    boolean referenced = false;

    /** 単項演算子により生成、かつリテラル値 */
    boolean constantValueWithSingleOperator = false;

    /** 状態 */
    public VariableState status = VariableState.UNLOADED;

    /**
     * Ctor.
     */
    public Variable( ASTVariableDeclaration node )
    {
        copy( node.symbol, this );
        this.astNode = node;
    }

    /**
     * 数値型かどうかを判定する
     */
    public boolean isNumeric()
    {
        switch( type & TYPE_MASK )
        {
            case TYPE_INT:
                return true;

            default:
                return false;
        }
    }

    /**
     * 定数かどうかを判定する
     */
    public boolean isConstant()
    {
        return ( accessFlag & ACCESS_ATTR_CONST ) != 0;
    }

    /**
     * UI変数かどうかを判定する
     */
    public boolean isUIVariable()
    {
        return ( accessFlag & ACCESS_ATTR_UI ) != 0;
    }

    /**
     * ポリフォニック変数かどうかを判定する
     */
    public boolean isPolyphonicVariable()
    {
        return ( accessFlag & ACCESS_ATTR_POLY ) != 0;
    }

    /**
     * valueの比較
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override

    public boolean equals( Object obj )
    {
        if( obj == null || !( obj instanceof Variable<?> ) )
        {
            return false;
        }

        Variable<?> p = (Variable<?>)obj;

        return p.value.equals( this.value );
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
    static public Variable<?> create( ASTVariableDeclaration decl )
    {
        String variableName = decl.symbol.name;
        if( variableName == null || variableName.length() == 0 )
        {
            throw new IllegalArgumentException();
        }
        char t = variableName.charAt( 0 );
        switch( t )
        {
            case '$':
            case '%':
                return new Variable<Integer>( decl );
            case '~':
            case '?':
                return new Variable<Double>( decl );
            case '@':
            case '!':
                return new Variable<String>( decl );
            default:
                throw new IllegalArgumentException();
        }
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
