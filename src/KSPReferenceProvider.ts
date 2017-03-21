/* =========================================================================

    KSPReferenceProvider.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import vscode = require( 'vscode' );

import { KSPSymbolUtil }        from './KSPSymbolUtil';
import { KSPSymbolType }        from './KSPSymbolUtil';
import { KSPSymbol }            from './KSPSymbolUtil';
import { KSPSymbolInformation } from './KSPSymbolUtil';

export class KSPReferenceProvider implements vscode.ReferenceProvider
{
    constructor()
    {
    }

    public provideReferences(
        document:vscode.TextDocument,
        position:vscode.Position,
        context:vscode.ReferenceContext,
        token:vscode.CancellationToken) : Thenable<Array<vscode.Location>>
    {
        var symbols: KSPSymbolInformation[] = KSPSymbolUtil.collect( document, token );
        if( !symbols )
        {
            return null;
        }
        var result = [];
        var symbol : string = KSPSymbolUtil.parseSymbolAt( document, position );
        var lineCount : number = document.lineCount;
        for( var i = 0; i < lineCount; i++ )
        {
            var text : string = document.lineAt( i ).text.trim();
            var words : string[] = text.split( /[\s|,|\[|\]|\(|\)]+/ );

            symbols.forEach( x=>{
                var sym : KSPSymbol         = x.KspSymbol;
                var symName : string        = sym.name;

                // User Function / Built-in Commands?
                if( symName == symbol )
                {
                    // User Function?
                    words.forEach( w=>{
                        if( w == symbol )
                        {
                            result.push( new vscode.Location(
                                document.uri,
                                new vscode.Position( i, 0 )
                            ));
                            return;
                        }
                    });
                    // Builtin-Commands
                    // TODO
                }
                // Variable?
                else if( KSPSymbol.toVariableNameFormat( x.KspSymbol ) == symbol )
                {
                    words.forEach( w=>{
                        if( w == symbol )
                        {
                            result.push( new vscode.Location(
                                document.uri,
                                new vscode.Position( i, 0 )
                            ));
                            return;
                        }
                    });
                }
            });
        }
        return Promise.resolve( result );
    }
}
