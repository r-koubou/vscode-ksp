/* =========================================================================

    CallbackTable.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.util.ArrayList;

import net.rkoubou.kspparser.analyzer.SymbolDefinition.SymbolType;
import net.rkoubou.kspparser.javacc.generated.ASTCallbackDeclaration;
import net.rkoubou.kspparser.javacc.generated.ASTCallbackArgumentList;

/**
 * コールバックテーブル
 */
public class CallbackTable extends SymbolTable<ASTCallbackDeclaration, Callback> implements AnalyzerConstants
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
        Callback c;
        if( decl.jjtGetNumChildren() > 0 )
        {
            // 引数有り
            c = new Callback( decl );
        }
        else
        {
            // 引数なし
            c = new CallbackWithArgs( decl );
        }
        return add( c );
    }

    /**
     * コールバック（引数なし）テーブルへの追加
     */
    public boolean add( Callback c )
    {
        final String name = c.name;
        if( table.containsKey( name ) )
        {
            //--------------------------------------------------------------------------
            // 宣言済み
            //--------------------------------------------------------------------------
            {
                Callback p = table.get( name );
                //--------------------------------------------------------------------------
                // 多重定義を許可されていないコールバックには追加不可
                //--------------------------------------------------------------------------
                if( !p.allowDuplicate )
                {
                    return false;
                }
                else
                {
                    // 多重宣言が許可されている
                    // 同一名のシンボルは追加済みなので引数の変数は使用せずにここで終わり
                    return true;
                }
            }
        }// ~if( table.containsKey( name ) )

        c.index = index;
        index++;

        c.symbolType = SymbolType.Callback;
        table.put( name, c );
        return true;
    }

    /**
     * コールバックテーブルへの追加
     */
    public boolean add( CallbackWithArgs c )
    {
        final String name = c.name;
        if( table.containsKey( name ) )
        {
            //--------------------------------------------------------------------------
            // 宣言済み
            //--------------------------------------------------------------------------
            {
                Callback p = table.get( name );
                //--------------------------------------------------------------------------
                // 多重定義を許可されていないコールバックには追加不可
                //--------------------------------------------------------------------------
                if( !p.allowDuplicate )
                {
                    return false;
                }
                //--------------------------------------------------------------------------
                // on ui_control のように、複数宣言可能な場合はそのコールバックに
                // 今回のコールバックの引数リストを保管させておく
                // この段階ではまだ型チェックは行わない。
                //--------------------------------------------------------------------------
                if( p instanceof CallbackWithArgs && p.astNode.jjtGetNumChildren() > 0 )
                {
                    ASTCallbackArgumentList list      = (ASTCallbackArgumentList)p.astNode.jjtGetChild( 0 );
                    CallbackWithArgs callbackWithArgs = (CallbackWithArgs)p;
                    /*
                        ASTCallbackDeclaration
                            -> ASTCallbackArgumentList
                    */
                    boolean match = true;
                    for( String n : list.args )
                    {
                        if( !callbackWithArgs.contains( n ) )
                        {
                            match = false;
                            break;
                        }
                    }
                    if( match )
                    {
                        c.index = index;
                        index++;

                        c.symbolType = SymbolType.Callback;
                        callbackWithArgs.duplicateList.add( c );
                        return true;
                    }
                }
                else
                {
                    // 引数を持たないが、多重宣言が許可されている
                    // 同一名のシンボルは追加済みなのでここで終わり
                    return true;
                }
            }
        }// ~if( table.containsKey( name ) )

        c.index = index;
        index++;

        c.symbolType = SymbolType.Callback;
        table.put( name, c );
        return true;
    }
}
