/* =========================================================================

    SymbolTable.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;

import net.rkoubou.kspparser.javacc.generated.SimpleNode;
import net.rkoubou.kspparser.obfuscator.ShortSymbolGenerator;


/**
 * ASTをベースにした基底シンボルテーブル
 */
abstract public class SymbolTable<NODE extends SimpleNode, SYMBOL extends SymbolDefinition> implements AnalyzerConstants
{

    /**
     * ソートタイプ
     */
    public enum SortType
    {
        /** テーブルインデックス値 */
        BY_INDEX,
        /** シンボルタイプ */
        BY_TYPE,
    }

    /** ネストなどのローカルスコープを許容する場合に使用する親ノード */
    protected SymbolTable<NODE, SYMBOL> parent;

    /** シンボルに割り当てられるユニークなインデックス値 */
    protected int index;

    /** テーブル本体 */
    protected Hashtable<String, SYMBOL> table = new Hashtable<String, SYMBOL>( 512 );

    /** ソート用比較処理（インデックス） */
    public final Comparator<SymbolDefinition> comparatorByIndex =

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

    /** ソート用比較処理（シンボルタイプ） */
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
                    return comparatorByIndex.compare( o1, o2 );
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
    public SYMBOL search( String name, boolean enableSearchParent )
    {
        SYMBOL v = table.get( name );
        if( v == null && enableSearchParent )
        {
            SymbolTable<NODE, SYMBOL> p = getParent();
            while( p != null )
            {
                v = p.table.get( name );
                if( v != null )
                {
                    return v;
                }
                p = p.getParent();
            }
            return null;
        }
        return v;
    }

    /**
     * 指定したシンボルがテーブルに登録されているか検索する
     * @return あった場合はsymbol自身、無い場合は null
     */
    public SYMBOL search( SymbolDefinition symbol, boolean enableSearchParent )
    {
        return search( symbol.getName( true ), enableSearchParent );
    }

    /**
     * 指定したシンボル名がテーブルに登録されているか検索する
     * @return あった場合は有効なインスタンス、無い場合は null
     */
    public SYMBOL search( String name )
    {
        return search( name, true );
    }

    /**
     * 指定したシンボルがテーブルに登録されているか検索する
     * @return あった場合はsymbol自身、無い場合は null
     */
    public SYMBOL search( SymbolDefinition symbol )
    {
        return search( symbol.getName( true ), true );
    }

    /**
     * 指定したインデックス値でテーブルに登録されているか検索する
     * @return あった場合はsymbol自身、無い場合は null
     */
    public SYMBOL search( int index )
    {
        for( Enumeration<SYMBOL> e = table.elements(); e.hasMoreElements(); )
        {
            SYMBOL v = e.nextElement();
            if( v.index == index )
            {
                return v;
            }
        }
        return null;
    }

    /**
     * 指定したシンボル名がテーブルに登録されているか検索する
     * @return あった場合はインデックス番号、無い場合は -1
     */
    public int searchID( String name, boolean enableSearchParent )
    {
        SYMBOL v = search( name, enableSearchParent );
        if( v == null )
        {
            SymbolTable<NODE, SYMBOL> p = getParent();
            while( p != null )
            {
                v = p.table.get( name );
                if( v != null )
                {
                    return v.index;
                }
                p = p.getParent();
            }
            return -1;
        }
        return v.index;
    }

    /**
     * 指定したシンボルがテーブルに登録されているか検索する
     * @return あった場合はインデックス番号、無い場合は -1
     */
    public int searchID( SymbolDefinition symbol, boolean enableSearchParent )
    {
        return searchID( symbol.getName( true ), enableSearchParent );
    }

    /**
     * 指定したシンボル名がテーブルに登録されているか検索する
     * @return あった場合はインデックス番号、無い場合は -1
     */
    public int searchID( String name )
    {
        return searchID( name, true );
    }

    /**
     * 指定したシンボルがテーブルに登録されているか検索する
     * @return あった場合はインデックス番号、無い場合は -1
     */
    public int searchID( SymbolDefinition symbol )
    {
        return searchID( symbol.getName( true ) );
    }

    /**
     * 登録されているシンボルを配列形式で返す
     */
    public SymbolDefinition[] toArray()
    {
        return table.values().toArray( new SymbolDefinition[ 0 ] );
    }

    /**
     * 登録されているシンボルを配列形式で返す
     */
    public SymbolDefinition[] toArray( SortType sortType )
    {
        Comparator<SymbolDefinition> c = null;

        switch( sortType )
        {
            case BY_INDEX:   c = comparatorByIndex;   break;
            case BY_TYPE: c = comparatorByType; break;
            default:
                throw new IllegalArgumentException();
        }

        SymbolDefinition[] array = table.values().toArray( new SymbolDefinition[ 0 ] );
        if( array.length > 0 )
        {
            Arrays.sort( array, c );
        }

        return array;

    }

    /**
     * 親テーブルを取得する
     */
    public SymbolTable<NODE, SYMBOL> getParent()
    {
        return parent;
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
            ps.println( v.getName() );
        }
    }

    /**
     * シンボル名のオブファスケートを行う
     */
    public void obfuscate()
    {
        for( Enumeration<SYMBOL> e = table.elements(); e.hasMoreElements(); )
        {
            SYMBOL v = e.nextElement();
            if( v.getName() != null && v.getName().length() > 0 && !v.reserved )
            {
                v.setObfuscatedName( ShortSymbolGenerator.generate( v.getName() ) );
            }
        }
    }
}
