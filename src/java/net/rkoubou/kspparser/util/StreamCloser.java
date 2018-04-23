/* =========================================================================

    StreamCloser.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;

/**
 * 各種ストリームを安全にクローズする
 */
public class StreamCloser
{
    /**
     * ctor.
     */
    private StreamCloser(){}

    /**
     * 指定されたストリームのクローズ
     */
    static public void close( InputStream in )
    {
        try{ in.close(); } catch( Throwable e ){}
    }

    /**
     * 指定されたストリームのクローズ
     */
    static public void close( OutputStream out )
    {
        try{ out.flush(); } catch( Throwable e ){}
        try{ out.close(); } catch( Throwable e ){}
    }

    /**
     * 指定されたストリームのクローズ
     */
    static public void close( Reader r )
    {
        try{ r.close(); } catch( Throwable e ){}
    }

    /**
     * 指定されたストリームのクローズ
     */
    static public void close( Writer w )
    {
        try{ w.flush(); } catch( Throwable e ){}
        try{ w.close(); } catch( Throwable e ){}
    }

    /**
     * 指定されたストリームのクローズ
     */
    static public void close( PrintStream ps )
    {
        try{ ps.flush(); } catch( Throwable e ){}
        try{ ps.close(); } catch( Throwable e ){}
    }
}
