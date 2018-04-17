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

export const VARIABLE_PREFIX_LIST: string[]    = [ '$', '%', '~', '?', '@', '!' ];
export const VARIABLE_REGEX: RegExp            = /([\$%~\?@!][a-zA-Z_\x7f-\xff][a-zA-Z0-9_\x7f-\xff]*)/g;
export const FUNCTION_REGEX: RegExp            = /function\s+([a-zA-Z_\x7f-\xff][a-zA-Z0-9_\x7f-\xff]*)\s*/g

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
    public provideCompletionItems( textDocument: vscode.TextDocument, position: vscode.Position, token: vscode.CancellationToken )
    {
        let result = [];
        let range  = textDocument.getWordRangeAtPosition( position );
        let prefix = range ? textDocument.getText( range ) : '';
        let text   = textDocument.getText();

        if( !range )
        {
            range = new vscode.Range( position, position );
        }
        let added = {};

        //----------------------------------------------------------------------
        // proposal component
        //----------------------------------------------------------------------
        let createNewProposal = function( kind: vscode.CompletionItemKind, name: string, entry: any )
        {
            let proposal  = new vscode.CompletionItem( name );
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
        let matches = function( name: string )
        {
            return prefix.length === 0 ||
                   name.length >= prefix.length &&
                   name.substr( 0, prefix.length ) === prefix;
        };

        //----------------------------------------------------------------------
        // Key-Value Validate
        //----------------------------------------------------------------------
        let validateProposal = function( name: string, table: any, kind: vscode.CompletionItemKind )
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
        for( let name in kspBuiltinVariables.builtinVariables )
        {
            validateProposal( name, kspBuiltinVariables.builtinVariables, vscode.CompletionItemKind.Variable );
        }
        //----------------------------------------------------------------------
        // Commands
        //----------------------------------------------------------------------
        for( let name in kspCommands.commands )
        {
            validateProposal( name, kspCommands.commands, vscode.CompletionItemKind.Function );
        }
        //----------------------------------------------------------------------
        // User Variables in File
        //----------------------------------------------------------------------
        {
            let prefixMatched = false;

            VARIABLE_PREFIX_LIST.forEach( function( element ){
                if( prefixMatched ) return;
                if( prefix[ 0 ] === element )
                {
                    prefixMatched = true;
                }
            });

            if( prefixMatched )
            {
                let variableMatch = VARIABLE_REGEX;
                let match = null;
                while( match = variableMatch.exec( text ) )
                {
                    let word = match[ 0 ];
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
            let functionMatch = FUNCTION_REGEX;
            let match = null;
            while( match = functionMatch.exec( text ) )
            {
                let word = match[ 1 ];
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
