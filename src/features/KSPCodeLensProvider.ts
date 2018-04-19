/* =========================================================================

    KSPCodeLensProvider.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

'use strict';

import vscode = require( 'vscode' );

export class KSPCodeLensProvider implements vscode.CodeLensProvider
{
    public provideCodeLenses( document: vscode.TextDocument, token: vscode.CancellationToken ): vscode.CodeLens[] | Thenable<vscode.CodeLens[]>
    {
        let result : vscode.CodeLens[] = []
        return Promise.resolve( result );
    }

    public resolveCodeLens?( codeLens: vscode.CodeLens, token: vscode.CancellationToken ): vscode.CodeLens | Thenable<vscode.CodeLens>
    {
        return null;
    }
}
