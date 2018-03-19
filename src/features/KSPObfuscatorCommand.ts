/* =========================================================================

    KSPObfuscatorCommand.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import * as vscode          from 'vscode';
import * as cp              from 'child_process';

import * as kspconst        from './KSPExtensionConstants';

export function doObfuscate( context: vscode.ExtensionContext )
{
    let editor              = vscode.window.activeTextEditor;
    let thisExtention       = vscode.extensions.getExtension( kspconst.EXTENSION_ID );
    let thisExtentionDir    = thisExtention.extensionPath;
    let options             = vscode.workspace.rootPath ? { cwd: vscode.workspace.rootPath } : undefined;
    let args: string[]      = [];
    vscode.window.showInformationMessage( "KSP: Sorry. Command has NOT been implemented yet" );
}
