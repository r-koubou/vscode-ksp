/* =========================================================================

    VariableTable.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.javacc.generated.SimpleNode;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;


/**
 * 基底シンボルテーブル
 */
abstract public class SymbolTable<NODE extends SimpleNode, SYMBOL extends SymbolDefinition> implements AnalyzerConstants
{
    public enum SortType
    {
        BY_ID,
        BY_TYPE,
    }

    protected SymbolTable<NODE, SYMBOL> parent;

    protected int index;
    protected Hashtable<String, SYMBOL> table = new Hashtable<String, SYMBOL>( 64 );

    public final Comparator<SymbolDefinition> comparatorById =

        new Comparator<SymbolDefinition>()
        {
            /**
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            public int compare( SymbolDefinition o1, SymbolDefinition o2 )
            {
                return o1.index - o2.index;
            }
        };

    public final Comparator<SymbolDefinition> comparatorByType =

        new Comparator<SymbolDefinition>()
        {
            /**
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            public int compare( SymbolDefinition o1, SymbolDefinition o2 )
            {
                int cmp = o1.symbolType.compareTo( o2.symbolType );
                if( cmp == 0 )
                {
                    return comparatorById.compare( o1, o2 );
                }
                return cmp;
            }
        };

    /**
     * ctor
     */
    public SymbolTable()
    {
        this.index = 0;
        parent     = null;
    }

    /**
     * ctor
     */
    public SymbolTable( SymbolTable<NODE, SYMBOL> parent )
    {
        this.index  = parent.index;
        this.parent = parent;
    }

    /**
     * ctor
     */
    public SymbolTable( SymbolTable<NODE, SYMBOL> parent, int startIndex )
    {
        this.index  = startIndex;
        this.parent = parent;
    }

    /**
     * シンボルテーブルへの追加
     */
    abstract public boolean add( NODE node );

    /**
     * 指定したシンボル名がテーブルに登録されているか検索する
     * @return あった場合は有効なインスタンス、無い場合は null
     */
    public SYMBOL searchVariable( String name, boolean enableSearchParent )
    {
        SYMBOL v = table.get( name );
        if( v == null && enableSearchParent )
        {
            SymbolTable<NODE, SYMBOL> p = parent;
            while( p != null )
            {
                v = p.table.get( name );
                if( v != null )
                {
                    return v;
                }
                p = p.parent;
            }
            return null;
        }
        return v;
    }

    /**
     * 指定したシンボル名がテーブルに登録されているか検索する
     * @return あった場合は有効なインスタンス、無い場合は null
     */
    public SYMBOL searchVariable( String name )
    {
        return searchVariable( name, true );
    }

    /**
     * 指定したシンボル名がテーブルに登録されているか検索する
     * @return あった場合はインデックス番号、無い場合は -1
     */
    public int searchVariableID( String name, boolean enableSearchParent )
    {
        SYMBOL v = searchVariable( name, enableSearchParent );
        if( v == null )
        {
            SymbolTable<NODE, SYMBOL> p = parent;
            while( p != null )
            {
                v = p.table.get( name );
                if( v != null )
                {
                    return v.index;
                }
                p = p.parent;
            }
            return -1;
        }
        return v.index;
    }

    /**
     * 指定したシンボル名がテーブルに登録されているか検索する
     * @return あった場合はインデックス番号、無い場合は -1
     */
    public int searchVariableID( String name )
    {
        return searchVariableID( name, true );
    }

    /**
     * 登録されているシンボルを配列形式で返す
     */
    public SymbolDefinition[] toArray( SortType sortType )
    {
        Comparator<SymbolDefinition> c = null;

        switch( sortType )
        {
            case BY_ID:   c = comparatorById;   break;
            case BY_TYPE: c = comparatorByType; break;
            default:
                throw new IllegalArgumentException();
        }

        SymbolDefinition[] array = table.values().toArray( new Variable[ 0 ] );
        if( array.length > 0 )
        {
            Arrays.sort( array, c );
        }

        return array;

    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder buff = new StringBuilder( 64 );
        for( Enumeration<SYMBOL> e = table.elements(); e.hasMoreElements(); )
        {
            SYMBOL v = e.nextElement();
            buff.append( v.toString() ).append( '\n' );
        }

        return buff.toString();
    }

    /**
     * デバッグ用のダンプ
     */
    public void dumpSymbol( PrintStream ps )
    {
        for( SymbolDefinition v : toArray( SortType.BY_TYPE) )
        {
            ps.println( v.name );
        }
    }

}
