/* =========================================================================

    kspMain.js
    Copyright(c) R-Koubou

   [License]
   MIT

   ======================================================================== */

// Based on PHP main implementation. (phpMain.js)

'use strict';

var assert = require( 'assert' );
var vscode = require( 'vscode' );

var completionItemProvider = require( './files/KSPCompletionItemProvider' );
var hoverProvider          = require( './files/KSPHoverProvider' );
var signatureHelpProvider  = require( './files/KSPSignatureHelpProvider' );

function activate(context)
{
    context.subscriptions.push(
        vscode.languages.registerCompletionItemProvider(
            'ksp', new completionItemProvider.default(), '$', '%', '~', '?', '@', '!' )
    );

    context.subscriptions.push(
        vscode.languages.registerHoverProvider( 'ksp', new hoverProvider.default() )
    );

    context.subscriptions.push(
        vscode.languages.registerSignatureHelpProvider( 'ksp', new signatureHelpProvider.default(), '(', ',' )
    );

    vscode.languages.setLanguageConfiguration( 'ksp', {
        wordPattern: /(-?\d*\.\d\w*)|([^\-\`\#\^\&\*\(\)\=\+\[\{\]\}\\\|\;\:\'\"\,\.\<\>\/\?\s]+)/g
    });

}

exports.activate = activate;
