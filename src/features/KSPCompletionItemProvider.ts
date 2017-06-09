/* =========================================================================

    KSPCompletionItemProvider.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

// Implemented based on Part of PHP Completion ItemProvider implementation. (completionItemProvider.ts)

'use strict';

import vscode = require( 'vscode' );

var kspBuiltinVariables = require( './generated/KSPBuiltinVariableInfo' );
var kspCommands         = require( './generated/KSPCommandsInfo' );

const VARIABLE_PREFIX_LIST    = [ '$', '%', '~', '?', '@', '!' ];
const VARIABLE_REGEX          = /([\$%~\?@!][a-zA-Z_\x7f-\xff][a-zA-Z0-9_\x7f-\xff]*)/g;
const FUNCTION_REGEX          = /function\s+([a-zA-Z_\x7f-\xff][a-zA-Z0-9_\x7f-\xff]*)\s*/g

export class KSPCompletionItemProvider
{
    private triggerCharacters : string[];

    /**
     * Ctor.
     */
    constructor()
    {
        this.triggerCharacters = VARIABLE_PREFIX_LIST;
    }

    /**
     * Implementation of Completion behaviour
     */
    public provideCompletionItems( doc, pos, token )
    {
        var result = [];
        var range  = doc.getWordRangeAtPosition( pos );
        var prefix = range ? doc.getText( range ) : '';

        if( !range )
        {
            range = new vscode.Range( pos, pos );
        }
        var added = {};

        //----------------------------------------------------------------------
        // proposal component
        //----------------------------------------------------------------------
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

        //----------------------------------------------------------------------
        // matcher
        //----------------------------------------------------------------------
        var matches = function( name )
        {
            return prefix.length === 0 ||
                   name.length >= prefix.length &&
                   name.substr( 0, prefix.length ) === prefix;
        };

        //----------------------------------------------------------------------
        // Key-Value Validate
        //----------------------------------------------------------------------
        var validateProposal = function( name, table, kind )
        {
            if( table.hasOwnProperty( name ) && matches( name ) )
            {
                added[ name ] = true;
                result.push( createNewProposal( kind, name, table[ name ] ) ) ;
            }
        }
        //----------------------------------------------------------------------
        // Built-In Variables
        //----------------------------------------------------------------------
        for( var name in kspBuiltinVariables.builtinVariables )
        {
            validateProposal( name, kspBuiltinVariables.builtinVariables, vscode.CompletionItemKind.Variable );
        }
        //----------------------------------------------------------------------
        // Commands
        //----------------------------------------------------------------------
        for( var name in kspCommands.commands )
        {
            validateProposal( name, kspCommands.commands, vscode.CompletionItemKind.Function );
        }
        //----------------------------------------------------------------------
        // User Variables in File
        //----------------------------------------------------------------------
        {
            var text          = doc.getText();
            var prefixMatched = false;

            VARIABLE_PREFIX_LIST.forEach( function( element ){
                if( prefixMatched ) return;
                if( prefix[ 0 ] === element )
                {
                    prefixMatched = true;
                }
            });

            if( prefixMatched )
            {
                var variableMatch = VARIABLE_REGEX;
                var match = null;
                while( match = variableMatch.exec( text ) )
                {
                    var word = match[ 0 ];
                    if( !added[ word ] )
                    {
                        added[ word ] = true;
                        result.push( createNewProposal( vscode.CompletionItemKind.Variable, word, null ) );
                    }
                }
            }
        }
        //----------------------------------------------------------------------
        // User Functions in File
        //----------------------------------------------------------------------
        {
            var functionMatch = FUNCTION_REGEX;
            var match = null;
            while( match = functionMatch.exec( text ) )
            {
                var word = match[ 1 ];
                if( !added[ word ] )
                {
                    added[ word ] = true;
                    result.push( createNewProposal( vscode.CompletionItemKind.Function, word, null ) );
                }
            }
        }

        return Promise.resolve( result );
    }
}
