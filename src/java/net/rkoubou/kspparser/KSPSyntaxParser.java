/* =========================================================================

    KSPSyntaxParser.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser;

import java.io.File;
import java.io.PrintStream;

import net.rkoubou.kspparser.analyzer.AnalyzeErrorCounter;
import net.rkoubou.kspparser.analyzer.AnalyzerOption;
import net.rkoubou.kspparser.analyzer.SemanticAnalyzer;
import net.rkoubou.kspparser.analyzer.SymbolCollector;
import net.rkoubou.kspparser.javacc.generated.ASTRootNode;
import net.rkoubou.kspparser.javacc.generated.KSPParser;
import net.rkoubou.kspparser.obfuscator.Obfuscator;

/**
 * KSPSyntaxParser
 */
public class KSPSyntaxParser
{
    //////////////////////////////////////////////////////////////////////////
    /**
     * アプリケーション・エントリポイント
     */
    static public void main( String[] args ) throws Throwable
    {
        PrintStream stdout = null;
        PrintStream stderr = null;
        try
        {
            // -Dkspparser.stdout.encoding=#### の指定があった場合、そのエンコードを標準出力・エラーに再設定する
            if( System.getProperty( "kspparser.stdout.encoding" ) != null )
            {
                String encoding = System.getProperty( "kspparser.stdout.encoding" );
                stdout = new PrintStream( System.out, true, encoding );
                stderr = new PrintStream( System.err, true, encoding );
                System.setOut( stdout );
                System.setErr( stderr );
            }
            File file            = new File( args[ 0 ] );
            KSPParser p          = new KSPParser( file );

            // 構文解析フェーズ
            ASTRootNode rootNode = p.analyzeSyntax();
            if( rootNode == null || AnalyzeErrorCounter.hasError() )
            {
                return;
            }


            // シンボル収集フェーズ
            SymbolCollector symbolCollector = new SymbolCollector( rootNode );
            AnalyzeErrorCounter.reset();
            symbolCollector.analyze();
            if( AnalyzeErrorCounter.hasError() )
            {
                return;
            }

            // 意味解析フェーズ
            if( !AnalyzerOption.parseonly )
            {
                SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer( symbolCollector );
                AnalyzeErrorCounter.reset();
                semanticAnalyzer.analyze();
                if( AnalyzeErrorCounter.hasError() )
                {
                    return;
                }

            }

            {
                Obfuscator obfuscator = new Obfuscator( rootNode, symbolCollector );
                obfuscator.analyze();
                System.out.println( obfuscator );
            }
        }
        finally
        {
            //AnalyzeErrorCounter.dump( System.out );
            if( stdout != null ){ try{ stdout.close(); } catch( Throwable e ){} }
            if( stderr != null ){ try{ stdout.close(); } catch( Throwable e ){} }
        }
    }
}
