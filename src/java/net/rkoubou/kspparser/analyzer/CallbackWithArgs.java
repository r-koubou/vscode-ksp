/* =========================================================================

    CallbackWithArgs.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.util.ArrayList;

import net.rkoubou.kspparser.javacc.generated.ASTCallbackArgumentList;
import net.rkoubou.kspparser.javacc.generated.ASTCallbackDeclaration;
import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclaration;
import net.rkoubou.kspparser.javacc.generated.KSPParserTreeConstants;

/**
 * コールバック（引数あり）の中間表現を示す
 */
public class CallbackWithArgs extends Callback implements KSPParserTreeConstants
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
    public void add( String name )
    {
        ASTVariableDeclaration decl = new ASTVariableDeclaration( JJTVARIABLEDECLARATION );
        decl.symbol.name = name;
        decl.symbol.symbolType = SymbolType.Variable;

        Argument a = new Argument( decl );
        a.setTypeFromVariableName();
        argList.add( a );
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
     * ASTノードCallbackArgumentListに格納されている変数文字列を元にargListを更新する。
     */
    public void updateArgList()
    {
        ASTCallbackArgumentList list = (ASTCallbackArgumentList)astNode.jjtGetChild( 0 );
        argList.clear();
        for( String n : list.args )
        {
            ASTVariableDeclaration decl = new ASTVariableDeclaration( JJTVARIABLEDECLARATION );
            decl.symbol.symbolType = SymbolType.Variable;
            decl.symbol.name = n;

            Argument arg = new Argument( decl );
            argList.add( arg );
        }
    }

    /**
     * このコールバックと指定されたコールバックが持つ引数リスト内容（変数名）が同一かどうか
     */
    public boolean equalsArgList( CallbackWithArgs o )
    {
        for( CallbackWithArgs c : duplicateList )
        {
            for( Argument a1 : c.argList )
            {
                for( Argument a2 : o.argList )
                {
                    if( a1.name.equals( a2.name ) )
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
