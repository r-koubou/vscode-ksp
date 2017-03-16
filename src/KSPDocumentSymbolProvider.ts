/* =========================================================================

    KSPDocumentSymbolProvider.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import vscode = require( 'vscode' );

class KSPDocumentSymbolProvider implements vscode.DocumentSymbolProvider
{
    public provideDocumentSymbols(
        document: vscode.TextDocument,
        token: vscode.CancellationToken ) : Thenable<vscode.SymbolInformation[]>
    {
        return Promise.resolve( null );
        // return new Promise<vscode.SymbolInformation[]>( (resolve, reject) =>{
        // });
    }
}
