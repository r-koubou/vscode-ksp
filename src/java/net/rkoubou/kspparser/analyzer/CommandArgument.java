/* =========================================================================

    CommandArgument.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.util.ArrayList;

/**
 * コマンドコール時：引数の変数の中間表現を示す
 */
public class CommandArgument implements AnalyzerConstants
{

    /** 引数を格納する（複数タイプを許容するコマンドに対応するため配列としている） */
    protected final ArrayList<Argument> arguments = new ArrayList<Argument>();

    /**
     * Ctor.
     */
    public CommandArgument()
    {
    }

    /**
     * Ctor.
     */
    public CommandArgument( Variable... args )
    {
        for( Variable a : args )
        {
            this.add( new Argument( a ) );
        }
    }

    /**
     * Ctor.
     */
    public CommandArgument( ArrayList<Variable> args )
    {
        for( Variable a : args )
        {
            this.add( new Argument( a ) );
        }
    }

    /**
     * Ctor.
     */
    public CommandArgument( Argument... args )
    {
        for( Argument a : args )
        {
            this.add( a );
        }
    }

    /**
     * 引数に複数のデータ型を許容する場合は、このメソッドにて追加をする。
     */
    public void add( Argument arg )
    {
        arg.name = "arg";
        arg.requireDeclarationOnInit = false;   // ビルトイン変数も有効なのでフラグは下ろす
        arguments.add( arg );
    }

    /**
     * 引数に複数のデータ型を許容する場合は、このメソッドにて追加をする。
     */
    public void add( Argument[] args )
    {
        for( Argument a : args )
        {
            this.add( a );
        }
    }

    /**
     * 現在の引数情報を取得する。
     */
    public ArrayList<Argument> get()
    {
        return new ArrayList<Argument>( arguments );
    }

    /**
     * 現在の許容する引数のデータ型の数を取得する。
     */
    public int getTypeNum()
    {
        return arguments.size();
    }
}
