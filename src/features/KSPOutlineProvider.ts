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

const TREE_ITEM_NONE: vscode.TreeItemCollapsibleState       = vscode.TreeItemCollapsibleState.None;
const TREE_ITEM_EXPANDED: vscode.TreeItemCollapsibleState   = vscode.TreeItemCollapsibleState.Expanded;
const TREE_ITEM_COLLAPSED: vscode.TreeItemCollapsibleState  = vscode.TreeItemCollapsibleState.Collapsed;

const FOLDER_ICON = {
    light: path.join( Constants.RES_BASEDIR, 'folder.svg' ),
    dark:  path.join( Constants.RES_BASEDIR, 'folder_dark.svg' )
};

export const COMMAND_JUMP: string            = "ksp.outline.jump";
/*
TODO
export const COMMAND_COLLAPSE_ALL: string    = "ksp.outline.collapse";
export const COMMAND_EXPAND_ALL: string      = "ksp.outline.expand";
*/

/**
 * A node for treeitem
 */
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

/**
 * Implementation of KSP TreeDataProvider
 */
export class KSPOutlineProvider implements vscode.TreeDataProvider<KSPSymbolNode>
{
    private _onDidChangeTreeData: vscode.EventEmitter<KSPSymbolNode | undefined> = new vscode.EventEmitter<KSPSymbolNode | undefined>();
    readonly onDidChangeTreeData: vscode.Event<KSPSymbolNode | undefined> = this._onDidChangeTreeData.event;
	private editor: vscode.TextEditor;

    private rootNode: KSPSymbolNode;
    private variableTree: KSPSymbolNode;
    private callbackTree: KSPSymbolNode;
    private functionTree: KSPSymbolNode;

    /**
     * ctor.
     */
    constructor( context:vscode.ExtensionContext )
    {
        vscode.window.registerTreeDataProvider( Constants.VIEW_ID_OUTLINE, this );
        vscode.commands.registerCommand( COMMAND_JUMP, range => this.jumpTo( range ) );
/*
TODO
        vscode.commands.registerCommand( COMMAND_COLLAPSE_ALL, () => this.collapseAllNode() );
        vscode.commands.registerCommand( COMMAND_EXPAND_ALL, () => this.expandAllNode() );
*/

        vscode.window.onDidChangeActiveTextEditor( (e) => { this.onDidChangedTextEditor( e ) } );
        vscode.workspace.onDidChangeTextDocument(  (e) => { this.onDidChangeTextDocument( e ); } );
        vscode.workspace.onDidOpenTextDocument( (e) => { this.onDidOpenTextDocument( e ); } );

        this.rootNode     = new KSPSymbolNode( 'root' );
        this.variableTree = new KSPSymbolNode( 'Variables', TREE_ITEM_COLLAPSED, this.rootNode, null );
        this.callbackTree = new KSPSymbolNode( 'Callbacks', TREE_ITEM_COLLAPSED, this.rootNode, null );
        this.functionTree = new KSPSymbolNode( 'Functions', TREE_ITEM_COLLAPSED, this.rootNode, null );

        this.variableTree.iconPath = FOLDER_ICON;
        this.callbackTree.iconPath = FOLDER_ICON;
        this.functionTree.iconPath = FOLDER_ICON;

    }

    /**
     * Set all node tree to empty
     */
    private clearAllNodeTree(): void
    {
        this.variableTree.children = [];
        this.callbackTree.children = [];
        this.functionTree.children = [];
    }

    /**
     * Get a current TextDocument instance which langid is this extention
     */
    private getCurrentTextDocument(): vscode.TextDocument
    {
        // All editor is closed
        if( !vscode.window.activeTextEditor )
        {
            return null;
        }

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
        this.refresh();
    }

    /**
     * vscode.workspace.onDidChangeTextDocument
     */
    private onDidChangeTextDocument( event:vscode.TextDocumentChangeEvent ): void
    {
        this.refresh();
    }

    public onDidOpenTextDocument( document: vscode.TextDocument ): void
    {
        this.refresh();
    }

