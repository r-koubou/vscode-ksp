/* =========================================================================

    Variable.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.util.Comparator;

import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclaration;

/**
 * 値、変数の中間表現を示す
 */
public class Variable extends SymbolDefinition
{
    public final ASTVariableDeclaration astNode;

    int    arraySize;
    int    constantIndex = -1;
    int    objectID;

    boolean referenced = false;
    boolean constantValueWithSingleOperator = false; // 単項演算子により生成、かつリテラル値

    /** 状態 */
    public VariableState status = VariableState.UNLOADED;

    static public Comparator<Variable> comparatorById =

        new Comparator<Variable>()
        {
            /**
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            public int compare( Variable o1, Variable o2 )
            {
                return o1.index - o2.index;
            }
        };

    static public Comparator<Variable> comparatorByType =

        new Comparator<Variable>()
        {
            /**
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            public int compare( Variable o1, Variable o2 )
            {
                int cmp = o1.type - o2.type;
                if( cmp == 0 )
                {
                    return comparatorById.compare( o1, o2 );
                }
                return cmp;
            }
        };

    /**
     * Ctor.
     */
    public Variable( ASTVariableDeclaration node )
    {
        copy( node.symbol, this );
        this.astNode = node;
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * 数値型かどうかを判定する
     */
    public boolean isNumeric()
    {
        switch( type )
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

        if( p.type != this.type )
        {
            return false;
        }

        switch( p.type & TYPE_MASK )
        {
            case TYPE_INT:
                int v1 = (Integer)this.value;
                int v2 = (Integer)p.value;
                return v1 == v2;

            case TYPE_STRING:
                return p.value.toString().equals( this.value.toString() );

        }

        if( ( type & TYPE_ATTR_ARRAY ) != 0 )
        {
            return p.value == value;
        }

        return false;
    }
}

