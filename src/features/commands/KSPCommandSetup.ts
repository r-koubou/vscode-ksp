/* =========================================================================

    KSPCommandSetup.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import * as vscode from 'vscode';

import KSPObfuscatorCommand =           require( './KSPObfuscatorCommand' );
import KSPLintCommand       =           require( './KSPLintCommand' );

/**
 * Register this extension's commands
 */
export function setupCommands( context: vscode.ExtensionContext )
{
    context.subscriptions.push(
        vscode.commands.registerCommand( 'ksp.obfuscate', KSPObfuscatorCommand.doObfuscate )
    );

    context.subscriptions.push(
        vscode.commands.registerCommand( 'ksp.parse.syntax', KSPLintCommand.doLint )
    );
}
