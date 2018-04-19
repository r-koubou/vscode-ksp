/* =========================================================================

    KSPDefinitionProvider.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import vscode = require( 'vscode' );

import { KSPSymbolUtil }        from './KSPSymbolUtil';
import { KSPSymbolType }        from './KSPSymbolUtil';
import { KSPSymbol }            from './KSPSymbolUtil';
import { KSPSymbolInformation } from './KSPSymbolUtil';

export class KSPDefinitionProvider implements vscode.DefinitionProvider
{
    constructor()
    {
    }

    public provideDefinition(
        document: vscode.TextDocument,
        position: vscode.Position,
        token: vscode.CancellationToken ) : Thenable<vscode.Location[]>
    {
        let symbols: KSPSymbolInformation[] = KSPSymbolUtil.collect( document, token );
        if( !symbols )
        {
            return null;
        }
        let result: vscode.Location[]  = [];
        let textLine : vscode.TextLine = document.lineAt( position.line );
        let symbol : string            = KSPSymbolUtil.parseSymbolAt( document, position );

        symbols.forEach( x=>{
            let sym : KSPSymbol         = x.KspSymbol;
            let symName : string        = sym.name;
            let declaredLine : boolean  = sym.lineNumber == textLine.lineNumber;

            // User Function?
            if( !declaredLine && symName == symbol )
            {
                result.push( x.location );
            }
            // Variable?
            else if( x.KspSymbol.toVariableNameFormat() == symbol )
            {
                if( !declaredLine )
                {
                    result.push( x.location );
                }

                // For UI Callback?
                if( x.KspSymbol.isUI )
                {
                    symbols.forEach( y=>{
                        if( y.KspSymbol.isUI && y.KspSymbol.kspSymbolType == KSPSymbolType.CALLBACK &&
                            y.KspSymbol.toVariableNameFormat( true ) == symbol )
                        {
                            result.push( y.location );
                        }
                    });
                }
            }
        });
        return Promise.resolve( result );
    }
}
