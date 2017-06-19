/* =========================================================================

    Variable.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.util.Arrays;

import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclaration;

/**
 * 値、変数の中間表現を示す
 */
public class Variable extends SymbolDefinition
{

    static public final String REGEX_PREPROCESSOR_PREFIX = "^[a-z|A-Z|_]";

    public final ASTVariableDeclaration astNode;

    /** 型 */
    public int type = TYPE_UNKNOWN;

    /** 配列型の場合の要素数 */
    public int arraySize = 0;

    /** コンスタントプールに格納される場合のインデックス番号 */
    public int constantIndex = -1;

    /** 値がある場合はその値(Integer,Double,String) */
    public Object value = null;

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
            case TYPE_REAL:
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
        if( obj == null || !( obj instanceof Variable ) )
        {
            return false;
        }

        Variable p = (Variable)obj;

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
     * valueに指定された値のIntegerインスタンスを割り当てる
     */
    public void setValue( int v )
    {
        if( getType() != TYPE_INT )
        {
            throw new IllegalStateException( "Type is " + getTypeName() );
        }
        this.value = new Integer( v );
    }

    /**
     * valueに指定された値のint[]インスタンスを割り当てる
     */
    public void setValue( int[] v )
    {
        if( getType() != TYPE_INT && !isArray() )
        {
            throw new IllegalStateException( "Type is " + getTypeName() );
        }
        int[] newV = Arrays.copyOf( v, v.length );
        this.value = newV;
    }

    /**
     * valueに指定された値のDoubleインスタンスを割り当てる
     */
    public void setValue( double v )
    {
        if( getType() != TYPE_REAL )
        {
            throw new IllegalStateException( "Type is " + getTypeName() );
        }
        this.value = new Double( v );
    }

    /**
     * valueに指定された値のint[]インスタンスを割り当てる
     */
    public void setValue( double[] v )
    {
        if( getType() != TYPE_REAL && !isArray() )
        {
            throw new IllegalStateException( "Type is " + getTypeName() );
        }
        double[] newV = Arrays.copyOf( v, v.length );
        this.value = newV;
    }

    /**
     * valueに指定された値のStringインスタンスを割り当てる
     */
    public void setValue( String v )
    {
        if( getType() != TYPE_STRING )
        {
            throw new IllegalStateException( "Type is " + getTypeName() );
        }
        this.value = v;
    }

    /**
     * valueに指定された値のint[]インスタンスを割り当てる
     */
    public void setValue( String[] v )
    {
        if( getType() != TYPE_STRING && !isArray() )
        {
            throw new IllegalStateException( "Type is " + getTypeName() );
        }
        String[] newV = Arrays.copyOf( v, v.length );
        this.value = newV;
    }

    /**
     * この変数の型が配列かどうかを判別する
     */
    public boolean isArray()
    {
        return ( type & TYPE_ATTR_ARRAY ) != 0;
    }

    /**
     * 型の識別値を返す
     */
    public int getType()
    {
        return type & TYPE_ATTR_MASK;
    }

    /**
     * 変数名を元に文字列表現された型情報を返す
     */
    public String getTypeName()
    {
        return getTypeName( name );
    }

    /**
     * 変数名を元に文字列表現された型情報を返す
     */
    static public String getTypeName( String name )
    {
        if( name == null || name.length() == 0 )
        {
            return "Unknown";
        }

        if( name.matches( REGEX_PREPROCESSOR_PREFIX ) )
        {
            return "Preprocessor Symbol";
        }

        char t = name.charAt( 0 );
        switch( t )
        {
            case '$': return "Integer";
            case '%': return "Integer Array";
            case '~': return "Real";
            case '?': return "Real Array";
            case '@': return "String";
            case '!': return "String Array";
            default:
                return "Unknown";
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

        if( variableName.matches( REGEX_PREPROCESSOR_PREFIX ) )
        {
            return TYPE_PREPROCESSOR_SYMBOL;
        }

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

    /**
     * 変数の型データからKSPの定める型式別の記号に変換する
     */
    public String toKSPTypeCharacter()
    {
        return toKSPTypeCharacter( type );
    }

    /**
     * 変数の型データからKSPの定める型式別の記号に変換する
     */
    static public String toKSPTypeCharacter( int type )
    {
        switch( type )
        {
            case TYPE_INT:                      return "$";
            case TYPE_INT | TYPE_ATTR_ARRAY:    return "%";
            case TYPE_REAL:                     return "~";
            case TYPE_REAL | TYPE_ATTR_ARRAY:   return "?";
            case TYPE_STRING:                   return "@";
            case TYPE_STRING | TYPE_ATTR_ARRAY: return "!";
            case TYPE_PREPROCESSOR_SYMBOL:      return "(Preprocessor Symbol)";
            default:
                throw new IllegalArgumentException( "type is " + type );
        }
    }
}
