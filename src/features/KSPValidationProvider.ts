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
import { KSPConfigurationManager} from './KSPConfigurationManager';
import * as kspconst        from './KSPExtensionConstants';

const PARSER_MESSAGE_DELIMITER: string      = "\t";
const REGEX_PARSER_MESSAGE_NEWLINE: RegExp  = /[\r]?\n/;

export class KSPValidationProvider
{
    private static REGEX_VARIABLE_SYMBOL: RegExp = /[\$|%|\~|\?|@|!]/;
    private static REGEX_TOKEN_MGR_ERROR: RegExp = /.*?TokenMgrError\: Lexical error at line (\d+)/;
    private static REGEX_PARSE_EXCEPTION: RegExp = /.*?ParseException\:.*?at line (\d+)/;

    private validationEnabled: boolean          = config.DEFAULT_ENABLE_VALIDATE;
    private realtimeValidationEnabled: boolean  = config.DEFAULT_REALTIME_VALIDATE;
    private validateParseSyntaxOnly:boolean     = config.DEFAULT_PARSE_SYNTAX_ONLY;
    private validateParseStrict:boolean         = config.DEFAULT_PARSE_STRICT;
    private validateParseUnused:boolean         = config.DEFAULT_PARSE_UNUSED;
    private realtimeValidationDelay:number      = config.DEFAULT_VALIDATE_DELAY;

    private executable: string                  = config.DEFAULT_JAVA_LOCATION;
    private pauseValidation: boolean            = false;
    private realtimeTrigger: boolean            = false;

    private onSaveListener: vscode.Disposable;
    private onChangedListener: vscode.Disposable;
    private diagnosticsList: { [key: string]: vscode.DiagnosticCollection };
    private delayersList:    { [key: string]: ThrottledDelayer<void> };

    /**
     * ctor
     */
    constructor( private workspaceStore: vscode.Memento )
    {
        this.diagnosticsList = Object.create( null );
        this.delayersList    = Object.create( null );
    }

    /**
     * provider activated
     */
    public activate( subscriptions: vscode.Disposable[] )
    {
        this.initConfiguration();
        this.loadConfiguration();
        vscode.workspace.onDidChangeConfiguration( this.loadConfiguration, this, subscriptions );
        vscode.workspace.onDidOpenTextDocument( this.triggerValidate, this, subscriptions );
        vscode.workspace.onDidCloseTextDocument( (textDocument) =>
        {
            this.clearDiagnosticCollection( textDocument );
            delete this.diagnosticsList[ textDocument.uri.toString() ];
            delete this.delayersList[ textDocument.uri.toString() ];
        }, null, subscriptions );
    }

    /**
     * Get DiagnosticCollection from active TextDocument
     */
    private getDiagnosticCollection( textDocument: vscode.TextDocument ): vscode.DiagnosticCollection
    {
        if( !textDocument )
        {
            throw "textDocument is null";
        }
        let key = textDocument.uri.toString();
        var p: vscode.DiagnosticCollection = this.diagnosticsList[ key ];
        if( p )
        {
            return p;
        }
        p = vscode.languages.createDiagnosticCollection();
        this.diagnosticsList[ key ] = p;
        return p;
    }

