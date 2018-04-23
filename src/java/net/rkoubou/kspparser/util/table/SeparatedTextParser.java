/* =========================================================================

    SeparatedTextParser.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.util.table;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import net.rkoubou.kspparser.util.StreamCloser;

/**
 * 区切り文字を用いたテキストファイルをパース、行毎の列データを保有する
 */
abstract public class SeparatedTextParser<T extends Row>
{
    /** 初期設定のデリミタ(TAB) */
    static public final String DEFAULT_DELIMITER= "\t";

    /** 行コメント表現(正規表現) */
    static public final Pattern REGEX_LINECOMMENT = Pattern.compile( "^\\s*#" );

    /** パース対象の文字コード（デフォルトではUTF-8） */
    protected final String delimiter;

    /** パース対象の文字コード（デフォルトではUTF-8） */
    protected final String encoding;

    /** 最大列数 */
    protected int maxColumnsNum;

    /** 行・列データTを格納する */
    protected final ArrayList<T> table = new ArrayList<T>( 256 );

    /**
     * ctor.
     */
    public SeparatedTextParser()
    {
        encoding  = "utf-8";
        delimiter = DEFAULT_DELIMITER;
    }

    /**
     * ctor.
     */
    public SeparatedTextParser( String encoding, String delimiterRegex )
    {
        this.encoding  = encoding;
        this.delimiter = delimiterRegex;
    }

    /**
     * 指定されたパスからパースを実行する
     */
    public void parse( String path ) throws IOException
    {
        parse( new File( path) );
    }

    /**
     * 指定されたパスからパースを実行する
     */
    public void parse( File file ) throws IOException
    {
        parse( new FileInputStream( file ) );
    }

    /**
     * 指定された入力ストリームからパースを実行する
     */
    public void parse( InputStream in ) throws IOException
    {
        BufferedReader br = null;
        try
        {
            String line;
            br = new BufferedReader( new InputStreamReader( in, encoding ) );
            while( ( line = br.readLine() ) != null )
            {
                boolean skip;
                String[] split;

                line = line.trim();
                skip = REGEX_LINECOMMENT.matcher( line ).find();
                if( skip )
                {
                    continue;
                }
                split   = line.split( delimiter );
                table.add( parseLine( line, split ) );
                Math.max( maxColumnsNum, split.length );
            }
        }
        finally
        {
            StreamCloser.close( br );
        }
    }

    /**
     * パース前の初期値にリセットする
     */
    public void clear()
    {
        table.clear();
        maxColumnsNum = 0;
    }

    /**
     * パースしたテーブルを取得する
     */
    public ArrayList<T> getTable()
    {
        return new ArrayList<T>( table );
    }

    /** 指定された行の列全体を取得する */
    public ArrayList<Column> getRow( int rowIndex )
    {
        T r = table.get( rowIndex );
        return new ArrayList<Column>( r.columns );
    }

    /**
     * {@link #parse(InputStream)} から行を読み込む毎に呼び出される。
     * @return 行をパースした結果
     */
    abstract protected T parseLine( String line, String[] split );
}
