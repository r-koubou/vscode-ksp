/* =========================================================================

    UserFunctionTable.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.analyzer.SymbolDefinition.SymbolType;
import net.rkoubou.kspparser.javacc.generated.ASTUserFunctionDeclaration;

/**
 * ユーザー定義関数テーブル
 */
public class UserFunctionTable extends SymbolTable<ASTUserFunctionDeclaration, UserFunction> implements AnalyzerConstants
{
    /**
     * ctor
     */
    public UserFunctionTable()
    {
        super();
    }

    /**
     * ctor
     */
    public UserFunctionTable( UserFunctionTable parent )
    {
        super( parent );
    }

    /**
     * ctor
     */
    public UserFunctionTable( UserFunctionTable parent, int startIndex )
    {
        super( parent, startIndex );
    }

    /**
     * ユーザー定義関数テーブルへの追加
     */
    @Override
    public boolean add( ASTUserFunctionDeclaration decl )
    {
        return add( new UserFunction( decl ) );
    }

    /**
     * ユーザー定義関数テーブルへの追加
     */
    public boolean add( UserFunction c )
    {
        final String name = c.name;
        if( table.containsKey( name ) )
        {
            return false;
        }

        c.index = index;
        index++;
        c.symbolType = SymbolType.UserFunction;
        table.put( name, c );
        return true;
    }
}
