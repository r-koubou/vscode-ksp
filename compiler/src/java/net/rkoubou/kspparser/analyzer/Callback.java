/* =========================================================================

    Callback.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.util.ArrayList;

import net.rkoubou.kspparser.javacc.generated.ASTCallbackArgumentList;
import net.rkoubou.kspparser.javacc.generated.ASTCallbackDeclaration;
import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclaration;
import net.rkoubou.kspparser.javacc.generated.KSPParserTreeConstants;

/**
 * コールバック（引数なし）の中間表現を示す
 */
public class Callback extends SymbolDefinition implements KSPParserTreeConstants
{
    /** getName() メソッドにて、内部ユニークIDを付与する場合に使用するデリミタ文字 */
    static public String DUPLICATE_DELIMITER = "<>";
    /** ユニークIDカウンタ */
    static protected long duplicateCounter = 0L;

    /** 元となるASTノード */
    public final ASTCallbackDeclaration astNode;

    /** 多重宣言を許可するかどうか(例：on ui_controlなどは複数宣言可能) */
    private boolean allowDuplicate;

    /** 外部定義ファイルからの取り込みとの組み合わせで使用する(allowDuplicate) */
    public boolean declared = false;

    /** このコールバックに割り当てられているユニークID */
    protected Long duplicateID = null;

    /** 引数リスト */
    public final ArrayList<Argument> argList = new ArrayList<Argument>();

    /**
     * 値コピー
     */
    static public void copy( Callback src, Callback dest )
    {
        SymbolDefinition.copy( src, dest );
        dest.allowDuplicate = src.allowDuplicate;
        dest.declared       = src.declared;
        dest.argList.clear();
        dest.argList.addAll( src.argList );
        dest.duplicateID    = null;
        dest.setAllowDuplicate( src.isAllowDuplicate() );
    }

    /**
     * Ctor.
     */
    public Callback( ASTCallbackDeclaration node )
    {
        SymbolDefinition.copy( node.symbol, this );
        this.astNode        = node;
        this.symbolType     = SymbolType.Callback;
        updateArgList();
    }

    /**
     * Ctor.
     */
    public Callback( Callback c )
    {
        astNode = c.astNode;
        Callback.copy( c, this );
    }

    /**
     * ASTノードCallbackArgumentListに格納されている変数文字列を元にargListを更新する。
     */
    public void updateArgList()
    {
        if( astNode.jjtGetNumChildren() < 2 )
        {
            // 引数リストなし
            argList.clear();
            return;
        }
        ASTCallbackArgumentList list = (ASTCallbackArgumentList)astNode.jjtGetChild( 0 );
        argList.clear();
        for( String n : list.args )
        {
            ASTVariableDeclaration decl = new ASTVariableDeclaration( JJTVARIABLEDECLARATION );
            decl.symbol.symbolType = SymbolType.Variable;
            decl.symbol.setName( n );

            Argument arg = new Argument( decl );
            argList.add( arg );
        }
    }

    /**
     * 内部ユニークIDのインクリメント
     */
    static protected void incDuplicateID()
    {
        duplicateCounter++;
        if( duplicateCounter == Long.MAX_VALUE )
        {
            throw new RuntimeException( "No more generate ID!" );
        }
    }

    /**
     * このコールバックが多重定義を許容しているかどうか
     */
    public boolean isAllowDuplicate()
    {
        return allowDuplicate;
    }

    /**
     * このコールバックが多重定義を許容するかを設定する。
     * @throws RuntimeException flag が true、かつ isAllowDuplicate() が true を返す条件を満たしている場合
     */
    public void setAllowDuplicate( boolean flag )
    {
        if( flag )
        {
            if( duplicateID != null )
            {
                throw new RuntimeException( "Already set allow duplication flag" );
            }
            duplicateID = duplicateCounter;
            incDuplicateID();
        }
        else
        {
            duplicateID = null;
        }
        allowDuplicate = flag;
    }

    /**
     * ユニークIDを付与した内部形式でのシンボル名を返す
     */
    @Override
    public String getName()
    {
        if( allowDuplicate )
        {
            return super.getName() + DUPLICATE_DELIMITER + duplicateID;
        }
        return super.getName();
    }
}
