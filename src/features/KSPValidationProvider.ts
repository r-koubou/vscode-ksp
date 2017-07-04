/* =========================================================================

    KSPValidationProvider.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

// Implemented based on Part of PHP Validation Provider implementation. (validationProvider.ts)

import * as vscode          from 'vscode';
import * as cp              from 'child_process';
import * as tmp             from 'tmp';
import * as fs              from 'fs';
import { ThrottledDelayer } from './libs/async';
import * as config          from './KSPConfigurationConstants';

const CHECKED_EXECUTABLE_PATH               = 'ksp.validate.checkedExecutablePath';
const PARSER_MESSAGE_DELIMITER: string      = "\t";
const REGEX_PARSER_MESSAGE_NEWLINE: RegExp  = /[\r]?\n/;
const COMMAND_UNTRUST_VALIDATION_EXECUTABLE = 'ksp.untrustValidationExecutable'

export class KSPValidationProvider
{
    private static REGEX_VARIABLE_SYMBOL: RegExp = /[\$|%|\~|\?|@|!]/;
    private static REGEX_TOKEN_MGR_ERROR: RegExp = /.*?TokenMgrError\: Lexical error at line (\d+)/;
    private static REGEX_PARSE_EXCEPTION: RegExp = /.*?ParseException\:.*?at line (\d+)/;

    private validationEnabled: boolean          = false;
    private realtimeValidationEnabled: boolean  = false;
    private realtimeValidationDelay:number      = config.DEFAULT_VALIDATE_DELAY;

    private executable: string                  = config.DEFAULT_JAVA_LOCATION;
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
            initConfig<boolean>( config.KEY_ENABLE_VALIDATE,            config.DEFAULT_ENABLE_VALIDATE );
            initConfig<boolean>( config.KEY_ENABLE_REALTIME_VALIDATE,   config.DEFAULT_REALTIME_VALIDATE );
            initConfig<number>(  config.KEY_REALTIME_VALIDATE_DELAY,    config.DEFAULT_VALIDATE_DELAY );
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
            getConfig<boolean>( config.KEY_ENABLE_VALIDATE, false, (v, user) =>{
                this.validationEnabled = v;
            });
            getConfig<boolean>( config.KEY_ENABLE_REALTIME_VALIDATE, false, (v, user) =>{
                this.realtimeValidationEnabled = v;
            });
            getConfig<number>( config.KEY_REALTIME_VALIDATE_DELAY, config.DEFAULT_VALIDATE_DELAY, (v, user) =>{
                if( v < 16 )
                {
                    this.realtimeValidationDelay = config.DEFAULT_VALIDATE_DELAY;
                    section.update( config.KEY_REALTIME_VALIDATE_DELAY, config.DEFAULT_VALIDATE_DELAY, true );
                    vscode.window.showWarningMessage( "KSP Configuration: " + config.KEY_REALTIME_VALIDATE_DELAY + ": too short or negative. Reset default time." );
                }
                else
                {
                    this.realtimeValidationDelay = v;
                }
            });
            getConfig<string>( config.KEY_JAVA_LOCATION, config.DEFAULT_JAVA_LOCATION, (v, user) =>{
                this.executable = v;
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
     * Handling for validation
     */
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
    private doValidate( textDocument: vscode.TextDocument ): Promise<void>
    {
        return new Promise<void>( (resolve, reject) =>
        {
            let exec = this.executable;
            let diagnostics: vscode.Diagnostic[] = [];

            let processLine = (lineText: string) =>
            {
                let msg: string[] = lineText.split( PARSER_MESSAGE_DELIMITER );
                if( msg.length >= 3 )
                {
                    let level   = msg[ 0 ].toUpperCase();
                    let line    = Number.parseInt( msg[ 1 ] ) - 1; // zero origin
                    let message = "[KSP] " + msg[ 2 ];
                    let range   = new vscode.Range( line, 0, line, Number.MAX_VALUE );
                    let diagnostic: vscode.Diagnostic = new vscode.Diagnostic( range, message );

                    if( level === "ERROR" )
                    {
                        diagnostic.severity = vscode.DiagnosticSeverity.Error;
                    }
                    else if( level === "WARNING" )
                    {
                        diagnostic.severity = vscode.DiagnosticSeverity.Warning;
                    }
                    else if( level === "INFO" || level === "DEBUG" )
                    {
                        diagnostic.severity = vscode.DiagnosticSeverity.Information;
                    }
                    else
                    {
                        diagnostic.severity = vscode.DiagnosticSeverity.Error;
                    }
                    diagnostics.push( diagnostic );
                }
            }; //~ processLine
            let processLineStdErr = (lineText: string) =>
            {
                // net.rkoubou.kspparser.javacc.generated.TokenMgrError: Lexical error at line <number>,
                let matches = lineText.match( KSPValidationProvider.REGEX_TOKEN_MGR_ERROR );
                let line: number = 0;
                if( !matches )
                {
                    matches = lineText.match( KSPValidationProvider.REGEX_PARSE_EXCEPTION );
                }
                if( matches )
                {
                    let message = "[KSP Parser] FATAL : Check your script carefully again.";
                    let line = Number.parseInt( matches[ 1 ] ) - 1; // zero origin
                    let diagnostic: vscode.Diagnostic = new vscode.Diagnostic(
                        new vscode.Range( line, 0, line, Number.MAX_VALUE ),
                        message
                    );
                    diagnostics.push( diagnostic );
                }
            }

            let thisExtention       = vscode.extensions.getExtension( "R-Koubou.kontakt-script-langage" );
            let thisExtentionDir    = thisExtention.extensionPath;
            let options             = vscode.workspace.rootPath ? { cwd: vscode.workspace.rootPath } : undefined;
            let args: string[]      = [];
            let src                 = textDocument.fileName;
            let tmpFile             = undefined;

            if( this.realtimeValidationEnabled )
            {
                tmpFile = tmp.fileSync();
                fs.writeFileSync( tmpFile.name, textDocument.getText() );
                src = tmpFile.name;
            }

            // java -Dkspparser.stdout.encoding=UTF-8 -Dkspparser.datadir=path/to/data -jar kspsyntaxparser.jar <document.fileName>
            args.push( "-Dkspparser.stdout.encoding=UTF-8" )
            args.push( "-Dkspparser.datadir=" + thisExtentionDir + "/kspparser/data" )
// launch en-US mode
//            args.push( "-Duser.language=en" );
//            args.push( "-Duser.country=US" );
            args.push( "-jar" );
            args.push( thisExtentionDir + "/kspparser/kspsyntaxparser.jar" );
            args.push( src );

            try
            {
                let childProcess = cp.spawn( exec, args, undefined );

                childProcess.on( 'error', (error: Error) =>
                {
                    if( tmpFile )
                    {
                        tmpFile.removeCallback();
                        tmpFile = undefined;
                    }

                    if( this.pauseValidation )
                    {
                        resolve();
                        return;
                    }
                    this.showFatal( 'Command "java" not found' );
                    this.pauseValidation = true;
                    resolve();
                });

                if( childProcess.pid )
                {
                    // handling stdout
                    childProcess.stdout.on( 'data', (data: Buffer) =>
                    {
                        //console.log( data.toString() );
                        data.toString().split( REGEX_PARSER_MESSAGE_NEWLINE ).forEach( x=>{
                            processLine( x );
                        });
                    });
                    // handling stderr
                    childProcess.stderr.on( 'data', (data: Buffer) =>
                    {
                        //console.log( data.toString() );
                        data.toString().split( REGEX_PARSER_MESSAGE_NEWLINE ).forEach( x=>{
                            processLineStdErr( x );
                        });
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
                this.showException( e, exec );
            }
        });
    }

    private showException( error: any, executable: string ): void
    {
        let message: string = "KSP Syntax Parser: FATAL ERROR";
        if( error.message )
        {
            message = error.message;
        }
        this.showFatal( message );
    }

    private showInfo( message: string ): void
    {
        vscode.window.showInformationMessage( message );
    }

    private showWarn( message: string ): void
    {
        vscode.window.showWarningMessage( message );
    }

    private showFatal( message: string ): void
    {
        vscode.window.showErrorMessage( message );
    }
}
