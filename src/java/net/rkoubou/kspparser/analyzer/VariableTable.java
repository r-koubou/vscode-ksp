/* =========================================================================

    VariableTable.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.analyzer.Variable;
import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclaration;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;


/**
 * 変数シンボルテーブル
 */
public class VariableTable implements AnalyzerConstants
{

    public enum SortType
    {
        BY_ID,
        BY_TYPE,
    }

    VariableTable parent;

    int index;
    Hashtable<String, Variable> table = new Hashtable<String, Variable>( 64 );

    /**
     * ctor
     */
    public VariableTable()
    {
        this.index = 0;
        parent     = null;
    }

    /**
     * ctor
     */
    public VariableTable( VariableTable parent )
    {
        this.index  = parent.index;
        this.parent = parent;
    }

    /**
     * ctor
     */
    public VariableTable( VariableTable parent, int startIndex )
    {
        this.index  = startIndex;
        this.parent = parent;
    }

    /**
     * 変数テーブルへの追加
     */
    public boolean add( ASTVariableDeclaration decl )
    {
        final String name = decl.symbol.name;
        if( table.containsKey( name ) )
        {
            // 宣言済み
            return false;
        }

        Variable v = new Variable( decl );

        if( v.isConstant() )
        {
            v.index = -1;
        }
        else
        {
            v.index = index;
            index++;
        }
        table.put( name, v );
        return true;
    }

    /**
     * 指定した変数名がテーブルに登録されているか検索する
     * @return あった場合は有効なインスタンス、無い場合は null
     */
    public Variable searchVariable( String name, boolean enableSearchParent )
    {
        Variable v = table.get( name );
        if( v == null && enableSearchParent )
        {
            VariableTable p = parent;
            while( p != null )
            {
                v = p.table.get( name );
                if( v != null )
                {
                    return v;
                }
                p = p.parent;
            }
            return null;
        }
        return v;
    }

    /**
     * 指定した変数名がテーブルに登録されているか検索する
     * @return あった場合は有効なインスタンス、無い場合は null
     */
    public Variable searchVariable( String name )
    {
        return searchVariable( name, true );
    }

    /**
     * 指定した変数名がテーブルに登録されているか検索する
     * @return あった場合はインデックス番号、無い場合は -1
     */
    public int searchVariableID( String name, boolean enableSearchParent )
    {
        Variable v = searchVariable( name, enableSearchParent );
        if( v == null )
        {
            VariableTable p = parent;
            while( p != null )
            {
                v = p.table.get( name );
                if( v != null )
                {
                    return v.index;
                }
                p = p.parent;
            }
            return -1;
        }
        return v.index;
    }

    /**
     * 指定した変数名がテーブルに登録されているか検索する
     * @return あった場合はインデックス番号、無い場合は -1
     */
    public int searchVariableID( String name )
    {
        return searchVariableID( name, true );
    }

    /**
     * 登録されている変数を配列形式で返す
     */
    public Variable[] toArray( SortType sortType )
    {
        Comparator<Variable> c = null;

        switch( sortType )
        {
            case BY_ID:   c = Variable.comparatorById;   break;
            case BY_TYPE: c = Variable.comparatorByType; break;
            default:
                throw new IllegalArgumentException();
        }

        Variable[] array = table.values().toArray( new Variable[ 0 ] );
        if( array.length > 0 )
        {
            Arrays.sort( array, c );
        }

        return array;

    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder buff = new StringBuilder( 64 );
        for( Enumeration<Variable> e = table.elements(); e.hasMoreElements(); )
        {
            Variable v = e.nextElement();
            buff.append( v.toString() ).append( '\n' );
        }

        return buff.toString();
    }

    /**
     * デバッグ用のダンプ
     */
    public void dumpSymbol( PrintStream ps )
    {
        for( Variable v : toArray( SortType.BY_TYPE) )
        {
            ps.println( v.name );
        }
    }

}
