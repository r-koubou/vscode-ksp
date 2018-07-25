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

import { KSPCompileBuilder}       from '../KSPCompileBuilder';
import { KSPSyntaxParserExecutor} from '../KSPSyntaxUtil';

export function doLint( context: vscode.ExtensionContext )
{
    let editor              = vscode.window.activeTextEditor;
    let args: string[]      = [];
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
    function runParser( callback?: (exitCode: number)=>void ){

        let argBuilder: KSPCompileBuilder   = new KSPCompileBuilder( scriptFilePath, null, false, false );
        let parser: KSPSyntaxParserExecutor = new KSPSyntaxParserExecutor();
        args = argBuilder.build();

        parser.onExit = (exitCode:number)=> {
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
        }
        parser.onException = (e:Error) => {
            vscode.window.showErrorMessage( `${MESSAGE_PREFIX}: ${MESSAGE_FAILED} : ${baseName}` );
        }

        parser.execSyntaxParser( args );

    }; //~function runParser

    // Output to Clipboard
    {
        runParser( ( exitCode )=>{
            if( exitCode == 0 )
            {
                let txt: string = fs.readFileSync( scriptFilePath ).toString();
                clipboard.writeSync( txt );
            }
        });
    }
}
