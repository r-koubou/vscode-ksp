/* =========================================================================

    ASTKSPNode.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.javacc;

import net.rkoubou.kspparser.analyzer.SymbolDefinition;

import net.rkoubou.kspparser.javacc.generated.KSPParser;
import net.rkoubou.kspparser.javacc.generated.Node;
import net.rkoubou.kspparser.javacc.generated.KSPParserTreeConstants;

/**
 * ASTの基底クラス
 */
abstract public class ASTKSPNode implements Node, KSPParserTreeConstants
{
    public Node          parent;
    public Node[]        children;
    public int           id;
    public Object        value;
    public KSPParser     parser;

    public final SymbolDefinition symbol = new SymbolDefinition();

    /**
     * このノードが親のノードから見てどの位置にいるかを調べる。
     * このノードがルートの場合やどの親ノードにも属さない場合は -1を返す。
     */
    public int getNodeIndexInParent()
    {
        if( getParent() == null )
        {
            // this == root
            return -1;
        }

        Node[] ch = ( (ASTKSPNode) getParent() ).getChildren();

        if( ch != null )
        {
            for( int i = 0; i < ch.length; i++ )
            {
                if( ch[ i ] == this )
                {
                    return i;
                }
            }
        }

        // not found
        return -1;

    }

    public ASTKSPNode getKSPNodeParent()
    {
        if( getParent() == null ) return null;
        return (ASTKSPNode) getParent();
    }

/*
    /**
     * このノードが演算子かどうかを判定する。
     */
    public boolean isOperator()
    {
        switch( id )
        {
            case JJTADD:            // +
            case JJTSUB:            // -
            case JJTMUL:            // *
            case JJTDIV:            // /
            case JJTMOD:            // mod
            case JJTAND:            // .and.
            case JJTINCLUSIVEOR:    // .or.
            case JJTEQUAL:          // =
            case JJTNOTEQUAL:       // #
            case JJTLT:             // <
            case JJTLE:             // <=
            case JJTGT:             // >
            case JJTGE:             // >=
            case JJTNEG:            // -
                return true;

            default:
                return false;
        }
    }

    /**
     * このノードが代入演算子かどうか判定する
     */
    public boolean isAssignOperator()
    {
        return id == JJTASSIGNMENT; // :=
    }

    /**
     * このノードが単項演算子かどうか判定する
     */
    public boolean isSingleOperator()
    {
        switch( id )
        {
            case JJTNEG:                // -
                return true;
            default:
                return false;
        }
    }

    /**
     * このノードが比較演算子かどうか判定する
     */
    public boolean isConditionOperator()
    {
        switch( id )
        {
            case JJTEQUAL:              // =
            case JJTNOTEQUAL:           // #
            case JJTLT:                 // <
            case JJTLE:                 // <=
            case JJTGT:                 // >
            case JJTGE:                 // >=
                return true;
        }
        return false;
    }

    // /**
    //  * データ型の文字列表現を取得する
    //  */
    // static public String getTypeName( SymbolDefinition sym )
    // {
    //     String ret = "unknown";

    //     switch( sym.type )
    //     {
    //         case TYPE_INT:    ret = "int";      break;
    //         case TYPE_STRING: ret = "string";   break;
    //         case TYPE_REAL:   ret = "real";     break;
    //     }
    //     switch( sym.type & TYPE_ATTR_MASK )
    //     {
    //         case TYPE_ATTR_ARRAY: ret += "[]";  break;
    //     }

    //     return ret;
    // }

    // /**
    //  * コールバック・ユーザー定義関数の識別子の文字列表現を取得する
    //  */
    // static public String getFunctionTypeName( SymbolDefinition sym )
    // {
    //     String ret = "unknown";

    //     switch( sym.type )
    //     {
    //         case FUNCTION_TYPE_CALLBACK: ret = "Callback";      break;
    //         case FUNCTION_TYPE_USER_DEF: ret = "User Function";      break;
    //     }
    //     return ret;
    // }

    /**
     * シンボルテーブルの作成。
     */
    public void collectSymbol() throws KSPParserError
    {}

    /**
     * 意味解析
     */
    public void semanticAnalyze() throws KSPParserError
    {}

//------------------------------------------------------------------------------
// setter / getter
//------------------------------------------------------------------------------

    public Node getParent()
    {
        return parent;
    }

    public Node[] getChildren()
    {
        return children;
    }

    public KSPParser getParser()
    {
        return parser;
    }


}
