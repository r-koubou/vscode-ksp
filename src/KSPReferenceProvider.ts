/* =========================================================================

    KSPReferenceProvider.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import vscode                       = require( 'vscode' );
import KSPCommandNameList           = require( './generated/KSPCommandNames' );
import KSPBuiltinVariableNameList   = require( './generated/KSPBuiltinVariableNames' );

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

            // Builtin-Commands?
            words.forEach( w=>{
                var found : boolean = false;
                if( w == symbol )
                {
                    KSPCommandNameList.commandNameList.forEach( cmd=>{
                        if( cmd == symbol )
                        {
                            result.push( new vscode.Location(
                                document.uri,
                                new vscode.Position( i, 0 )
                            ));
                            found = true;
                            return;
                        }
                    });
                }
                if( found )
                {
                    return;
                }
            });
            // Builtin-Variables?
            words.forEach( w=>{
                var found : boolean = false;
                if( w == symbol )
                {
                    KSPBuiltinVariableNameList.builtinVariableNameList.forEach( v=>{
                        if( v == symbol )
                        {
                            result.push( new vscode.Location(
                                document.uri,
                                new vscode.Position( i, 0 )
                            ));
                            found = true;
                            return;
                        }
                    });
                }
                if( found )
                {
                    return;
                }
            });
            // User definition
            symbols.forEach( x=>{
                var sym : KSPSymbol         = x.KspSymbol;
                var symName : string        = sym.name;

                // User Function?
                if( symName == symbol )
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
