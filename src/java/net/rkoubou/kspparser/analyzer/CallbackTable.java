/* =========================================================================

    CallbackTable.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import net.rkoubou.kspparser.analyzer.SymbolDefinition.SymbolType;
import net.rkoubou.kspparser.javacc.generated.ASTCallbackDeclaration;
import net.rkoubou.kspparser.javacc.generated.KSPParserTreeConstants;
import net.rkoubou.kspparser.javacc.generated.ASTCallbackArgumentList;

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
        if( decl.jjtGetNumChildren() == 2 )
        {
            /*
                            CallbackDeclaration
                                      |
                        +-------------+-----------+
                        |                         |
                CallbackArgumentList            Block
            */
            // 引数有り
            CallbackWithArgs c = new CallbackWithArgs( decl );
            return addWithArgs( c );
        }
        else
        {
            // 引数なし
            Callback c = new Callback( decl );
            return add( c );
        }
    }

    /**
     * コールバック（引数なし）テーブルへの追加
     */
    public boolean add( Callback c )
    {
        final String name = c.getName();
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
                    return true;
                }
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

        // NI が定義していないコールバックの可能性
        if( !c.reserved )
        {
            MessageManager.printlnW( MessageManager.PROPERTY_WARN_CALLBACK_UNKNOWN, c );
            AnalyzeErrorCounter.w();
        }

        return true;
    }

    /**
     * コールバックテーブルへの追加
     */
    public boolean addWithArgs( CallbackWithArgs c )
    {
        final String name = c.getName();
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
                if( p instanceof CallbackWithArgs && p.astNode.jjtGetNumChildren() > 0 && c.astNode.jjtGetNumChildren() > 0 )
                {
                    /*
                        ASTCallbackDeclaration
                            -> ASTCallbackArgumentList
                    */
                    CallbackWithArgs callbackWithArgs = (CallbackWithArgs)p;
                    //--------------------------------------------------------------------------
                    // 重複宣言チェック
                    //--------------------------------------------------------------------------
                    c.updateArgList();
                    if( callbackWithArgs.duplicateList.size() > 0 )
                    {
                        if( callbackWithArgs.equalsArgList( c ) )
                        {
                            return false;
                        }
                        callbackWithArgs.duplicateList.add( c );
                    }
                    else
                    {
                        // AST が持つ原始的なリストからテーブル変数へコピー
                        c.updateArgList();
                        callbackWithArgs.duplicateList.add( c );
                    }
                    return true;
                }
                else
                {
                    // 引数を持たないが、多重宣言が許可されている
                    // 同一名のシンボルは追加済みなのでここで終わり
                    return true;
                }
            }
        }// ~if( table.containsKey( name ) )
        else
        {
            // 初回登録（＝ReservedSymbolManagerによる、コールバック定義ファイルからのパース結果時）
            c.index = index;
            index++;
            c.symbolType = SymbolType.Callback;
            table.put( name, c );

            // NI が定義していないコールバックの可能性
            if( !c.reserved )
            {
                MessageManager.printlnW( MessageManager.PROPERTY_WARN_CALLBACK_UNKNOWN, c );
                AnalyzeErrorCounter.w();
            }

            return true;
        }
    }
}
