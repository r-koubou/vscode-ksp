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
        var symbols: KSPSymbolInformation[] = KSPSymbolUtil.collect( document, token );
        if( !symbols )
        {
            return null;
        }
        var result = [];
        var textLine : vscode.TextLine = document.lineAt( position.line );
        var symbol : string            = KSPSymbolUtil.parseSymbolAt( document, position );

        symbols.forEach( x=>{
            var sym : KSPSymbol         = x.KspSymbol;
            var symName : string        = sym.name;
            var declaredLine : boolean  = sym.lineNumber == textLine.lineNumber;

            // User Function?
            if( !declaredLine && symName == symbol )
            {
                result.push( x.location );
            }
            // Variable?
            else if( KSPSymbol.toVariableNameFormat( x.KspSymbol ) == symbol )
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
                            KSPSymbol.toVariableNameFormat( y.KspSymbol, true ) == symbol )
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
