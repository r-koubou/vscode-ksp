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
        let symbols: KSPSymbolInformation[] = KSPSymbolUtil.collect( document, token );
        if( !symbols )
        {
            return null;
        }
        let result: vscode.Location[] = [];
        let symbol : string = KSPSymbolUtil.parseSymbolAt( document, position );
        let lineCount : number = document.lineCount;
        for( let i = 0; i < lineCount; i++ )
        {
            let text : string = document.lineAt( i ).text.trim();
            let words : string[] = text.split( /[\s|,|\[|\]|\(|\)]+/ );

            // Builtin-Commands?
            words.forEach( w=>{
                let found : boolean = false;
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
                let found : boolean = false;
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
                let sym : KSPSymbol         = x.KspSymbol;
                let symName : string        = sym.name;

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
                else if( x.KspSymbol.toVariableNameFormat() == symbol )
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
