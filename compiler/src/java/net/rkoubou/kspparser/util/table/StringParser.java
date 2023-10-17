/* =========================================================================

    StringParser.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.util.table;

/**
 * Stringインスタンスを格納するデフォルト実装
 */
public class StringParser extends SeparatedTextParser<Row>
{
    /**
     * {@link #parse(InputStream)} から行を読み込む毎に呼び出される。
     */
    @Override
    protected Row parseLine( String line, String[] split )
    {
        Row ret = new Row();
        for( String s : split )
        {
            ret.add( new Column( s ) );
        }
        return ret;
    }
}