    /**
     * Refresh outline nodetree
     */
    private refresh(): void
    {
        this.editor = vscode.window.activeTextEditor;
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
        if( element.value )
        {
            element.command = {
                command: COMMAND_JUMP,
                title: '',
                arguments: [ element.value.location.range ]
            };
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

            this.clearAllNodeTree();

            for( const v of table )
            {
                const type: KSPSymbolType = v.KspSymbol.kspSymbolType;
                const child: KSPSymbolNode = new KSPSymbolNode( v.name, TREE_ITEM_NONE, null, v );

                // primitive type
                if( type >= KSPSymbolType.VARIABLE_TYPE_BEGIN && type <= KSPSymbolType.VARIABLE_TYPE_END )
                {
                    child.parrent  = this.variableTree;
                    child.label    = v.KspSymbol.toVariableNameFormat() + " : " + v.KspSymbol.variableTypeName + ( v.KspSymbol.isPolyphonic ? ", Polyphonic" : "" );
                    child.iconPath = {
                        light: path.join( Constants.RES_BASEDIR, 'variable.svg' ),
                        dark:  path.join( Constants.RES_BASEDIR, 'variable.svg' )
                    };
                    if( v.KspSymbol.isConst )
                    {
                        child.iconPath = {
                            light: path.join( Constants.RES_BASEDIR, 'constant_variable.svg' ),
                            dark:  path.join( Constants.RES_BASEDIR, 'constant_variable.svg' )
                        };
                    }
                    if( v.KspSymbol.isUI )
                    {
                        child.iconPath = {
                            light: path.join( Constants.RES_BASEDIR, 'ui_variable.svg' ),
                            dark:  path.join( Constants.RES_BASEDIR, 'ui_variable.svg' )
                        };
                    }
                }
                else if( type == KSPSymbolType.CALLBACK )
                {
                    child.parrent = this.callbackTree;
                    child.iconPath = {
                        light: path.join( Constants.RES_BASEDIR, 'callback.svg' ),
                        dark:  path.join( Constants.RES_BASEDIR, 'callback.svg' )
                    };

                    if( v.KspSymbol.uiVariableName )
                    {
                        child.label   += " : " + v.KspSymbol.uiVariableName;
                    }
                }
                else if( type == KSPSymbolType.USER_FUNCTION )
                {
                    child.parrent = this.functionTree;
                    child.iconPath = {
                        light: path.join( Constants.RES_BASEDIR, 'function.svg' ),
                        dark:  path.join( Constants.RES_BASEDIR, 'function.svg' )
                    };
                }

                if( child.parrent )
                {
                    child.parrent.children.push( child );
                }
            }
            result.push( this.variableTree );
            result.push( this.callbackTree );
            result.push( this.functionTree );
        }
        return result;
    }

//------------------------------------------------------------------------------
// Command Handling
//------------------------------------------------------------------------------

    /**
     * Jump to symbol from outline item selected
     */
    public jumpTo( range: vscode.Range )
    {
		this.editor.selection = new vscode.Selection( range.start, range.end );
        this.editor.revealRange( range, vscode.TextEditorRevealType.InCenterIfOutsideViewport );
        vscode.window.showTextDocument( this.editor.document, vscode.ViewColumn.Active );
    }

/*
TODO
        "view/title": [
            {
                "when": "view == kspOutLine",
                "command": "ksp.outline.collapse",
                "group": "navigation"
            },
            {
                "when": "view == kspOutLine",
                "command": "ksp.outline.expand",
                "group": "navigation"
            }
        ]
        :
        :
        {
            "command": "ksp.outline.collapse",
            "title": "Collapse All",
            "icon":{
                "light": "resources/icon/collapse_all.svg",
                "dark": "resources/icon/collapse_all.svg"
            }
        },
        {
            "command": "ksp.outline.expand",
            "title": "Expand All",
            "icon":{
                "light": "resources/icon/expand_all.svg",
                "dark": "resources/icon/expand_all.svg"
            }
        }
*/
    /**
     * Collapse all node tree
     */
    public collapseAllNode(): void
    {
        // this.variableTree.collapsibleState = TREE_ITEM_COLLAPSED;
        // this.callbackTree.collapsibleState = TREE_ITEM_COLLAPSED;
        // this.functionTree.collapsibleState = TREE_ITEM_COLLAPSED;
        // this._onDidChangeTreeData.fire( this.variableTree );
        // this._onDidChangeTreeData.fire( this.callbackTree );
        // this._onDidChangeTreeData.fire( this.functionTree );
    }

    /**
     * Expand all node tree
     */
    public expandAllNode(): void
    {
        // this.variableTree.collapsibleState = TREE_ITEM_EXPANDED;
        // this.callbackTree.collapsibleState = TREE_ITEM_EXPANDED;
        // this.functionTree.collapsibleState = TREE_ITEM_EXPANDED;
        // this._onDidChangeTreeData.fire( this.variableTree );
        // this._onDidChangeTreeData.fire( this.callbackTree );
        // this._onDidChangeTreeData.fire( this.functionTree );
    }

}
