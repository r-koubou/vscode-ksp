/* =========================================================================

    KSPValidationProvider.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

// Based on PHP Validation Provider implementation. (validationProvider.ts)

import * as vscode          from 'vscode';
import * as cp              from 'child_process';
import * as tmp             from 'tmp';
import * as fs              from 'fs';
import { ThrottledDelayer } from './libs/async';
import * as config          from './KSPConfigConstants';

const CHECKED_EXECUTABLE_PATH = 'ksp.validate.checkedExecutablePath';
const PARSER_MESSAGE_DELIMITER: string = "\t";
const COMMAND_UNTRUST_VALIDATION_EXECUTABLE = 'ksp.untrustValidationExecutable'
const COMMAND_UNTRUST_VALIDATION_EXECUTABLE_CONTEXT = 'ksp.untrustValidationExecutableContext'

export class KSPValidationProvider
{

    private validationEnabled: boolean          = false;
    private realtimeValidationEnabled: boolean  = false;
    private realtimeValidationDelay:number      = config.DEFAULT_VARIDATE_DELAY;

    private executable: string                  = "java";
    private pauseValidation: boolean            = false;
    private realtimeTrigger: boolean            = false;

    private onSaveListener: vscode.Disposable;
    private onChangedListener: vscode.Disposable;
    private diagnosticCollection: vscode.DiagnosticCollection;
    private delayers: { [key: string]: ThrottledDelayer<void> };

    /**
     * ctor
     */
    constructor( private workspaceStore: vscode.Memento )
    {
        this.delayers = Object.create( null );
        this.onSaveListener = vscode.workspace.onDidSaveTextDocument( this.triggerValidate, this) ;
    }

    /**
     * provider activated
     */
    public activate( subscriptions: vscode.Disposable[] )
    {
        this.diagnosticCollection = vscode.languages.createDiagnosticCollection();
        subscriptions.push( this );

        this.initConfiguration();
        this.loadConfiguration();
        vscode.workspace.onDidChangeConfiguration( this.loadConfiguration, this, subscriptions );
        vscode.workspace.onDidOpenTextDocument( this.triggerValidate, this, subscriptions );
        vscode.workspace.onDidCloseTextDocument( (textDocument) =>
        {
            this.diagnosticCollection.delete( textDocument.uri );
            delete this.delayers[ textDocument.uri.toString() ];
        }, null, subscriptions );

        subscriptions.push(
            vscode.commands.registerCommand(
            COMMAND_UNTRUST_VALIDATION_EXECUTABLE,
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
        }
        this.doDispose( this.diagnosticCollection );
    }

    /**
     * Safety dispose
     */
    public doDispose( p?:vscode.Disposable )
    {
        if( p ) p.dispose();
    }

    /**
     * Initialize configuration.
     * When first time of activate this extention, store default values.
     */
    private initConfiguration(): void
    {
        let section: vscode.WorkspaceConfiguration = vscode.workspace.getConfiguration( config.CONFIG_SECTION_NAME );
        if( section )
        {
            let initConfig = function<T>(key:string, defaultValue:T )
            {
                if( !section.has( key) )
                {
                    section.update( key, defaultValue, true );
                }
            };
            initConfig<boolean>( config.KEY_ENABLE_VARIDATE,            config.DEFAULT_ENABLE_VARIDATE );
            initConfig<boolean>( config.KEY_ENABLE_REALTIME_VARIDATE,   config.DEFAULT_REALTIME_VARIDATE );
            initConfig<number>(  config.KEY_REALTIME_VARIDATE_DELAY,    config.DEFAULT_VARIDATE_DELAY );
        }
    }

    /**
     * Load configuration for validation
     */
    private loadConfiguration(): void
    {
        let section: vscode.WorkspaceConfiguration = vscode.workspace.getConfiguration( config.CONFIG_SECTION_NAME );
        let oldExecutable = this.executable;
        if( section )
        {
            // Load implementation
            let getConfig = function<T>(key:string, defaultValue:T, callback: ( value:T, userDefined:boolean) => void )
            {
                let value:T = defaultValue;
                let userDefined: boolean = false;
                let inspect = section.inspect<T>( key );
                if( inspect )
                {
                    if( inspect.workspaceValue )
                    {
                        value       = inspect.workspaceValue;
                        userDefined = true;
                    }
                    else if( inspect.globalValue )
                    {
                        value = inspect.globalValue;
                    }
                }
                callback( value, userDefined );
            };
            // Get configurations
            getConfig<boolean>( config.KEY_ENABLE_VARIDATE, false, (v, user) =>{
                this.validationEnabled = v;
            });
            getConfig<boolean>( config.KEY_ENABLE_REALTIME_VARIDATE, false, (v, user) =>{
                this.realtimeValidationEnabled = v;
            });
            getConfig<number>( config.KEY_REALTIME_VARIDATE_DELAY, config.DEFAULT_VARIDATE_DELAY, (v, user) =>{
                if( v < 16 )
                {
                    this.realtimeValidationDelay = config.DEFAULT_VARIDATE_DELAY;
                    section.update( config.KEY_REALTIME_VARIDATE_DELAY, config.DEFAULT_VARIDATE_DELAY, true );
                    vscode.window.showWarningMessage( "KSP Configuration: " + config.KEY_REALTIME_VARIDATE_DELAY + ": too short or negative. Reset default time." );
                }
                else
                {
                    this.realtimeValidationDelay = v;
                }
            });
            // ~Get configurations

            if( this.pauseValidation )
            {
                this.pauseValidation = oldExecutable === this.executable;
            }
            this.doDispose( this.onChangedListener );
            this.doDispose( this.onSaveListener );
            this.diagnosticCollection.clear();

            if( !this.validationEnabled )
            {
                return;
            }
            this.onSaveListener = vscode.workspace.onDidSaveTextDocument( (e) => {
                let key   = e.uri.toString();
                let delay = this.delayers[ key ];
                if( delay )
                {
                    delay.cancel();
                }
                this.realtimeTrigger = false;
                this.triggerValidate( e );
            }, this );

            this.onChangedListener = vscode.workspace.onDidChangeTextDocument( (e) => {
                this.realtimeTrigger = true;
                this.triggerValidate( e.document );
            });
        }
    }

    /**
     *
     */
    private untrustValidationExecutable()
    {
        this.workspaceStore.update( CHECKED_EXECUTABLE_PATH, undefined );
        vscode.commands.executeCommand( 'setContext', COMMAND_UNTRUST_VALIDATION_EXECUTABLE_CONTEXT, false );
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
                delayer              = new ThrottledDelayer<void>( 0 );
                this.delayers[ key ] = delayer;
            }

            let delay = this.realtimeTrigger ? this.realtimeValidationDelay : 0;
            if( this.realtimeValidationEnabled )
            {
                delayer.defaultDelay = delay;
            }
            // Exec syntax parser
            delayer.trigger( () => this.doValidate( textDocument ) );
        };
        trigger();
    }

    /**
     * Execute syntax parser program
     */
    private doValidate(textDocument: vscode.TextDocument): Promise<void>
    {
        return new Promise<void>( (resolve, reject) =>
        {
            let exec = "java";
            let diagnostics: vscode.Diagnostic[] = [];

            let processLine = (lineText: string) =>
            {
                let msg: string[] = lineText.split( PARSER_MESSAGE_DELIMITER );
                if( msg.length > 0 )
                {
                    let line    = Number.parseFloat( msg[ 0 ] ) - 1; // zero origin
                    let message = msg[ 1 ];
                    let diagnostic: vscode.Diagnostic = new vscode.Diagnostic(
                        new vscode.Range( line, 0, line, Number.MAX_VALUE ),
                        message
                    );
                    diagnostics.push( diagnostic );
                }
            }; //~ processLine
            let processLineStdErr = (lineText: string) =>
            {
                // net.rkoubou.kspparser.javacc.generated.TokenMgrError: Lexical error at line <number>,
                let regex: RegExp = /.*?TokenMgrError\: Lexical error at line (\d+)/;
                let matches = lineText.match( regex );
                if( matches )
                {
                    let message = "FATAL Lexical error";
                    let line = Number.parseInt( matches[ 2 ] );
                    let diagnostic: vscode.Diagnostic = new vscode.Diagnostic(
                        new vscode.Range( line, 0, line, Number.MAX_VALUE ),
                        message
                    );
                    vscode.window.showErrorMessage( "KSPSyntaxParser.jar FATAL : Lexical error. Check your script again." );
                }
            }

            let thisExtention    = vscode.extensions.getExtension( "R-Koubou.kontakt-script-langage" );
            let thisExtentionDir = thisExtention.extensionPath;
            let options       = vscode.workspace.rootPath ? { cwd: vscode.workspace.rootPath } : undefined;
            let args: string[] = [];
            let src = textDocument.fileName;
            let tmpFile = undefined;

            if( this.realtimeValidationEnabled )
            {
                tmpFile = tmp.fileSync();
                fs.writeFileSync( tmpFile.name, textDocument.getText() );
                src = tmpFile.name;
            }

            // java -Dkspparser.datadir=path/to/data -jar kspsyntaxparser.jar <document.fileName>
            args.push( "-Dkspparser.datadir=" + thisExtentionDir + "/kspparser/data/lang/message" )
            args.push( "-jar" );
            args.push( thisExtentionDir + "/kspparser/kspsyntaxparser.jar" );
            args.push( src );

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
                    // handling stdout
                    childProcess.stderr.on( 'data', (data: Buffer) =>
                    {
                        processLineStdErr( data.toString() );
                    });
                    // process finished
                    childProcess.stdout.on( 'end', () =>
                    {
                        if( tmpFile )
                        {
                            tmpFile.removeCallback();
                            tmpFile = undefined;
                        }
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
