/* =========================================================================

    KSPDocumentSymbolProvider.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import vscode = require( 'vscode' );

export class KSPDocumentSymbolProvider implements vscode.DocumentSymbolProvider
{
    constructor()
    {
    }

    public provideDocumentSymbols(
        document: vscode.TextDocument,
        token: vscode.CancellationToken ) : Thenable<vscode.SymbolInformation[]>
    {
        var result: vscode.SymbolInformation[] = [];

        let count = document.lineCount;
        for( var i = 0; i < count; i++ )
        {
            //-----------------------------------------------------------------
            // check declare variables
            //-----------------------------------------------------------------
            {
                var DECLARE_REGEX = /^\s*declare\s+(ui_[a-z|A-Z]+|const)?\s*([\$%~\?@!][a-zA-Z_\x7f-\xff][a-zA-Z0-9_\x7f-\xff]*)/g;

                var text  = document.lineAt( i ).text;
                var match = DECLARE_REGEX.exec( text );
                if( match )
                {
                    var isConst                     = match[ 1 ] && match[ 1 ].toString() == "const";
                    var isUi                        = match[ 1 ] && match[ 1 ].startsWith( "ui_" );
                    var symKind: vscode.SymbolKind  = vscode.SymbolKind.Variable;
                    var name : string               = match[ 2 ];
                    var containerName : string      = "Variable";

                    match.forEach( element => {
                        console.log( element );
                    });

                    if( isConst )
                    {
                        containerName = "Constant Variable";
                        symKind = vscode.SymbolKind.Constant;
                    }

                    var variableTypeChar            = name.charAt( 0 );
                    var variableType : string       = "(unkown)";
                    if     ( variableTypeChar == '$' ) { variableType = "(Integer)"; }
                    else if( variableTypeChar == '~' ) { variableType = "(Real)"; }
                    else if( variableTypeChar == '@' ) { variableType = "(String)"; }
                    else if( variableTypeChar == '%' ) { variableType = "(Integer Array)"; }
                    else if( variableTypeChar == '?' ) { variableType = "(Real Array)"; }
                    else if( variableTypeChar == '!' ) { variableType = "(String Array)"; }

                    if( isUi )
                    {
                        containerName = "UI Variable";
                        variableType  = "(" + match[ 1 ].trim() + ")";
                    }

                    var add = new vscode.SymbolInformation(
                        name.substr( 1 ),
                        symKind, containerName + " " + variableType,
                        new vscode.Location( document.uri, new vscode.Position( i, i ) )
                    );
                    result.push( add );
                    continue;
                }
            } //~check declare variables
            //-----------------------------------------------------------------
            // check callback ( on #### )
            //-----------------------------------------------------------------
            {
                var DECLARE_REGEX = /^\s*(on\s+)([a-z|A-Z|_]+)(\s*\(\s*[^\)]+\s*\))?/g;

                var text  = document.lineAt( i ).text;
                var match = DECLARE_REGEX.exec( text );
                if( match )
                {
                    var isUi                        = match[ 2 ] != undefined && match[ 3 ] != undefined && match[ 2 ].startsWith( "ui_" );
                    var symKind: vscode.SymbolKind  = vscode.SymbolKind.Function;
                    var name : string               = match[ 2 ];
                    var containerName : string      = "Callback";

                    if( !match[ 2 ] && !match[ 3 ] )
                    {
                        continue;
                    }

                    name = "on " + name;

                    if( isUi )
                    {
                        var uiName = match[ 3 ].replace( "(", "" ).replace( ")", "" ).trim();
                        containerName = "UI Callback for " + uiName;
                    }

                    var add = new vscode.SymbolInformation(
                        name,
                        symKind, containerName,
                        new vscode.Location( document.uri, new vscode.Position( i, i ) )
                    );
                    result.push( add );
                    continue;
                }
            } //~callback
            //-----------------------------------------------------------------
            // check user function ( function #### )
            //-----------------------------------------------------------------
            {
                var DECLARE_REGEX = /^\s*(function\s+)([a-z|A-Z|_]+)/g;

                var text  = document.lineAt( i ).text;
                var match = DECLARE_REGEX.exec( text );
                if( match )
                {
                    var symKind: vscode.SymbolKind  = vscode.SymbolKind.Function;
                    var name : string               = match[ 2 ];
                    var containerName : string      = "Function";
                    var add = new vscode.SymbolInformation(
                        name,
                        symKind, containerName,
                        new vscode.Location( document.uri, new vscode.Position( i, i ) )
                    );
                    result.push( add );
                    continue;
                }
            } //~function

        }
        return Promise.resolve( result );
    }
}
