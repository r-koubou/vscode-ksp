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
        var line   : string = textLine.text;
        var eolPos : number = line.length;
        var symbol : string = line.charAt( position.character );
        for( var i = position.character + 1; i < eolPos; i++ )
        {
            var regex : RegExp = /[\s|\(|\)|\{|\}|:|\[|\]|,|\+|-|\/|\*|<|>|\^]+/g;
            var char  = line.charAt( i );
            var match = regex.exec( char );
            if( match )
            {
                break;
            }
            symbol += char;
        }
        for( var i = position.character - 1; i >= 0; i-- )
        {
            var regex : RegExp = /[\s|\(|\)|\{|\}|:|\[|\]|,|\+|-|\/|\*|<|>|\^]+/g;
            var char  = line.charAt( i );
            var match = regex.exec( char );
            if( match )
            {
                break;
            }
            symbol = char + symbol;
        }

        symbols.forEach( x=>{
            var sym : KSPSymbol = x.KspSymbol;
            var symName         = sym.name;

            // Declare line?
            if( sym.lineNumber == textLine.lineNumber )
            {
                return;
            }
            // User Function?
            else if( symName == symbol )
            {
                result.push( x.location );
            }
            // Variable?
            else if( KSPSymbol.toVariableNameFormat( x.KspSymbol ) == symbol )
            {
                result.push( x.location );
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
