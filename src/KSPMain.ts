/* =========================================================================

    KSPMain.ts
    Copyright(c) R-Koubou

   [License]
   MIT

   ======================================================================== */

// Based on PHP main implementation. (phpMain.js)

'use strict';

import vscode  = require( 'vscode' );

import { KSPCompletionItemProvider } from './KSPCompletionItemProvider';
import { KSPHoverProvider } from './KSPHoverProvider';
import { KSPSignatureHelpProvider } from './KSPSignatureHelpProvider';

function activate(context)
{
    context.subscriptions.push(
        vscode.languages.registerCompletionItemProvider(
            'ksp', new KSPCompletionItemProvider(), '$', '%', '~', '?', '@', '!' )
    );

    context.subscriptions.push(
        vscode.languages.registerHoverProvider( 'ksp', new KSPHoverProvider() )
    );

    context.subscriptions.push(
        vscode.languages.registerSignatureHelpProvider( 'ksp', new KSPSignatureHelpProvider(), '(', ',' )
    );

    vscode.languages.setLanguageConfiguration( 'ksp', {
        wordPattern: /(-?\d*\.\d\w*)|([^\-\`\#\^\&\*\(\)\=\+\[\{\]\}\\\|\;\:\'\"\,\.\<\>\/\?\s]+)/g
    });

}

exports.activate = activate;
