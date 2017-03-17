/* =========================================================================

    KSPDocumentSymbolProvider.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import vscode = require( 'vscode' );

import { KSPSymbolUtil } from './KSPSymbolUtil';

export class KSPDocumentSymbolProvider implements vscode.DocumentSymbolProvider
{
    constructor()
    {
    }

    public provideDocumentSymbols(
        document: vscode.TextDocument,
        token: vscode.CancellationToken ) : Thenable<vscode.SymbolInformation[]>
    {
        return Promise.resolve( KSPSymbolUtil.collect( document, token ) );
    }
}