    /**
     * Clear diagnosticCollection
     */
    private clearDiagnosticCollection( textDocument: vscode.TextDocument )
    {
        if( !textDocument )
        {
            return;
        }
        let key = textDocument.uri.toString();
        let p   = this.diagnosticsList[ key ];
        if( p )
        {
            p.clear();
        }
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
            initConfig<boolean>( config.KEY_PARSE_SYNTAX_ONLY,          config.DEFAULT_PARSE_SYNTAX_ONLY );
            initConfig<boolean>( config.KEY_PARSE_STRICT,               config.DEFAULT_PARSE_STRICT );
            initConfig<boolean>( config.KEY_PARSE_UNUSED,               config.DEFAULT_PARSE_UNUSED );
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
            // Get configurations
            KSPConfigurationManager.getConfig<boolean>( config.KEY_ENABLE_VALIDATE, config.DEFAULT_ENABLE_VALIDATE, (v, user) =>{
                this.validationEnabled = v;
            });
            KSPConfigurationManager.getConfig<string>( config.KEY_JAVA_LOCATION, config.DEFAULT_JAVA_LOCATION, (v, user) =>{
                this.executable = v;
            });
            KSPConfigurationManager.getConfig<boolean>( config.KEY_ENABLE_REALTIME_VALIDATE, config.DEFAULT_REALTIME_VALIDATE, (v, user) =>{
                this.realtimeValidationEnabled = v;
            });
            KSPConfigurationManager.getConfig<number>( config.KEY_REALTIME_VALIDATE_DELAY, config.DEFAULT_VALIDATE_DELAY, (v, user) =>{
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
            KSPConfigurationManager.getConfig<boolean>( config.KEY_PARSE_SYNTAX_ONLY, config.DEFAULT_PARSE_SYNTAX_ONLY, (v, user) =>{
                this.validateParseSyntaxOnly = v;
            });
            KSPConfigurationManager.getConfig<boolean>( config.KEY_PARSE_STRICT, config.DEFAULT_PARSE_STRICT, (v, user) =>{
                this.validateParseStrict = v;
            });
            KSPConfigurationManager.getConfig<boolean>( config.KEY_PARSE_UNUSED, config.DEFAULT_PARSE_UNUSED, (v, user) =>{
                this.validateParseUnused = v;
            });
            // ~Get configurations

            if( this.pauseValidation )
            {
                this.pauseValidation = oldExecutable === this.executable;
            }
            this.doDispose( this.onChangedListener );
            this.doDispose( this.onSaveListener );

            if( !this.validationEnabled )
            {
                return;
            }
            this.onSaveListener = vscode.workspace.onDidSaveTextDocument( (e) => {
                let key   = e.uri.toString();
                let delay = this.delayersList[ key ];
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

            if( this.validationEnabled )
            {
                let documents: vscode.TextDocument[] = vscode.workspace.textDocuments;
                if( documents )
                {
                    documents.forEach( (doc:vscode.TextDocument)=>{
                        this.triggerValidate( doc );
                    });
                }
            }

        }
    }

    /**
     * Handling for validation
     */
    private triggerValidate( textDocument: vscode.TextDocument ): void
    {
        if( !textDocument || textDocument.languageId !== "ksp" || !this.validationEnabled )
        {
            return;
        }
        let trigger = () =>
        {
            let key = textDocument.uri.toString();
            let delayer = this.delayersList[ key ];
            if( !delayer )
            {
                delayer              = new ThrottledDelayer<void>( 0 );
                this.delayersList[ key ] = delayer;
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
        if( textDocument.isClosed )
        {
            return;
        }
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

            let thisExtention        = vscode.extensions.getExtension( kspconst.EXTENSION_ID );
            let thisExtentionDir     = thisExtention.extensionPath;
            let options              = vscode.workspace.rootPath ? { cwd: vscode.workspace.rootPath } : undefined;
            let args: string[]       = [];
            let src                  = textDocument.fileName;
            let tmpFile              = undefined;
            let diagnosticCollection = this.getDiagnosticCollection( textDocument );

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
            args.push( thisExtentionDir + "/kspparser/KSPSyntaxParser.jar" );
            if( this.validateParseSyntaxOnly )
            {
                args.push( "--parseonly" );
            }
            if( this.validateParseStrict )
            {
                args.push( "--strict" );
            }
            if( this.validateParseUnused )
            {
                args.push( "--unused" );
            }
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
                        resolve();
                    });
                    // handling stderr
                    childProcess.stderr.on( 'data', (data: Buffer) =>
                    {
                        //console.log( data.toString() );
                        data.toString().split( REGEX_PARSER_MESSAGE_NEWLINE ).forEach( x=>{
                            processLineStdErr( x );
                        });
                        resolve();
                    });
                    // process finished
                    childProcess.stdout.on( 'end', () =>
                    {
                        if( tmpFile )
                        {
                            tmpFile.removeCallback();
                            tmpFile = undefined;
                        }
                        if( diagnosticCollection )
                        {
                            diagnosticCollection.set( textDocument.uri, diagnostics );
                        }
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
