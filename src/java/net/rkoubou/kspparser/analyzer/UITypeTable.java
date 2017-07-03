/* =========================================================================

    UITypeTable.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;


/**
 * UITypeのシンボルテーブル
 */
public class UITypeTable
{
    public enum SortType
    {
        BY_ID,
        BY_NAME,
    }

    protected int index;
    protected Hashtable<String, UIType> table = new Hashtable<String, UIType>( 64 );

    public final Comparator<UIType> comparatorById =

        new Comparator<UIType>()
        {
            /**
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            public int compare( UIType o1, UIType o2 )
            {
                return o1.index - o2.index;
            }
        };

    public final Comparator<UIType> comparatorByName =

        new Comparator<UIType>()
        {
            /**
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            public int compare( UIType o1, UIType o2 )
            {
                return o1.name.compareTo( o2.name );
            }
        };

    /**
     * ctor
     */
    public UITypeTable()
    {
        this.index = 0;
    }

    /**
     * テーブルへの追加
     */
    public boolean add( UIType v )
    {
        final String name = v.name;
        if( table.containsKey( name ) )
        {
            // 定義済み
            return false;
        }

        v.index = index;
        index++;

        table.put( name, v );
        return true;
    }

    /**
     * 指定したシンボル名がテーブルに登録されているか検索する
     * @return あった場合は有効なインスタンス、無い場合は null
     */
    public UIType search( String name )
    {
        return table.get( name );
    }

    /**
     * 指定したシンボル名がテーブルに登録されているか検索する
     * @return あった場合はインデックス番号、無い場合は -1
     */
    public int searchID( String name )
    {
        UIType v = search( name );
        if( v == null )
        {
            return -1;
        }
        return v.index;
    }

    /**
     * 登録されているシンボルを配列形式で返す
     */
    public UIType[] toArray( SortType sortType )
    {
        Comparator<UIType> c = null;

        switch( sortType )
        {
            case BY_ID:   c = comparatorById;   break;
            case BY_NAME: c = comparatorByName; break;
            default:
                throw new IllegalArgumentException();
        }

        UIType[] array = table.values().toArray( new UIType[ 0 ] );
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
        for( Enumeration<UIType> e = table.elements(); e.hasMoreElements(); )
        {
            UIType v = e.nextElement();
            buff.append( v.name ).append( '\n' );
        }

        return buff.toString();
    }

    /**
     * デバッグ用のダンプ
     */
    public void dumpSymbol( PrintStream ps )
    {
        for( UIType v : toArray( SortType.BY_NAME ) )
        {
            ps.println( v.name );
        }
    }

}
