/* =========================================================================

    KSPParserError.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.javacc;

import java.io.File;

/**
 *
 */
public class KSPParserError extends Exception
{

    static private int errorNum = 0;

    //////////////////////////////////////////////////////////////////////////
    /**
     * ファイル名、行番号を付加したメッセージつきの KSPParserError を生成する。
     */
    static public KSPParserError create( ASTKSPNode n, String message )
    {
        if( n != null )
        {
            File f = n.parser.getFile();
            return new KSPParserError( f + ":" + n.line + ": " + message );
        }
        else
        {
            return new KSPParserError( message );
        }
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     */
    public KSPParserError()
    {
        super();
        errorNum++;
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * @param message
     */
    public KSPParserError( String message )
    {
        super( message );
        errorNum++;

    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * @param cause
     */
    public KSPParserError( Throwable cause )
    {
        super( cause );
        errorNum++;
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * @param message
     * @param cause
     */
    public KSPParserError( String message, Throwable cause )
    {
        super( message, cause );
        errorNum++;
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * @return Returns the errorNum.
     */
    public static int getErrorNum()
    {
        return errorNum;
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     *
     */
    public static void resetErrorNum()
    {
        KSPParserError.errorNum = 0;
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     *
     */
    public static void incrErrorNum()
    {
        KSPParserError.errorNum++;
    }
}
