/* =========================================================================

    KSPLintCommand.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import * as vscode          from 'vscode';
import * as fs              from 'fs';
import * as path            from 'path';
import * as clipboard       from 'clipboardy';

import { KSPCompileBuilder }    from '../KSPCompileBuilder';
import { KSPCompileExecutor }   from '../KSPCompileExecutor';

export function doLint( context: vscode.ExtensionContext )
{
    let editor: vscode.TextEditor = vscode.window.activeTextEditor;
    let textDocument: vscode.TextDocument;
    let baseName: string;
    let scriptFilePath: string;

    const MESSAGE_PREFIX: string        = "KSP";
    const MESSAGE_FAILED: string        = "Failed";
    const MESSAGE_CLIPBOARD: string     = "No error. Script has been copied to clipboard";

    //--------------------------------------------------------------------------
    // Preverify
    //--------------------------------------------------------------------------

    // Any files not opened
    if( !editor )
    {
        vscode.window.showErrorMessage( "Editor not opened" );
        return;
    }

    textDocument = editor.document;
    if( textDocument.languageId !== "ksp" )
    {
        vscode.window.showErrorMessage( `${MESSAGE_PREFIX}: Language mode is not 'ksp'` );
        return;
    }

    scriptFilePath = textDocument.fileName;
    baseName       = path.basename( textDocument.fileName );

    if( textDocument.isUntitled || textDocument.isDirty )
    {
        vscode.window.showErrorMessage( `${MESSAGE_PREFIX}: ${baseName} - File is not saved.` );
        return;
    }

    //--------------------------------------------------------------------------
    // Run Syntax parser
    //--------------------------------------------------------------------------
    function runCompiler( callback?: (exitCode: number)=>void ){

        let argBuilder: KSPCompileBuilder   = new KSPCompileBuilder( scriptFilePath, null, false, false );
        let compiler: KSPCompileExecutor    = new KSPCompileExecutor();

        compiler.onExit = (exitCode:number) => {
            if( exitCode != 0 )
            {
                vscode.window.showErrorMessage( `${MESSAGE_PREFIX}: ${MESSAGE_FAILED}. Please check your script : ${baseName}` );
            }
            else
            {
                vscode.window.showInformationMessage( `${MESSAGE_PREFIX}: ${MESSAGE_CLIPBOARD}` );
            }
            if( callback )
            {
                callback( exitCode );
            }
        };
        compiler.onException = (e:Error) => {
            vscode.window.showErrorMessage( `${MESSAGE_PREFIX}: ${MESSAGE_FAILED} : ${baseName}` );
        };

        compiler.execute( textDocument, argBuilder );

    }; //~function runParser

    // Output to Clipboard
    {
        runCompiler( ( exitCode )=>{
            if( exitCode == 0 )
            {
                let txt: string = fs.readFileSync( scriptFilePath ).toString();
                clipboard.writeSync( txt );
            }
        });
    }
}
