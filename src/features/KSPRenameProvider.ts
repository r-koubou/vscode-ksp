/* =========================================================================

    KSPRenameProvider.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import vscode = require( 'vscode' );
import {
    Range,
    TextDocument,
    Position,
    CancellationToken,
    ProviderResult,
    WorkspaceEdit
} from 'vscode';

import KSPExtensionConstants        = require( './KSPExtensionConstants' );
import {
    KSPSymbolUtil,
    KSPSymbolType,
    KSPSymbol,
    KSPSymbolInformation
} from './KSPSymbolUtil';

import KSPSyntaxUtil from './KSPSyntaxUtil';

export class KSPRenameProvider implements vscode.RenameProvider
{
    public provideRenameEdits( document:TextDocument,
                               position:Position,
                               newName:string,
                               token:CancellationToken ) : ProviderResult<WorkspaceEdit>
    {
        console.log( "target: " + document.fileName );
        return new Promise<vscode.WorkspaceEdit>( (ressolve, reject) => {

            if( !newName )
            {
                reject();
            }

            const result : WorkspaceEdit = new WorkspaceEdit();
            const symbols: KSPSymbolInformation[] = KSPSymbolUtil.collect( document, token );
            const org    : string = KSPSymbolUtil.parseSymbolAt( document, position );
            let renamed : boolean = false;
            let regex : RegExp = undefined;

            if( KSPSymbol.isVariable( org ) )
            {
                regex = new RegExp( "\\" + org, 'g' );
            }
            else if( !KSPSyntaxUtil.matchKeyword( org ) && !KSPSyntaxUtil.matchLiteral( org ) )
            {
                regex = new RegExp( org, "g" );
            }

            // replace
            if( regex )
            {
                for( let i = 0; i < document.lineCount; i++ )
                {
                    let lineText = document.lineAt( i ).text;
                    if( lineText.match( regex ) )
                    {
                        let orgLength = lineText.length;
                        lineText      = lineText.replace( regex, newName );
                        result.replace( document.uri, new Range( i, 0, i, orgLength ), lineText );
                    }
                }
                renamed = true;
            }

            if( renamed )
            {
                ressolve( result );
            }
            else
            {
                reject();
            }
        });
    }
}
