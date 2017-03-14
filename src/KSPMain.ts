/* =========================================================================

    kspMain.js
    Copyright(c) R-Koubou

   [License]
   MIT

   ======================================================================== */

// Based on PHP main implementation. (phpMain.js)

'use strict';

import vscode  = require( 'vscode' );

var completionItemProvider = require( 'KSPCompletionItemProvider' );
var hoverProvider          = require( 'KSPHoverProvider' );
var signatureHelpProvider  = require( 'KSPSignatureHelpProvider' );

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
