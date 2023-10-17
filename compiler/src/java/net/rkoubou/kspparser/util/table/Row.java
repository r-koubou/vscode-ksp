/* =========================================================================

    Column.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.util.table;

import java.util.ArrayList;

/**
 * 行を表現する基底クラス
 */
public class Row
{
    /** 行データを格納するテーブル */
    protected final ArrayList<Column> columns;

    /**
     * ctor.
     */
    public Row()
    {
        columns = new ArrayList<Column>();
    }

    /**
     * ctor.
     */
    public Row( ArrayList<Column> src )
    {
        columns = new ArrayList<Column>( src );
    }

    /**
     * 列数を取得する
     */
    public int length()
    {
        return columns.size();
    }

    /**
     * 指定された位置の列のデータを取得する
     */
    public void add( Column col )
    {
        columns.add( col );
    }

    /**
     * 指定された位置の列のデータを取得する
     */
    public Column get( int index )
    {
        return columns.get( index );
    }

    /**
     * この行のの列データを取得する
     */
    public ArrayList<Column> getColumns()
    {
        return new ArrayList<Column>( columns );
    }

    /**
     * 指定された位置の列のデータを取得する
     */
    public void clear()
    {
        columns.clear();
    }

    /**
     * Colmnインスタンスへのコンビニエンスメソッド
     */
    public Integer intValue( int index )
    {
        return columns.get( index ).intValue();
    }

    /**
     * Colmnインスタンスへのコンビニエンスメソッド
     */
    public Long longValue( int index )
    {
        return columns.get( index ).longValue();
    }

    /**
     * Colmnインスタンスへのコンビニエンスメソッド
     */
    public Boolean booleanValue( int index )
    {
        return columns.get( index ).booleanValue();

    }

    /**
     * Colmnインスタンスへのコンビニエンスメソッド
     */
    public String stringValue( int index )
    {
        return columns.get( index ).stringValue();
    }
}
