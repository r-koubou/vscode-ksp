/* =========================================================================

    KSPOutlineProvider.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import vscode                       = require( 'vscode' );

import { KSPSymbolUtil }        from './KSPSymbolUtil';
import { KSPSymbolType }        from './KSPSymbolUtil';
import { KSPSymbol }            from './KSPSymbolUtil';
import { KSPSymbolInformation } from './KSPSymbolUtil';

export class KSPOutlineProvider implements vscode.TreeDataProvider<KSPSymbolInformation>
{
    /**
     * Get [TreeItem](#TreeItem) representation of the `element`
     *
     * @param element The element for which [TreeItem](#TreeItem) representation is asked for.
     * @return [TreeItem](#TreeItem) representation of the element
     */
    public getTreeItem( element: KSPSymbolInformation): vscode.TreeItem | Thenable<vscode.TreeItem>
    {
        return null;
    }

    /**
     * Get the children of `element` or root if no element is passed.
     *
     * @param element The element from which the provider gets children. Can be `undefined`.
     * @return Children of `element` or root if no element is passed.
     */
    public getChildren( element?: KSPSymbolInformation ): vscode.ProviderResult<KSPSymbolInformation[]>
    {
        return null;
    }
}