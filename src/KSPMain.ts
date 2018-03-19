/* =========================================================================

    KSPMain.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

'use strict';

import vscode  = require( 'vscode' );

import { KSPCompletionItemProvider }    from './features/KSPCompletionItemProvider';
import { KSPHoverProvider }             from './features/KSPHoverProvider';
import { KSPSignatureHelpProvider }     from './features/KSPSignatureHelpProvider';
import { KSPDocumentSymbolProvider }    from './features/KSPDocumentSymbolProvider';
import { KSPDefinitionProvider }        from './features/KSPDefinitionProvider';
import { KSPReferenceProvider }         from './features/KSPReferenceProvider';
import { KSPValidationProvider }        from './features/KSPValidationProvider';
import { KSPRenameProvider }            from './features/KSPRenameProvider';

import KSPObfuscatorCommand =           require( './features/KSPObfuscatorCommand' );

export function activate( context:vscode.ExtensionContext ) : any
{
    let validator = new KSPValidationProvider( context.workspaceState );
    validator.activate( context.subscriptions );

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

    context.subscriptions.push(
        vscode.languages.registerDocumentSymbolProvider( 'ksp', new KSPDocumentSymbolProvider() )
    );

    context.subscriptions.push(
        vscode.languages.registerDefinitionProvider( 'ksp', new KSPDefinitionProvider() )
    );

    context.subscriptions.push(
        vscode.languages.registerReferenceProvider( 'ksp', new KSPReferenceProvider() )
    );

    context.subscriptions.push(
        vscode.languages.registerRenameProvider( 'ksp', new KSPRenameProvider() )
    );

    context.subscriptions.push(
        vscode.commands.registerCommand( 'ksp.obfuscate', KSPObfuscatorCommand.doObfuscate )
    );

    vscode.languages.setLanguageConfiguration( 'ksp', {
        wordPattern: /(-?\d*\.\d\w*)|([^\-\`\#\^\&\*\(\)\=\+\[\{\]\}\\\|\;\:\'\"\,\.\<\>\/\?\s]+)/g
    });

}
