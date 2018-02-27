/* =========================================================================

    ReturnType.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.util.ArrayList;

/**
 * コマンドの戻り値の中間表現を示す
 */
public class ReturnType implements AnalyzerConstants
{
    /** データ型を格納する（複数タイプを許容するコマンドに対応するため配列としている） */
    public final ArrayList<Integer> typeList = new ArrayList<Integer>();

    /**
     * ctor.
     */
    public ReturnType()
    {
    }

    /**
     * ctor.
     */
    public ReturnType( int... type )
    {
        for( int i : type )
        {
            typeList.add( i );
        }
    }

    /**
     * 戻り値を保有していない場合は true を返す
     */
    public boolean empty()
    {
        return typeList.isEmpty();
    }

    /**
     * 指定されたタイプ値を保有しているかどうか
     * @see AnalyzerConstants
     */
    public boolean contains( int type )
    {
        for( int i : typeList )
        {
            if( i == type )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for( int t : typeList )
        {
            sb.append( SymbolDefinition.toKSPTypeCharacter( t ) );
        }
        return sb.toString();
    }

}
