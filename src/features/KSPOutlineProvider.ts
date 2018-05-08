/* =========================================================================

    KSPOutlineProvider.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import vscode                   = require( 'vscode' );

import * as path                from 'path';
import * as Constants           from './KSPExtensionConstants';

import { KSPSymbolUtil }        from './KSPSymbolUtil';
import { KSPSymbolType }        from './KSPSymbolUtil';
import { KSPSymbol }            from './KSPSymbolUtil';
import { KSPSymbolInformation } from './KSPSymbolUtil';
import { resolve } from 'dns';

const TREE_ITEM_NONE: vscode.TreeItemCollapsibleState       = vscode.TreeItemCollapsibleState.None;
const TREE_ITEM_EXPANDED: vscode.TreeItemCollapsibleState   = vscode.TreeItemCollapsibleState.Expanded;
const TREE_ITEM_COLLAPSED: vscode.TreeItemCollapsibleState  = vscode.TreeItemCollapsibleState.Collapsed;

class KSPSymbolNode extends vscode.TreeItem
{
    public parrent: KSPSymbolNode       = null;
    public children: KSPSymbolNode[]    = [];
    public value: KSPSymbolInformation  = null;

    constructor( label: string, collapsibleState: vscode.TreeItemCollapsibleState = vscode.TreeItemCollapsibleState.None, parrent?: KSPSymbolNode, value?: KSPSymbolInformation )
    {
        super( label, collapsibleState );
        this.parrent            = parrent;
        this.value              = value;
    }
}

export class KSPOutlineProvider implements vscode.TreeDataProvider<KSPSymbolNode>
{
    private _onDidChangeTreeData: vscode.EventEmitter<KSPSymbolNode | undefined> = new vscode.EventEmitter<KSPSymbolNode | undefined>();
    readonly onDidChangeTreeData: vscode.Event<KSPSymbolNode | undefined> = this._onDidChangeTreeData.event;

    private rootNode: KSPSymbolNode;

    /**
     * ctor.
     */
    constructor( context:vscode.ExtensionContext )
    {
        vscode.window.onDidChangeActiveTextEditor( (e) => { this.onDidChangedTextEditor( e ) } );
        vscode.workspace.onDidChangeTextDocument(  (e) => { this.onDidChangeTextDocument( e ); } );
        this.rootNode = new KSPSymbolNode( 'root' );
    }

    /**
     * Get a current TextDocument instance which langid is this extention
     */
    private getCurrentTextDocument(): vscode.TextDocument
    {
        const document: vscode.TextDocument = vscode.window.activeTextEditor.document;
        if( !this.validateTextDocument( document ) )
        {
            return null;
        }
        return document;
    }

    /**
     * valid a TextDocument instance
     */
    private validateTextDocument( document: vscode.TextDocument ): boolean
    {
        if( !document || document.languageId !== Constants.LANG_ID || document.isClosed )
        {
            return false;
        }
        return true;
    }

    /**
     * Handling of window.onDidChangeActiveTextEditor
     */
    private onDidChangedTextEditor( textEditor:vscode.TextEditor ): void
    {
        const document: vscode.TextDocument = this.getCurrentTextDocument();

        if( !vscode.window.activeTextEditor )
        {
            return;
        }
        if( !document )
        {
            return;
        }
        this.refresh();
    }

    /**
     * vscode.workspace.onDidChangeTextDocument
     */
    private onDidChangeTextDocument( event:vscode.TextDocumentChangeEvent ): void
    {
        const document: vscode.TextDocument = event.document;
        if( !this.validateTextDocument( document ) )
        {
            return;
        }
        this.refresh();
    }

    /**
     * Refresh outline nodetree
     */
    private refresh(): void
    {
        this._onDidChangeTreeData.fire();
    }

    /**
     * Get [TreeItem](#TreeItem) representation of the `element`
     *
     * @param element The element for which [TreeItem](#TreeItem) representation is asked for.
     * @return [TreeItem](#TreeItem) representation of the element
     */
    public getTreeItem( element: KSPSymbolNode ): vscode.TreeItem | Thenable<vscode.TreeItem>
    {
        const document: vscode.TextDocument = this.getCurrentTextDocument();
        if( !document || !element )
        {
            return null;
        }
        return element;
    }

    /**
     * Get the children of `element` or root if no element is passed.
     *
     * @param element The element from which the provider gets children. Can be `undefined`.
     * @return Children of `element` or root if no element is passed.
     */
    public getChildren( element?: KSPSymbolNode ): Thenable<KSPSymbolNode[]>
    {
        const document: vscode.TextDocument = this.getCurrentTextDocument();
        let result: KSPSymbolInformation[] = [];
        if( !document )
        {
            return Promise.resolve( [] );
        }
        return new Promise( resolve => {
            if( element )
            {
                resolve( this.getSymbolInformations( document, element ) );
            }
            else
            {
                resolve( this.getSymbolInformations( document ) );
            }
        });
    }

    /**
     * Collect Symbol Informations
     */
    private getSymbolInformations( document: vscode.TextDocument, parrent?: KSPSymbolNode ): KSPSymbolNode[]
    {
        if( !document )
        {
            return [];
        }

        const result: KSPSymbolNode[]        = [];
        const table: KSPSymbolInformation[]  = KSPSymbolUtil.collect( document );

        if( parrent )
        {
            for( const v of parrent.children )
            {
                result.push( v );
            }
        }
        else
        {
            const root: KSPSymbolNode = this.rootNode;

            let variableTree: KSPSymbolNode = new KSPSymbolNode( 'Variables', TREE_ITEM_COLLAPSED, root, null );
            let callbackTree: KSPSymbolNode = new KSPSymbolNode( 'Callbacks', TREE_ITEM_COLLAPSED, root, null );
            let functionTree: KSPSymbolNode = new KSPSymbolNode( 'Functions', TREE_ITEM_COLLAPSED, root, null );

            variableTree.iconPath = vscode.ThemeIcon.Folder;
            callbackTree.iconPath = vscode.ThemeIcon.Folder;
            functionTree.iconPath = vscode.ThemeIcon.Folder;

            for( const v of table )
            {
                const type: KSPSymbolType = v.KspSymbol.kspSymbolType;
                const child: KSPSymbolNode = new KSPSymbolNode( v.name, TREE_ITEM_NONE, null, v );

                // primitive type
                if( type >= KSPSymbolType.VARIABLE_TYPE_BEGIN && type <= KSPSymbolType.VARIABLE_TYPE_END )
                {
                    child.parrent  = variableTree;
                    child.label    = v.KspSymbol.toVariableNameFormat();
                    child.iconPath = {
                        light: path.join( Constants.EXTENTION_DIR, 'resources', 'variable.png' ),
                        dark:  path.join( Constants.EXTENTION_DIR, 'resources', 'variable.png' )
                    };

                    if( v.KspSymbol.isUI )
                    {
                        child.label += " - " + v.KspSymbol.variableTypeName;
                    }
                }
                else if( type == KSPSymbolType.CALLBACK )
                {
                    child.parrent = callbackTree;
                    child.iconPath = {
                        light: path.join( Constants.EXTENTION_DIR, 'resources', 'callback.png' ),
                        dark:  path.join( Constants.EXTENTION_DIR, 'resources', 'callback.png' )
                    };

                    if( v.KspSymbol.uiVariableName )
                    {
                        child.label   += " - for " + v.KspSymbol.uiVariableName;
                    }
                }
                else if( type == KSPSymbolType.USER_FUNCTION )
                {
                    child.parrent = functionTree;
                    child.iconPath = {
                        light: path.join( Constants.EXTENTION_DIR, 'resources', 'function.png' ),
                        dark:  path.join( Constants.EXTENTION_DIR, 'resources', 'function.png' )
                    };
                }

                if( child.parrent )
                {
                    child.parrent.children.push( child );
                }
            }
            result.push( variableTree );
            result.push( callbackTree );
            result.push( functionTree );
        }
        return result;
    }

}
