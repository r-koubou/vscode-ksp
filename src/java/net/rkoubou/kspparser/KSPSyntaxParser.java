/* =========================================================================

    KSPSyntaxParser.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser;

import java.io.File;

import net.rkoubou.kspparser.javacc.generated.KSPParser;

/**
 * KSPSyntaxParser
 */
public class KSPSyntaxParser
{
    //////////////////////////////////////////////////////////////////////////
    /**
     * commant
     */
    static public void main( String[] args ) throws Throwable
    {
        File file = new File( args[ 0 ] );
        KSPParser p = new KSPParser( file );
        p.analyzeSyntax();
    }
}
