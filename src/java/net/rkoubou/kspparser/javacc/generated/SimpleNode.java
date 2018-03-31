/* Generated By:JJTree: Do not edit this line. SimpleNode.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=net.rkoubou.kspparser.javacc.ASTKSPNode,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package net.rkoubou.kspparser.javacc.generated;

import net.rkoubou.kspparser.analyzer.SymbolDefinition;

public
class SimpleNode extends net.rkoubou.kspparser.javacc.ASTKSPNode implements Node
{

    protected Node parent;
    protected Node[] children;
    protected int id;
    protected Object value;
    protected KSPParser parser;

    public SimpleNode( int i )
    {
        id = i;
    }

    public SimpleNode( KSPParser p, int i )
    {
        this( i );
        parser = p;
    }

    public void jjtOpen(){}

    public void jjtClose(){}

    public void jjtSetParent( Node n ) { parent = n; }
    public Node jjtGetParent() { return parent; }

    public void jjtAddChild( Node n, int i )
    {
        if ( children == null )
        {
            children = new Node[i + 1];
        }
        else if ( i >= children.length )
        {
            Node c[] = new Node[i + 1];
            System.arraycopy( children, 0, c, 0, children.length );
            children = c;
        }
        children[i] = n;
    }

    public Node jjtGetChild( int i )
    {
        return children[i];
    }

    public int jjtGetNumChildren()
    {
        return ( children == null ) ? 0 : children.length;
    }

    public void jjtSetValue( Object value ) { this.value = value; }
    public Object jjtGetValue() { return value; }

    /** Accept the visitor. **/
    public Object jjtAccept( KSPParserVisitor visitor, Object data )
    {
        return visitor.visit( this, data );
    }

    /** Accept the visitor. **/
    public Object childrenAccept( KSPParserVisitor visitor, Object data )
    {
        if ( children != null )
        {
            for ( int i = 0; i < children.length; ++i )
            {
                children[i].jjtAccept( visitor, data );
            }
        }
        return data;
    }

/*
    You can override these two methods in subclasses of SimpleNode to
    customize the way the node appears when the tree is dumped.  If
    your output uses more than one line you should override
    toString(String), otherwise overriding toString() is probably all
    you need to do.
*/
    public String toString()
    {
        return KSPParserTreeConstants.jjtNodeName[ id ];
    }
    public String toString( String prefix ) { return prefix + toString(); }

/*
Override this method if you want to customize how the node dumps out its children.
*/
    public void dump( String prefix )
    {
        System.out.println( toString( prefix ) );
        if ( children != null )
        {
            for ( int i = 0; i < children.length; ++i )
            {
                SimpleNode n = ( SimpleNode )children[i];
                if ( n != null )
                {
                    n.dump( prefix + " " );
                }
            }
        }
    }

    public int getId()
    {
        return id;
    }

    /**
     * 追記：このノードを指定された新規ノードに置き換える
     */
    public <T extends SimpleNode> T reset( T newNode, Node[] newChildren, Object newValue, SymbolDefinition newSymbol )
    {
        if( parent == null )
        {
            newNode.id       = newNode.id;
            newNode.value    = newValue;
            newNode.parser   = parser;
            newNode.children = newChildren;
            newSymbol.value  = newValue;
            SymbolDefinition.copy( newSymbol, newNode.symbol );
            return newNode;
        }
        for( int i = 0; i < parent.jjtGetNumChildren(); i++ )
        {
            SimpleNode n = ( SimpleNode )parent.jjtGetChild( i );
            if( n != this )
            {
                continue;
            }
            newNode.parent   = parent;
            newNode.id       = newNode.id;
            newNode.value    = newValue;
            newNode.parser   = n.parser;
            newNode.children = newChildren;
            newSymbol.value  = newValue;
            SymbolDefinition.copy( newSymbol, newNode.symbol );
            ( ( SimpleNode )parent ).children[ i ]  = newNode;
            break;
        }
        return newNode;
    }

    /**
     * 親ノードが算術演算子ノードかどうかを判定する
     */
    public boolean isParentBinaryOperator()
    {
        if( parent == null )
        {
            return false;
        }
        switch( parent.getId() )
        {
            case JJTSTRADD:
            case JJTBITWISEOR:
            case JJTBITWISEAND:
            case JJTEQUAL:
            case JJTNOTEQUAL:
            case JJTLT:
            case JJTGT:
            case JJTLE:
            case JJTGE:
            case JJTADD:
            case JJTSUB:
            case JJTMUL:
            case JJTDIV:
            case JJTMOD:
            case JJTNEG:
            case JJTNOT:
                return true;
            default:
                return false;
        }
    }

    /**
     * このノードが配列変数で、親ノード次第で添え字の有無の検証が必要かどうかを判定する
     */
    public boolean isNecessaryValidArraySubscribe()
    {
        if( parent == null )
        {
            return false;
        }
        if( !symbol.isArray() )
        {
            return false;
        }
        if( isParentBinaryOperator() )
        {
            return true;
        }
        switch( parent.getId() )
        {
            case JJTASSIGNMENT:
                return true;
            default:
                return false;
        }
    }
}

/* JavaCC - OriginalChecksum=8c445a9763bc677bca44d000fc9dd06c (do not edit this line) */
