/* =========================================================================

    CommandTable.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.analyzer.SymbolDefinition.SymbolType;
import net.rkoubou.kspparser.javacc.generated.ASTCallCommand;

/**
 * KSPコマンド定義テーブル
 */
public class CommandTable extends SymbolTable<ASTCallCommand, Command> implements AnalyzerConstants
{
    /**
     * ctor
     */
    public CommandTable()
    {
        super();
    }

    /**
     * ctor
     */
    public CommandTable( CommandTable parent )
    {
        super( parent );
    }

    /**
     * ctor
     */
    public CommandTable( CommandTable parent, int startIndex )
    {
        super( parent, startIndex );
    }

    /**
     * ユーザー定義関数テーブルへの追加
     */
    @Override
    public boolean add( ASTCallCommand decl )
    {
        return add( new Command( decl ) );
    }

    /**
     * コマンドテーブルへの追加
     */
    public boolean add( Command c )
    {
        final String name = c.getName();
        if( table.containsKey( name ) )
        {
            return false;
        }

        c.index = index;
        index++;
        c.symbolType = SymbolType.Command;
        table.put( name, c );
        return true;
    }
}
