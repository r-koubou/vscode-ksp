/* =========================================================================

    CompletionItemProvider.js
    Copyright(c) R-Koubou

   [License]
   MIT

   ======================================================================== */

// Based on PHP HOver Provider implementation. (hoverProvider.js)

'use strict';

var assert = require( 'assert' );
var vscode = require( 'vscode' );

var kspBuiltinVariables = require( './KSPVariables' );
var kspCommands         = require( './KSPCommands' );

var KSPHoverProvider = (function()
{
    function KSPHoverProvider(){}

    KSPHoverProvider.prototype.provideHover = function ( doc, pos, token )
    {
        var wordRange = doc.getWordRangeAtPosition( pos );
        if( !wordRange )
        {
            return;
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
    };
    return KSPHoverProvider;
}());

Object.defineProperty( exports, "__esModule", { value: true } );
exports.default = KSPHoverProvider;
