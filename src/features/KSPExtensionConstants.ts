/* =========================================================================

    KSPExtensionConstants.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import * as vscode from 'vscode';
import * as path   from 'path';

export const EXTENSION_ID: string   = 'rkoubou.ksp';
export const LANG_ID: string        = 'ksp';
export const EXTENTION_DIR          = vscode.extensions.getExtension( EXTENSION_ID ).extensionPath;
export const RES_BASEDIR            = path.join( EXTENTION_DIR, 'resources', 'icon' );
