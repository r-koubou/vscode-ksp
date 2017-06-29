/* =========================================================================

    SemanticAnalyzer.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.util.ArrayList;

import net.rkoubou.kspparser.analyzer.SymbolDefinition.SymbolType;
import net.rkoubou.kspparser.javacc.generated.ASTAssignment;
import net.rkoubou.kspparser.javacc.generated.ASTCallCommand;
import net.rkoubou.kspparser.javacc.generated.ASTCommandArgumentList;
import net.rkoubou.kspparser.javacc.generated.ASTLiteral;
import net.rkoubou.kspparser.javacc.generated.ASTRefVariable;
import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclaration;
import net.rkoubou.kspparser.javacc.generated.SimpleNode;

/**
 * 意味解析実行クラス
 */
public class SemanticAnalyzer extends AbstractAnalyzer
{

    /** シンボルテーブル保持インスタンス */
    public final SymbolCollector symbolCollector;

    /**
     * ctor
     */
    public SemanticAnalyzer( SymbolCollector symbolCollector )
    {
        super( symbolCollector.astRootNode );
        this.symbolCollector = symbolCollector;
    }

    /**
     * 意味解析の実行
     */
    @Override
    public void analyze() throws Exception
    {
        astRootNode.jjtAccept( this, null );
    }

    /**
     * 変数宣言
     */
    @Override
    public Object visit( ASTVariableDeclaration node, Object data)
    {
        final Object ret = defaultVisit( node, data );
        final VariableTable variableTable = symbolCollector.variableTable;
        final UITypeTable uiTypeTable     = symbolCollector.uiTypeTable;
        final Variable variable           = variableTable.search( node.symbol.name );
        return ret;
    }

    /**
     * 代入式
     */
    @Override
    public Object visit( ASTAssignment node, Object data)
    {
        final Object ret = defaultVisit( node, data );
        final VariableTable variableTable = symbolCollector.variableTable;
        final Variable variable           = variableTable.search( node.symbol.name );
        return ret;
    }

    /**
     * コマンド呼び出し
     * @return node 自身
     */
    @Override
    public Object visit( ASTCallCommand node, Object data)
    {
        final CommandTable cmdTable = symbolCollector.commandTable;
        final Command cmd           = cmdTable.search( node.symbol.name );

        if( cmd == null )
        {
            // ドキュメントに記載のない隠しコマンドの可能性
            // エラーにせず、警告に留める
            System.out.println( "Warn unknown command : " + node.symbol.name );
            return node;
        }

        // 引数の解析
        node.childrenAccept( this, cmd );

        return node;
    }

    /**
     * コマンド引数
     * @param data 呼び出し元 Command インスタンス
     * @return null
     */
    @Override
    public Object visit( ASTCommandArgumentList node, Object data )
    {
        final int childrenNum               = node.jjtGetNumChildren();
        final VariableTable variableTable   = symbolCollector.variableTable;
        final CommandTable commandTable     = symbolCollector.commandTable;

        Object ret    = null;
        Command cmd   = null;

        if( !( data instanceof Command ) )
        {
            // ここに到達する時点で意味解析自体のバグ
            return null;
        }

        cmd = (Command)data;
        ArrayList<CommandArgument> argList = cmd.argList;

        //--------------------------------------------------------------------------
        // 引数の数チェック
        //--------------------------------------------------------------------------
        if( node.jjtGetNumChildren() != cmd.argList.size() )
        {
            System.out.println( "Error invalid argc : " + node.jjtGetNumChildren() );
            return null;
        }
        //--------------------------------------------------------------------------
        // 引数の型チェック
        //--------------------------------------------------------------------------
        {
            for( int i = 0; i < childrenNum; i++ )
            {
                Object evalValue  = node.jjtGetChild( i ).jjtAccept( this, null );    // 子ノードの評価式結果
                if( evalValue == null )
                {
                    return false;
                }
                boolean valid           = false;
                SimpleNode evalNode     = (SimpleNode)evalValue;
                SymbolDefinition symbol = evalNode.symbol;
                int type                = symbol.type;

                // 評価式が変数だった場合のための参照
                Variable userVar        = variableTable.search( symbol.name );

                // 引数毎に複数のデータ型が許容される仕様のため照合
                //System.out.println("-------- : " + argList.size() + "/" + childrenNum );
                for( Argument arg : argList.get( i ).arguments )
                {
                    //System.out.println( "#ARG: " + arg.type );
                    //--------------------------------------------------------------------------
                    // コマンドがUI属性付き変数を要求
                    //--------------------------------------------------------------------------
                    if( userVar != null && ( arg.accessFlag & ACCESS_ATTR_UI ) != 0 )
                    {
                        if( userVar.uiTypeInfo == null )
                        {
                            // ui_##### 修飾子が無い変数
                            //System.out.println( "Not UI type" + userVar.name );
                            break;
                        }
                        if( arg.uiTypeName.equals( userVar.uiTypeInfo.name ) )
                        {
                            // 要求されている ui_#### と変数宣言時の ui_#### 修飾子が一致
                            //System.out.println( "UI OK" );
                            valid = true;
                            break;
                        }
                    }
                    //--------------------------------------------------------------------------
                    // ui_#### 修飾子が無い変数
                    //--------------------------------------------------------------------------
                    else if( userVar != null )
                    {
                        if( arg.type == userVar.type )
                        {
                            valid = true;
                            break;
                        }
                    }
                    //--------------------------------------------------------------------------
                    // コマンドの戻り値
                    //--------------------------------------------------------------------------
                    else if( evalNode.getId() == JJTCALLCOMMAND )
                    {
                        ASTCallCommand callCmd = (ASTCallCommand)evalNode;
                        Command retCommand = commandTable.search( callCmd.symbol.name );

                        // [nullチェック]
                        // 隠しコマンドやドキュメント化されていない場合に逆引きできない可能性があるため
                        if( retCommand != null )
                        {
                            for( int t : retCommand.returnType.typeList )
                            {
                                // 呼び出したコマンドの戻り値と
                                // このコマンドの求められている引数の型チェック
                                if( t == arg.type )
                                {
                                    valid = true;
                                    break;
                                }
                            }
                        }
                        else
                        {
                            // 未知のコマンドなので正しいかどうかの判定が不可能
                            // エラーにせずに警告に留める
                            System.out.println( "Warn: Unknown command. argument check ignored - " + callCmd.symbol.name );
                            valid = true;
                            break;
                        }
                    }
                    //--------------------------------------------------------------------------
                    // リテラル
                    //--------------------------------------------------------------------------
                    else
                    {
                        if( arg.type == type )
                        {
                            valid = true;
                            break;
                        }
                    }
                } // for( Argument arg : a.arguments )

                if( !valid )
                {
                    System.out.println( "Error NOT MATCH" );
                }

            } //~for( int i = 0; i < childrenNum; i++ )
        }
        return null;
    }

    /**
     * リテラル定数参照
     * @return node自身
     */
    @Override
    public Object visit( ASTLiteral node, Object data )
    {
        return node;
    }

    /**
     * 変数参照
     * @return node自身
     */
    @Override
    public Object visit( ASTRefVariable node, Object data )
    {
        //--------------------------------------------------------------------------
        // 宣言済みかどうか
        //--------------------------------------------------------------------------
        Variable v = symbolCollector.variableTable.search( node.symbol.name );
        if( v == null )
        {
            // 宣言されていない変数
            System.out.println( "Error NOT declared : " + node.symbol.name );
            return node;
        }
        return node;
    }
}
