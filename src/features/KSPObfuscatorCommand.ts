/* =========================================================================

    KSPObfuscatorCommand.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import * as vscode          from 'vscode';
import * as cp              from 'child_process';
import * as path            from 'path';

import * as kspconst        from './KSPExtensionConstants';
import * as config          from './KSPConfigurationConstants';

import { KSPConfigurationManager} from './KSPConfigurationManager';

export function doObfuscate( context: vscode.ExtensionContext )
{
    let editor              = vscode.window.activeTextEditor;
    let thisExtention       = vscode.extensions.getExtension( kspconst.EXTENSION_ID );
    let thisExtentionDir    = thisExtention.extensionPath;
    let options             = vscode.workspace.rootPath ? { cwd: vscode.workspace.rootPath } : undefined;
    let args: string[]      = [];
    let textDocument: vscode.TextDocument;
    let suffix = config.DEFAULT_OBFUSCATOR_SUFFIX;
    let REG_SUFFIX: RegExp  = /(.*)(?:\.([^.]+$))/;
    let defaultOutputDir: string;
    let defaultOutputName: string;
    let defaultOutputPath: string;

    KSPConfigurationManager.getConfig<string>( config.KEY_OBFUSCATOR_SUFFIX, config.DEFAULT_OBFUSCATOR_SUFFIX, (v, user) =>{
        suffix = v;
    });


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
        vscode.window.showErrorMessage( "Language mode is not 'ksp'" );
        return;
    }

    if( textDocument.isUntitled || textDocument.isDirty )
    {
        vscode.window.showErrorMessage( "File is not saved." );
        return;
    }

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
    function obfuscate( output:string ){

        let inline: boolean = config.DEFAULT_INLINE_FUNCTION;
        KSPConfigurationManager.getConfig<boolean>( config.KEY_OBFUSCATOR_INLINE_FUNCTION, config.DEFAULT_INLINE_FUNCTION, (v, user) =>{
            inline = v;
        });

        // java -Dkspparser.stdout.encoding=UTF-8 -Dkspparser.datadir=path/to/data -jar kspsyntaxparser.jar <document.fileName>
        args.push( "-Dkspparser.stdout.encoding=UTF-8" )
        args.push( "-Dkspparser.datadir=" + thisExtentionDir + "/kspparser/data" )
        // launch en-US mode
        //            args.push( "-Duser.language=en" );
        //            args.push( "-Duser.country=US" );
        args.push( "-jar" );
        args.push( thisExtentionDir + "/kspparser/KSPSyntaxParser.jar" );
        args.push( "--strict" );
        args.push( "--obfuscate" );
        if( inline )
        {
            args.push( "--inline-userfunction" )
        }
        args.push( "--output" );
        args.push( output );
        args.push( textDocument.fileName );

        try
        {
            let exec = config.DEFAULT_JAVA_LOCATION;
            KSPConfigurationManager.getConfig<string>( config.KEY_JAVA_LOCATION, config.DEFAULT_JAVA_LOCATION, (v, user) =>{
                exec = v;
            });

            let childProcess = cp.spawn( exec, args, undefined );

            childProcess.on( 'error', (error: Error) =>
            {
                vscode.window.showErrorMessage( 'Command "java" not found' );
            });

            if( childProcess.pid )
            {
                let hasError: boolean = false;

                // handling stdout
                childProcess.stdout.on( 'data', (data: Buffer) =>
                {
                });
                // handling stderr
                childProcess.stderr.on( 'data', (data: Buffer) =>
                {
                    hasError = true;
                    console.log( data.toString() );
                });
                // process finished with exit code
                childProcess.on( 'exit', (exitCode) =>
                {
                    if( exitCode != 0 )
                    {
                        vscode.window.showErrorMessage( "KSP Obfuscator: Failed. Please check your script." );
                    }
                    else
                    {
                        vscode.window.showInformationMessage( "KSP Obfuscator: Successfully : " + path.basename( textDocument.fileName ) );
                    }
                });
            }
        }
        catch( e )
        {
            vscode.window.showErrorMessage( "Obfuscation failed : " + path.basename( textDocument.fileName ) );
        }

    }; //~function obfuscate

    vscode.window.showSaveDialog({
        defaultUri: vscode.Uri.file( defaultOutputPath ),
        filters:{ 'KSP Script': [ 'txt', 'ksp' ] }
    }).then( result=>{
        if( result )
        {
            obfuscate( result.path );
        }
    });
}
