/* =========================================================================

    CallbackTable.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.analyzer.SymbolDefinition.SymbolType;
import net.rkoubou.kspparser.javacc.generated.ASTCallbackDeclaration;
import net.rkoubou.kspparser.javacc.generated.KSPParserTreeConstants;

/**
 * コールバックテーブル
 */
public class CallbackTable extends SymbolTable<ASTCallbackDeclaration, Callback> implements AnalyzerConstants, KSPParserTreeConstants
{
    /**
     * ctor
     */
    public CallbackTable()
    {
        super();
    }

    /**
     * ctor
     */
    public CallbackTable( CallbackTable parent )
    {
        super( parent );
    }

    /**
     * ctor
     */
    public CallbackTable( CallbackTable parent, int startIndex )
    {
        super( parent, startIndex );
    }

    /**
     * コールバックテーブルへの追加
     */
    @Override
    public boolean add( ASTCallbackDeclaration decl )
    {
        /*
                        CallbackDeclaration
                                    |
                    +-------------+-----------+
                    |                         |
            CallbackArgumentList            Block
        */
        Callback c = new Callback( decl );
        return add( c );
    }

    /**
     * コールバックテーブルへの追加
     */
    public boolean add( Callback c )
    {
        return add( c, c.getName() );
    }

    /**
     * コールバックテーブルへの追加
     */
    public boolean add( Callback c, String name )
    {
        if( table.containsKey( name ) )
        {
            //--------------------------------------------------------------------------
            // 宣言済み
            //--------------------------------------------------------------------------
            {
                Callback p = table.get( name );
                //--------------------------------------------------------------------------
                // 外部定義ファイルで取り込んだシンボルがソースコード上で宣言されているかどうか
                // 初回の検出時はフラグを立てるだけ
                //--------------------------------------------------------------------------
                if( p.reserved && !p.declared )
                {
                    p.declared = true;
                }
                //--------------------------------------------------------------------------
                // 多重定義を許可されていないコールバックには追加不可
                //--------------------------------------------------------------------------
                if( !p.isAllowDuplicate() )
                {
                    return false;
                }
                return true;
            }
        }
        c.index = index;
        index++;
        c.symbolType = SymbolType.Callback;
        table.put( name, c );
        return true;
    }
}
