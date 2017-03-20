/* =========================================================================

    KSPTypeDefinitionProvider.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import vscode = require( 'vscode' );

import { KSPSymbolUtil } from './KSPSymbolUtil';
import { KSPSymbolInformation } from './KSPSymbolUtil';

export class KSPTypeDefinitionProvider implements vscode.TypeDefinitionProvider
{
    constructor()
    {
    }

    public provideTypeDefinition(
        document: vscode.TextDocument,
        position: vscode.Position,
        token: vscode.CancellationToken ) : Thenable<vscode.Location[]>
    {
        var symbols: KSPSymbolInformation[] = KSPSymbolUtil.collect( document, token );
        if( !symbols )
        {
            return null;
        }
        var result = [];
        symbols.forEach( x=>{
            result.push( x.location );
        });
        return Promise.resolve( result );
    }
}
