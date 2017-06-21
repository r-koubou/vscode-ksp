/* =========================================================================

    UserFunctionTable.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.analyzer.SymbolDefinition.SymbolType;
import net.rkoubou.kspparser.javacc.generated.ASTPreProcessorDefine;
import net.rkoubou.kspparser.javacc.generated.ASTPreProcessorUnDefine;
import net.rkoubou.kspparser.javacc.generated.KSPParserTreeConstants;

/**
 * プリプロセッサシンボルテーブル
 */
public class PreProcessorSymbolTable extends SymbolTable<ASTPreProcessorDefine, PreProcessorSymbol> implements KSPParserTreeConstants
{
    /**
     * ctor
     */
    public PreProcessorSymbolTable()
    {
        super();
    }

    /**
     * ctor
     */
    public PreProcessorSymbolTable( PreProcessorSymbolTable parent )
    {
        super( parent );
    }

    /**
     * ctor
     */
    public PreProcessorSymbolTable( PreProcessorSymbolTable parent, int startIndex )
    {
        super( parent, startIndex );
    }

    /**
     * プリプロセッサシンボルテーブルへの追加
     */
    @Override
    public boolean add( ASTPreProcessorDefine decl )
    {
        return add( new PreProcessorSymbol( decl ) );
    }

    /**
     * プリプロセッサシンボルテーブルへの追加
     */
    public boolean add( PreProcessorSymbol c )
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

    /**
     * UNDEFによるプリプロセッサシンボルテーブルからの除去
     */
    public boolean remove( ASTPreProcessorUnDefine undef )
    {
        ASTPreProcessorDefine d = new ASTPreProcessorDefine( JJTPREPROCESSORDEFINE );
        SymbolDefinition.copy( undef.symbol, d.symbol );
        return remove( new PreProcessorSymbol( d ) );
    }

    /**
     * UNDEFによるプリプロセッサシンボルテーブルからの除去
     */
    public boolean remove( PreProcessorSymbol c )
    {
        final String name = c.name;
        if( table.containsKey( name ) )
        {
            return false;
        }
        table.remove( name );
        return true;
    }
}
