/* =========================================================================

    KSPObfuscatorCommand.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import * as vscode          from 'vscode';
import * as path            from 'path';
import * as fs              from 'fs';
import * as tmp             from 'tmp';
import * as clipboard       from 'clipboardy';

import * as config          from '../KSPConfigurationConstants';

import { KSPConfigurationManager }  from '../KSPConfigurationManager';
import { KSPCompileBuilder }        from '../KSPCompileBuilder';
import { KSPCompileExecutor }       from '../KSPCompileExecutor';

export function doObfuscate( context: vscode.ExtensionContext )
{
    let editor              = vscode.window.activeTextEditor;
    let textDocument: vscode.TextDocument;
    let baseName: string;
    let suffix = config.DEFAULT_OBFUSCATOR_SUFFIX;
    let REG_SUFFIX: RegExp  = /(.*)(?:\.([^.]+$))/;
    let defaultOutputDir: string;
    let defaultOutputName: string;
    let defaultOutputPath: string;
    let toClipboard: boolean = config.DEFAULT_DEST_CLIPBOARD;

    suffix      = KSPConfigurationManager.getConfig<string>( config.KEY_OBFUSCATOR_SUFFIX, config.DEFAULT_OBFUSCATOR_SUFFIX );
    toClipboard = KSPConfigurationManager.getConfig<boolean>( config.KEY_OBFUSCATOR_DEST_CLIPBOARD, config.DEFAULT_DEST_CLIPBOARD );

    const MESSAGE_PREFIX: string        = "KSP Obfuscator(BETA)";
    const MESSAGE_FAILED: string        = "Failed";
    const MESSAGE_CLIPBOARD: string     = "Obfuscated code has been copied to clipboard";


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

    baseName = path.basename( textDocument.fileName );

    //--------------------------------------------------------------------------
    // Initialize from context
    //--------------------------------------------------------------------------
    defaultOutputDir  = path.dirname( textDocument.fileName );
    defaultOutputName = path.basename( textDocument.fileName );
    {
        let group: string[] = defaultOutputName.match( REG_SUFFIX );
        if( group.length >= 2 )
        {
            defaultOutputName = defaultOutputName.match( REG_SUFFIX )[ 1 ];
        }
    }
    defaultOutputPath = path.join( defaultOutputDir, defaultOutputName ) + suffix;

    //--------------------------------------------------------------------------
    // Run Obfuscator function
    //--------------------------------------------------------------------------
    function obfuscate( output:string, outputToClipboard: boolean, callback?: (exitCode: number)=>void ){

        let inline: boolean = KSPConfigurationManager.getConfig<boolean>( config.KEY_OBFUSCATOR_INLINE_FUNCTION, config.DEFAULT_INLINE_FUNCTION );
        let parser: KSPCompileExecutor      = KSPCompileExecutor.getCompiler( textDocument ).init();
        let argBuilder: KSPCompileBuilder   = new KSPCompileBuilder( textDocument.fileName, null, true, inline, output );

        parser.OnExit = (exitCode:number) => {

            if( exitCode != 0 )
            {
                vscode.window.showErrorMessage( `${MESSAGE_PREFIX}: ${MESSAGE_FAILED}. Please check your script : ${baseName}` );
            }
            else
            {
                if( toClipboard )
                {
                    vscode.window.showInformationMessage( `${MESSAGE_PREFIX}: ${MESSAGE_CLIPBOARD}` );
                }
                else
                {
                    vscode.window.showInformationMessage( `${MESSAGE_PREFIX}: Saved to ${baseName}` );
                }
            }

            if( callback )
            {
                callback( exitCode );
            }
        };
        parser.OnException = (error:Error) => {
            vscode.window.showErrorMessage( `${MESSAGE_PREFIX}: ${MESSAGE_FAILED} : ${baseName}` );
        }

        parser.execute( textDocument, argBuilder );

    }; //~function obfuscate

    // Output to Clipboard
    if( toClipboard )
    {
        let tmpFile: tmp.SynchrounousResult;
        tmpFile = tmp.fileSync();
        obfuscate( tmpFile.name, true, ( exitCode )=>{
            if( exitCode == 0 )
            {
                let txt: string = fs.readFileSync( tmpFile.name ).toString();
                clipboard.writeSync( txt );
                try { tmpFile.removeCallback(); }catch( e ){}
            }
        });
    }
    // Output to File
    else
    {
        vscode.window.showSaveDialog({
            defaultUri: vscode.Uri.file( defaultOutputPath ),
            filters:{ 'KSP Script': [ 'txt', 'ksp' ] }
        }).then( result=>{
            if( result )
            {
                obfuscate( result.path, false );
            }
        });
    }
}
