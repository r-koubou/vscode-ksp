/* =========================================================================

    CompletionItemProvider.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

// Based on PHP HOver Provider implementation. (hoverProvider.js)

'use strict';

import vscode = require( 'vscode' );

var kspBuiltinVariables = require( './generated/KSPVariables' );
var kspCommands         = require( './generated/KSPCommands' );

export class KSPHoverProvider
{
    /**
     * Ctor.
     */
    constructor()
    {}

    /**
     * Implementation of Hover behaviour
     */
    public provideHover( doc, pos, token ) : vscode.Hover
    {
        var wordRange = doc.getWordRangeAtPosition( pos );
        if( !wordRange )
        {
            return null;
        }

        var name  = doc.getText( wordRange );
        var entry = kspCommands.commands[ name ] ||
                    kspBuiltinVariables.builtinVariables[ name ];

        if( entry && entry.description )
        {
            var signature = name + ( entry.signature || '' );
            var contents  = [ entry.description, { language: 'ksp', value: signature } ];
            return new vscode.Hover( contents, wordRange );
        }
        return null;
    }
}
