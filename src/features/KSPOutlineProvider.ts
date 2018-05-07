/* =========================================================================

    KSPOutlineProvider.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import vscode                   = require( 'vscode' );

import * as Constants           from './KSPExtensionConstants';

import { KSPSymbolUtil }        from './KSPSymbolUtil';
import { KSPSymbolType }        from './KSPSymbolUtil';
import { KSPSymbol }            from './KSPSymbolUtil';
import { KSPSymbolInformation } from './KSPSymbolUtil';

export class KSPOutlineProvider implements vscode.TreeDataProvider<KSPSymbolInformation>
{

    /**
     * ctor.
     */
    constructor( context:vscode.ExtensionContext )
    {
        vscode.window.onDidChangeActiveTextEditor( (e) => { this.onDidChangedTextEditor( e ) } );
        vscode.workspace.onDidChangeTextDocument(  (e) => { this.onDidChangeTextDocument( e ); } );
    }

    /**
     * Get a TextDocument instance which langid is this extention
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
    }

    /**
     * Refresh outline nodetree
     */
    private refresh(): void
    {}

    /**
     * Get [TreeItem](#TreeItem) representation of the `element`
     *
     * @param element The element for which [TreeItem](#TreeItem) representation is asked for.
     * @return [TreeItem](#TreeItem) representation of the element
     */
    public getTreeItem( element: KSPSymbolInformation ): vscode.TreeItem | Thenable<vscode.TreeItem>
    {
        const document: vscode.TextDocument = this.getCurrentTextDocument();
        if( !document || !element )
        {
            return null;
        }
        return null;
    }

    /**
     * Get the children of `element` or root if no element is passed.
     *
     * @param element The element from which the provider gets children. Can be `undefined`.
     * @return Children of `element` or root if no element is passed.
     */
    public getChildren( element?: KSPSymbolInformation ): Thenable<KSPSymbolInformation[]>
    {
        const document: vscode.TextDocument = this.getCurrentTextDocument();
        let result: KSPSymbolInformation[] = [];
        if( !document )
        {
            return;
        }
        if( !element )
        {
            const tree: KSPSymbolInformation[] = KSPSymbolUtil.collect( document );
            let variableTree = [];
            let callbackTree = [];
            let functionTree = [];

            tree.sort( ( a: KSPSymbolInformation, b: KSPSymbolInformation ): number => {
                const typeA: KSPSymbolType = a.KspSymbol.kspSymbolType;
                const typeB: KSPSymbolType = b.KspSymbol.kspSymbolType;
                if( typeA < typeB )
                {
                    return -1;
                }
                if( typeA > typeB )
                {
                    return 1;
                }
                return 0;
            });

            for( let i of tree )
            {
                const t = i.KspSymbol.kspSymbolType;
                if( t >= KSPSymbolType.VARIABLE_TYPE_BEGIN && t <= KSPSymbolType.VARIABLE_TYPE_END )
                {
                    variableTree.push( i );
                }
                else if( t == KSPSymbolType.CALLBACK )
                {
                    callbackTree.push( i );
                }
                else if( t == KSPSymbolType.USER_FUNCTION )
                {
                    functionTree.push( i );
                }
            }
            return Promise.resolve( result );
        }
        else
        {
            return null;
        }
    }
}
