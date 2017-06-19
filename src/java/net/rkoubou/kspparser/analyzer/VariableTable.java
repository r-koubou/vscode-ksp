/* =========================================================================

    VariableTable.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.analyzer.Variable;
import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclaration;


/**
 * 変数テーブル
 */
public class VariableTable extends SymbolTable<ASTVariableDeclaration, Variable> implements AnalyzerConstants
{
    /**
     * ctor
     */
    public VariableTable()
    {
        super();
    }

    /**
     * ctor
     */
    public VariableTable( VariableTable parent )
    {
        super( parent );
    }

    /**
     * ctor
     */
    public VariableTable( VariableTable parent, int startIndex )
    {
        super( parent, startIndex );
    }

    /**
     * 変数テーブルへの追加
     */
    @Override
    public boolean add( ASTVariableDeclaration decl, SymbolDefinition.SymbolType type )
    {
        return add( new Variable( decl ) );
    }

    /**
     * 変数テーブルへの追加
     */
    public boolean add( Variable v )
    {
        final String name = v.name;
        if( table.containsKey( name ) )
        {
            // 宣言済み
            return false;
        }

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
        v.setTypeFromVariableName();
        return true;
    }
}
