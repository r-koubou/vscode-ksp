/* =========================================================================

    kspMain.js
    Copyright(c) R-Koubou

   [License]
   MIT

   ======================================================================== */

'use strict';

var assert = require( 'assert' );
var vscode = require( 'vscode' );

var completionItemProvider = require( './files/KSPCompletionItemProvider' );

function activate(context)
{
    context.subscriptions.push(
        vscode.languages.registerCompletionItemProvider(
            'ksp', new completionItemProvider.default(), '$', '%', '~', '?', '@', '!' )
    );
    vscode.languages.setLanguageConfiguration( 'ksp', {
        wordPattern: /(-?\d*\.\d\w*)|([^\-\`\#\^\&\*\(\)\=\+\[\{\]\}\\\|\;\:\'\"\,\.\<\>\/\?\s]+)/g
    });

}

exports.activate = activate;
