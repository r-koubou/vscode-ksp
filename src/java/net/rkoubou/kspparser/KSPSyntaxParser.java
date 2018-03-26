/* =========================================================================

    KSPSyntaxParser.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.Writer;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import net.rkoubou.kspparser.analyzer.AnalyzeErrorCounter;
import net.rkoubou.kspparser.analyzer.SemanticAnalyzer;
import net.rkoubou.kspparser.analyzer.SymbolCollector;
import net.rkoubou.kspparser.javacc.generated.ASTRootNode;
import net.rkoubou.kspparser.javacc.generated.KSPParser;
import net.rkoubou.kspparser.obfuscator.Obfuscator;
import net.rkoubou.kspparser.options.CommandlineOptions;;

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
        PrintStream stdout          = null;
        PrintStream stderr          = null;
        CmdLineParser cmdLineParser = null;
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
            // コマンドライン引数の解析
            cmdLineParser = CommandlineOptions.setup( args );
            if( CommandlineOptions.options.usage )
            {
                usage( cmdLineParser );
                System.exit( 1 );
                return;
            }
            if( CommandlineOptions.options.sourceFile == null )
            {
                usage( cmdLineParser );
                System.exit( 1 );
                return;
            }

            // プログラム解析
            File file            = new File( CommandlineOptions.options.sourceFile );
            KSPParser p          = new KSPParser( file );

            // 構文解析フェーズ
            ASTRootNode rootNode = p.analyzeSyntax();
            if( rootNode == null || AnalyzeErrorCounter.hasError() )
            {
                System.exit( 1 );
                return;
            }

            // シンボル収集フェーズ
            SymbolCollector symbolCollector = new SymbolCollector( rootNode );
            AnalyzeErrorCounter.reset();
            symbolCollector.analyze();
            if( AnalyzeErrorCounter.hasError() )
            {
                System.exit( 1 );
                return;
            }

            // 意味解析フェーズ
            if( !CommandlineOptions.options.parseonly )
            {
                SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer( symbolCollector );
                AnalyzeErrorCounter.reset();
                semanticAnalyzer.analyze();

                if( AnalyzeErrorCounter.hasError() )
                {
                    System.exit( 1 );
                    return;
                }

                // オブファスケートは意味解析フェーズで構築したASTが必要なため
                if( CommandlineOptions.options.obfuscate )
                {
                    Obfuscator obfuscator = new Obfuscator( rootNode, symbolCollector );
                    obfuscator.analyze();

                    // ファイルに出力
                    String outputFile = CommandlineOptions.options.outputFile;
                    if( outputFile != null )
                    {
                        Writer writer = null;
                        try
                        {
                            writer = new FileWriter( outputFile, false );
                            writer.write( obfuscator.toString() );
                        }
                        finally
                        {
                            if( writer != null )
                            {
                                try{ writer.flush(); } catch( Throwable e ){}
                                try{ writer.close(); } catch( Throwable e ){}
                            }
                        }
                    }
                    // 標準出力に出力
                    else
                    {
                        System.out.println( obfuscator );
                    }
                }
                System.exit( 0 );
                return;
            }
        }
        catch( CmdLineException cmd )
        {
            System.err.println( cmd.getMessage() );
            usage( cmdLineParser );
            System.exit( 1 );
        }
        catch( FileNotFoundException fne )
        {
            System.err.println( "script not found : " + fne.getMessage() );
        }
        finally
        {
            //AnalyzeErrorCounter.dump( System.out );
            if( stdout != null ){ try{ stdout.close(); } catch( Throwable e ){} }
            if( stderr != null ){ try{ stdout.close(); } catch( Throwable e ){} }
        }
    }

    /**
     * コマンドライン引数の表示
     */
    static private void usage( CmdLineParser p )
    {
        System.err.println( "usage" );
        System.err.println( "  java -jar ./KSPSyntaxParser.jar [options] source" );
        if( p != null )
        {
            System.err.println();
            p.printUsage( System.err );
        }
        else
        {
            System.err.println( "please type -h to show usage" );
        }
        System.err.println();
    }

}
