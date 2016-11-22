/* =========================================================================

    CompletionItemProvider.js
    Copyright(c) R-Koubou

   [License]
   MIT

   ======================================================================== */

// Based on PHP Completion ItemProvider implementation. (completionItemProvider.js)

'use strict';

var assert = require( 'assert' );
var vscode = require( 'vscode' );

var kspBuiltinVariables = require( './KSPVariables' );
var kspCommands         = require( './KSPCommands' );

var KSPCompletionItemProvider = (function () {

    /**
     * Ctor.
     */
    function KSPCompletionItemProvider()
    {
        this.triggerCharacters = [ '$', '%', '~', '?', '@', '!' ];
    }

    /**
     *
     */
    KSPCompletionItemProvider.prototype.provideCompletionItems = function( doc, pos, token ){

        var result = [];
        var range  = doc.getWordRangeAtPosition( pos );
        var prefix = range ? doc.getText( range ) : '';
//console.log( "pos:" + pos.character + "prefix:" + prefix );
        if( !range )
        {
            range = new vscode.Range( pos, pos );
        }
        var added = {};

        // proposal component
        var createNewProposal = function( kind, name, entry )
        {
            var proposal  = new vscode.CompletionItem( name );
            proposal.kind = kind;
            if( entry )
            {
                if( entry.description )
                {
                    proposal.documentation = entry.description;
                }
                if( entry.signature )
                {
                    proposal.detail = entry.signature;
                }
            }
            return proposal;
        };

        // matcher
        var matches = function( name )
        {
            return prefix.length === 0 ||
                   name.length >= prefix.length &&
                   name.substr( 0, prefix.length ) === prefix;
        };

        // Key-Value Validate
        var validateProposal = function( name, table, kind )
        {
            if( table.hasOwnProperty( name ) && matches( name ) )
            {
                added[ name ] = true;
                result.push( createNewProposal( kind, name, table[ name ] ) ) ;
            }
        }
        // Built-In Variables
        for( var name in kspBuiltinVariables.builtinVariables )
        {
            validateProposal( name, kspBuiltinVariables.builtinVariables, vscode.CompletionItemKind.Variable );
        }
        // Commands
        for( var name in kspCommands.commands )
        {
            validateProposal( name, kspCommands.commands, vscode.CompletionItemKind.Function );
        }

        return Promise.resolve( result );
    };

    return KSPCompletionItemProvider;

}());

Object.defineProperty( exports, "__esModule", { value: true } );
exports.default = KSPCompletionItemProvider;
