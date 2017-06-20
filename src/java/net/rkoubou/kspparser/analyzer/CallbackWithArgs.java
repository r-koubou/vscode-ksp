/* =========================================================================

    Callback.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.util.ArrayList;

import net.rkoubou.kspparser.javacc.generated.ASTCallbackDeclaration;

/**
 * コールバック（引数あり）の中間表現を示す
 */
public class CallbackWithArgs extends Callback
{
    /** 引数リスト */
    public final ArrayList<Argument> argList = new ArrayList<Argument>();

    /** 同名コールバックのリスト（on ui_contolのように多重宣言を許可する場合のみに格納する） */
    public final ArrayList<CallbackWithArgs> duplicateList = new ArrayList<CallbackWithArgs>( 64 );

    /**
     * Ctor.
     */
    public CallbackWithArgs( ASTCallbackDeclaration node )
    {
        super( node );
    }

    /**
     * 指定された変数名が引数リストに含まれているかどうか
     */
    public boolean contains( String name )
    {
        for( Variable v : argList )
        {
            if( name.equals( v.name ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 指定されたコールバック名が同名コールバックのリストに含まれているかどうか
     */
    public boolean containsDuplicate( String name )
    {
        for( CallbackWithArgs c : duplicateList )
        {
            if( name.equals( c.name ) )
            {
                return true;
            }
        }
        return false;
    }
}
