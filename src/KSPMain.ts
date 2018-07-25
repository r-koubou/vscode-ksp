/* =========================================================================

    KSPMain.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

'use strict';

import vscode  = require( 'vscode' );

import * as Constants                   from './features/KSPExtensionConstants';

import { KSPCompletionItemProvider }    from './features/KSPCompletionItemProvider';
import { KSPHoverProvider }             from './features/KSPHoverProvider';
import { KSPSignatureHelpProvider }     from './features/KSPSignatureHelpProvider';
import { KSPDocumentSymbolProvider }    from './features/KSPDocumentSymbolProvider';
import { KSPDefinitionProvider }        from './features/KSPDefinitionProvider';
import { KSPReferenceProvider }         from './features/KSPReferenceProvider';
import { KSPValidationProvider }        from './features/KSPValidationProvider';
import { KSPRenameProvider }            from './features/KSPRenameProvider';

import KSPCommandSetup                  = require( './features/commands/KSPCommandSetup' );

export function activate( context:vscode.ExtensionContext ) : any
{

//------------------------------------------------------------------------------
// Providers
//------------------------------------------------------------------------------

    context.subscriptions.push(
        vscode.languages.registerCompletionItemProvider(
            Constants.LANG_ID, new KSPCompletionItemProvider(), '$', '%', '~', '?', '@', '!' )
    );

    context.subscriptions.push(
        vscode.languages.registerHoverProvider( Constants.LANG_ID, new KSPHoverProvider() )
    );

    context.subscriptions.push(
        vscode.languages.registerSignatureHelpProvider( Constants.LANG_ID, new KSPSignatureHelpProvider(), '(', ',' )
    );

    context.subscriptions.push(
        vscode.languages.registerDocumentSymbolProvider( Constants.LANG_ID, new KSPDocumentSymbolProvider() )
    );

    context.subscriptions.push(
        vscode.languages.registerDefinitionProvider( Constants.LANG_ID, new KSPDefinitionProvider() )
    );

    context.subscriptions.push(
        vscode.languages.registerReferenceProvider( Constants.LANG_ID, new KSPReferenceProvider() )
    );

    context.subscriptions.push(
        vscode.languages.registerRenameProvider( Constants.LANG_ID, new KSPRenameProvider() )
    );

//------------------------------------------------------------------------------
// Commands
//------------------------------------------------------------------------------

    KSPCommandSetup.setupCommands( context );

//------------------------------------------------------------------------------
// Language Configuration
//------------------------------------------------------------------------------

    vscode.languages.setLanguageConfiguration( Constants.LANG_ID, {
        wordPattern: /(-?\d*\.\d\w*)|([^\-\`\#\^\&\*\(\)\=\+\[\{\]\}\\\|\;\:\'\"\,\.\<\>\/\?\s]+)/g
    });

//------------------------------------------------------------------------------
// Other setup
//------------------------------------------------------------------------------

    const validator = new KSPValidationProvider( context.workspaceState );
    validator.activate( context );

}
