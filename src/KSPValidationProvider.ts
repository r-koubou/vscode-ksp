/* =========================================================================

    KSPValidationProvider.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

// Based on PHP Validation Provider implementation. (validationProvider.ts)

import * as vscode          from 'vscode';
import * as cp              from 'child_process';
import { ThrottledDelayer } from './libs/async';


const CheckedExecutablePath = 'ksp.validate.checkedExecutablePath';

export class KSPValidationProvider
{
    private static MESSAGE_DELIMITER: string = "\t";

    private validationEnabled: boolean = true;
    private executable: string;
    private pauseValidation: boolean   = false;

    private documentListener: vscode.Disposable;
    private diagnosticCollection: vscode.DiagnosticCollection;
    private delayers: { [key: string]: ThrottledDelayer<void> };

    /**
     * ctor
     */
    constructor( private workspaceStore: vscode.Memento )
    {
        this.delayers = Object.create( null );
        this.documentListener = vscode.workspace.onDidSaveTextDocument( this.triggerValidate, this) ;
    }

    /**
     * provider activated
     */
    public activate( subscriptions: vscode.Disposable[] )
    {
        this.diagnosticCollection = vscode.languages.createDiagnosticCollection();
        subscriptions.push( this );
        //##vscode.workspace.onDidChangeConfiguration( this.loadConfiguration, this, subscriptions );
        //##this.loadConfiguration();
        vscode.workspace.onDidOpenTextDocument( this.triggerValidate, this, subscriptions );
        vscode.workspace.onDidCloseTextDocument( (textDocument) =>
        {
            this.diagnosticCollection.delete( textDocument.uri );
            delete this.delayers[ textDocument.uri.toString() ];
        }, null, subscriptions );

        subscriptions.push(
            vscode.commands.registerCommand(
            'ksp.untrustValidationExecutable',
            this.untrustValidationExecutable, this
        ));
    }

    /**
     * Release resources
     */
    public dispose(): void
    {
        if( this.diagnosticCollection )
        {
            this.diagnosticCollection.clear();
            this.diagnosticCollection.dispose();
        }
    }

    /**
     *
     */
    private loadConfiguration(): void
    {}

    /**
     *
     */
    private untrustValidationExecutable()
    {
        this.workspaceStore.update( CheckedExecutablePath, undefined );
        vscode.commands.executeCommand( 'setContext', 'ksp.untrustValidationExecutableContext', false );
    }

    private triggerValidate( textDocument: vscode.TextDocument ): void
    {
        if( textDocument.languageId !== "ksp" || !this.validationEnabled )
        {
            return;
        }

        let trigger = () =>
        {
            let key = textDocument.uri.toString();
            let delayer = this.delayers[ key ];
            if( !delayer )
            {
                delayer = new ThrottledDelayer<void>( 0 );
                this.delayers[ key ] = delayer;
            }
            // Exec syntax parser
            delayer.trigger( () => this.doValidate( textDocument ) );
        };
        trigger();
    }

    /**
     * Do syntax parser program
     */
    private doValidate(textDocument: vscode.TextDocument): Promise<void>
    {
        return new Promise<void>( (resolve, reject) =>
        {
            let exec = "java";
            let diagnostics: vscode.Diagnostic[] = [];

            let processLine = (line: string) =>
            {
                let msg: string[] = line.split( "\t" );
                if( msg.length > 0 )
                {
                    let line    = Number.parseFloat( msg[ 0 ] );
                    let message = msg[ 1 ];
                    let diagnostic: vscode.Diagnostic = new vscode.Diagnostic(
                        new vscode.Range( line, 0, line, Number.MAX_VALUE ),
                        message
                    );
                    diagnostics.push( diagnostic );
                }
            }; //~ processLine

            let thisExtention    = vscode.extensions.getExtension( "R-Koubou.kontakt-script-langage" );
            let thisExtentionDir = thisExtention.extensionPath;
            let options       = vscode.workspace.rootPath ? { cwd: vscode.workspace.rootPath } : undefined;
            let args: string[] = [];
            // java -Dkspparser.datadir=path/to/data -jar kspsyntaxparser.jar <document.fileName>
            args.push( "-Dkspparser.datadir=" + thisExtentionDir + "/kspparser/data/lang/message" )
            args.push( "-jar" );
            args.push( thisExtentionDir + "/kspparser/kspsyntaxparser.jar" );
            args.push( textDocument.fileName );

            try
            {
                let childProcess = cp.spawn( exec, args, undefined );

                childProcess.on( 'error', (error: Error) =>
                {
                    console.log( error );
                    if( this.pauseValidation )
                    {
                        resolve();
                        return;
                    }
                    this.showError( error, exec );
                    this.pauseValidation = true;
                    resolve();
                });

                if( childProcess.pid )
                {
                    // handling stdout
                    childProcess.stdout.on( 'data', (data: Buffer) =>
                    {
                        processLine( data.toString() );
                    });
                    // process finished
                    childProcess.stdout.on( 'end', () =>
                    {
                        this.diagnosticCollection.set( textDocument.uri, diagnostics );
                        resolve();
                    });
                }
                else
                {
                    resolve();
                }
            }
            catch( e )
            {
                this.showError( e, exec );
            }

        });
    }

    private showError( error: any, executable: string ): void
    {
        let message: string = "KSP fatal error";
        if( error.message )
        {
            message = error.message;
        }
        vscode.window.showInformationMessage( message );
    }

}
