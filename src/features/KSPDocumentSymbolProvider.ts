/* =========================================================================

    KSPDocumentSymbolProvider.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import vscode = require( 'vscode' );

import { KSPSymbolUtil, KSPSymbolInformation, KSPSymbolType } from './KSPSymbolUtil';

export class KSPDocumentSymbolProvider implements vscode.DocumentSymbolProvider
{
    constructor()
    {
    }

    public provideDocumentSymbols(
        document: vscode.TextDocument,
        token: vscode.CancellationToken ) : Thenable<vscode.SymbolInformation[]>
    {
        const result: KSPSymbolInformation[] = KSPSymbolUtil.collect( document );
        for( const v of result )
        {
            if( v.KspSymbol.kspSymbolType == KSPSymbolType.CALLBACK )
            {
                v.name = "on " + v.name;
            }
        }
        return Promise.resolve( result );
    }
}
