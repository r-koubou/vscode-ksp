/* =========================================================================

    Position.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.javacc.generated.Token;

/**
 * シンボル等の行番号、列を格納する
 */
public class Position
{
    public int beginLine    = 0;
    public int endLine      = 0;
    public int beginColumn  = 0;
    public int endColumn    = 0;

    /**
     * ctor.
     */
    public Position(){}

    /**
     * コピーコンストラクタ
     */
    public Position( Position src )
    {
        copy( src, this );
    }

    /**
     * ディープコピー
     */
    static public void copy( Position src, Position dest )
    {
        dest.beginLine      = src.beginLine;
        dest.endLine        = src.endLine;
        dest.beginColumn    = src.beginColumn;
        dest.endColumn      = src.endColumn;
    }

    /**
     * ディープコピー
     */
    public void copy( Position src )
    {
        this.beginLine      = src.beginLine;
        this.endLine        = src.endLine;
        this.beginColumn    = src.beginColumn;
        this.endColumn      = src.endColumn;
    }

    /**
     * Tokenからの値のディープコピー
     */
    static public void copy( Token src, Position dest )
    {
        dest.beginLine     = src.beginLine;
        dest.endLine       = src.endLine;
        dest.beginColumn   = src.beginColumn;
        dest.endColumn     = src.endColumn;
    }

    /**
     * Tokenからの値のディープコピー
     */
    public void copy( Token src )
    {
        this.beginLine      = src.beginLine;
        this.endLine        = src.endLine;
        this.beginColumn    = src.beginColumn;
        this.endColumn      = src.endColumn;
    }

    /**
     * TokenからPositionを生成するコンビニエンスメソッド
     */
    static public Position create( Token t )
    {
        Position p = new Position();
        p.beginLine     = t.beginLine;
        p.endLine       = t.endLine;
        p.beginColumn   = t.beginColumn;
        p.endColumn     = t.endColumn;
        return p;
    }

    /**
     * 行数カウント
     */
    public int lineCount()
    {
        return endLine - beginLine + 1;
    }

    /**
     * 列数カウント
     */
    public int columnCount()
    {
        return endColumn - beginColumn + 1;
    }
}