/* =========================================================================

    Column.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.util.table;

/**
 * 列を表現するオブジェクト
 */
public class Column
{
    /** 格納する値 */
    public final Object value;

    /**
     * ctor.
     */
    public Column( Object v )
    {
        this.value = v;
    }

    /**
     * 表現可能な値の場合に有効な値を返す。表現できない場合は null を返す。
     */
    public Integer intValue()
    {
        try
        {
            return Integer.parseInt( value.toString() );
        }
        catch( Throwable e )
        {
            return null;
        }
    }

    /**
     * 表現可能な値の場合に有効な値を返す。表現できない場合は null を返す。
     */
    public Long longValue()
    {
        try
        {
            return Long.parseLong( value.toString() );
        }
        catch( Throwable e )
        {
            return null;
        }
    }

    /**
     * 表現可能な値の場合に有効な値を返す。表現できない場合は null を返す。
     */
    public Boolean booleanValue()
    {
        try
        {
            return Boolean.parseBoolean( value.toString() );
        }
        catch( Throwable e )
        {
            return null;
        }
    }

    /**
     * value.toString() を返す
     */
    public String stringValue()
    {
        return value.toString();
    }
}
