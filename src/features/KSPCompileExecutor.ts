/* =========================================================================

    KSPCompileExecutor.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import vscode = require( 'vscode' );

import * as tmp                     from 'tmp';
import * as fs                      from 'fs';
import * as path                    from 'path';
import * as cp                      from 'child_process';
import { ThrottledDelayer }         from './libs/async';
import * as config                  from './KSPConfigurationConstants';
import { KSPConfigurationManager }  from './KSPConfigurationManager';
import { KSPCompileBuilder }        from './KSPCompileBuilder';

const REGEX_PARSER_MESSAGE_NEWLINE: RegExp  = /[\r]?\n/;
const PARSER_MESSAGE_DELIMITER: string      = "\t";
const REGEX_TOKEN_MGR_ERROR: RegExp         = /.*?TokenMgrError\: Lexical error at line (\d+)/;
const REGEX_PARSE_EXCEPTION: RegExp         = /.*?ParseException\:.*?at line (\d+)/;

/**
 * Execute KSP Compile program
 */
export class KSPCompileExecutor implements vscode.Disposable
{

    private static pool: { [key: string]: KSPCompileExecutor } = {};

    private _onError:(txt:string)=>void = undefined;
    private _onException:(e:Error)=>void = undefined;
    private _onStdout:(txt:string)=>void = undefined;
    private _onStderr:(txt:string)=>void = undefined;
    private _onEnd:()=>void = undefined;
    private _onExit:(exitCode:number)=>void = undefined;

    private running: boolean = false;
    private tempFile;
    private _diagnosticCollection: vscode.DiagnosticCollection = vscode.languages.createDiagnosticCollection();
    private diagnostics: vscode.Diagnostic[] = [];

    private _delayer: ThrottledDelayer<void> = new ThrottledDelayer<void>( 0 );

    /**
     * ctor.
     */
    private constructor()
    {
        this._delayer.defaultDelay = config.DEFAULT_VALIDATE_DELAY;
    }

    /**
     * Create the unique instance per document
     */
    static getCompiler( document: vscode.TextDocument )
    {
        if( !document )
        {
            throw "textDocument is null";
        }
        let key = document.fileName;
        let p: KSPCompileExecutor = KSPCompileExecutor.pool[ key ];
        if( p )
        {
            return p;
        }
        p = new KSPCompileExecutor();
        KSPCompileExecutor.pool[ key ] = p;
        return p;
    }

    /**
     * Remove all callback events
     */
    protected unsetllEvents(): void
    {
        this.OnEnd          = undefined;
        this.OnError        = undefined;
        this.OnException    = undefined;
        this.OnExit         = undefined;
        this.OnStderr       = undefined;
        this.OnStdout       = undefined;
    }

    /**
     * Clear diagnostics from problems view
     */
    protected clearaDiagnostics(): void
    {
        this.DiagnosticCollection.clear();
    }

    /**
     * Must call before when parser program do run
     */
    public init(): KSPCompileExecutor
    {
        this.unsetllEvents();
        return this;
    }

    /**
     * Dispose resourves
     */
    public dispose(): void
    {
        this.clearaDiagnostics();
        this.unsetllEvents();
    }

    /**
     * Dispose resourves
     */
    public static dispose( document: vscode.TextDocument ): void
    {
        const key = document.fileName;
        const p   = this.pool[ key ];
        if( p )
        {
            p.dispose();
            delete this.pool[ key ];
        }
    }

    //--------------------------------------------------------------------------
    // setter for callbacks
    //--------------------------------------------------------------------------
    set OnError( error:(txt:string)=>void )
    {
        this._onError = error;
    }
    set OnException( exception:(e:Error)=>void )
    {
        this._onException = exception;
    }
    set OnStdout( stdout:(txt:string)=>void )
    {
        this._onStdout = stdout;
    }
    set OnStderr( stderr:(txt:string)=>void )
    {
        this._onStderr = stderr;
    }
    set OnEnd( end:()=>void )
    {
        this._onEnd = end;
    }
    set OnExit( exit:(exitCode:number)=>void )
    {
        this._onExit = exit;
    }

    get DiagnosticCollection(): vscode.DiagnosticCollection
    {
        return this._diagnosticCollection;
    }

    get Delayer(): ThrottledDelayer<void>
    {
        return this._delayer;
    }

    /**
     * Parse stdout/stderr for generate diagnostics
     */
    private parseStdOut( lineText:string ): void
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
            this.diagnostics.push( diagnostic );
        }
    }

    /**
     * Parse stderr for generate diagnostics
     */
    private parseStdErr( lineText:string ): void
    {
        // net.rkoubou.kspparser.javacc.generated.TokenMgrError: Lexical error at line <number>,
        let matches = lineText.match( REGEX_TOKEN_MGR_ERROR );
        let line: number = 0;
        if( !matches )
        {
            matches = lineText.match( REGEX_PARSE_EXCEPTION );
        }
        if( matches )
        {
            let message = "[KSP Parser] FATAL : Check your script carefully again.";
            let line = Number.parseInt( matches[ 1 ] ) - 1; // zero origin
            let diagnostic: vscode.Diagnostic = new vscode.Diagnostic(
                new vscode.Range( line, 0, line, Number.MAX_VALUE ),
                message
            );
            this.diagnostics.push( diagnostic );
        }
    }

    /**
     * remove previous tempfile
     */
    private removeTempfile()
    {
        if( this.tempFile )
        {
            this.tempFile.removeCallback();
            this.tempFile = undefined;
        }
    }

    /**
     * Execute KSP syntax parser program (async)
     */
    private executeImpl( document:vscode.TextDocument, argBuilder:KSPCompileBuilder, useTmpFile: Boolean = false, useDiagnostics: boolean = true ) : Promise<void>
    {
        return new Promise<void>( (resolve, reject ) => {

            if( !KSPConfigurationManager.getConfig<boolean>( config.KEY_ENABLE_VALIDATE, config.DEFAULT_ENABLE_VALIDATE ) )
            {
                vscode.window.showErrorMessage( 'KSP: Validate is disabled. See Preference of KSP' );
                this.running  = false;
                resolve();
                return;
            }

            this.diagnostics = [];

// launch en-US mode
            // argBuilder.forceUseEn_US = true;

            this.removeTempfile();

            if( useTmpFile )
            {
                this.tempFile = tmp.fileSync();
                fs.writeFileSync( this.tempFile.name, document.getText() );
                argBuilder.inputFile = this.tempFile.name;
            }

            let processFailed: boolean = false;

            try
            {
                let args: string[] = argBuilder.build();
                let exec         = KSPConfigurationManager.getConfig<string>( config.KEY_JAVA_LOCATION, config.DEFAULT_JAVA_LOCATION );
                let childProcess = cp.spawn( exec, args, undefined );

                childProcess.on( 'error', (error: Error) =>
                {
                    this.removeTempfile();
                    this._diagnosticCollection.set( document.uri, undefined );

                    vscode.window.showErrorMessage( 'KSP: Command "java" not found' );
                    if( this._onError )
                    {
                        this._onError( 'KSP: Command "java" not found' );
                    }
                    this.running = false;
                    resolve();
                });

                if( childProcess.pid )
                {
                    this.running  = true;
                    processFailed = false;

                    // handling stdout
                    childProcess.stdout.on( 'data', (data: Buffer) =>
                    {
                        if( useDiagnostics )
                        {
                            data.toString().split( REGEX_PARSER_MESSAGE_NEWLINE ).forEach( x=>{
                                this.parseStdOut( x );
                            });
                        }

                        if( this._onStdout )
                        {
                            this._onStdout( data.toString() );
                        }
                        resolve();
                    });
                    // handling stderr
                    childProcess.stderr.on( 'data', (data: Buffer) =>
                    {
                        if( useDiagnostics )
                        {
                            data.toString().split( REGEX_PARSER_MESSAGE_NEWLINE ).forEach( x=>{
                                this.parseStdErr( x );
                            });
                        }

                        if( this._onStderr )
                        {
                            this._onStderr( data.toString() );
                        }
                        resolve();
                    });
                    // process finished with exit code
                    childProcess.on( 'exit', (exitCode) =>
                    {
                        if( this._onExit )
                        {
                            this._onExit( exitCode );
                        }
                        resolve();
                    });
                    // process finished
                    childProcess.stdout.on( 'end', () =>
                    {
                        this.removeTempfile();

                        if( useDiagnostics )
                        {
                            if( !document.isClosed )
                            {
                                this.DiagnosticCollection.set( document.uri, this.diagnostics );
                            }
                            else
                            {
                                this.clearaDiagnostics();
                            }
                        }

                        if( this._onEnd )
                        {
                            this._onEnd();
                        }
                        this.running = false;
                        resolve();
                    });
                }
                else
                {
                    if( this.OnError )
                    {
                        this._onError( "childProcess is invalid" )
                    }
                    this.running  = false;
                    processFailed = true;
                }
            }
            catch( e )
            {
                this._diagnosticCollection.set( document.uri, undefined );
                if( this._onException )
                {
                    this._onException( e );
                }
                this.running = false;
                reject( e )
            }
            finally
            {
                if( processFailed )
                {
                    this.removeTempfile();
                }
            }
        });
    }

    /**
     * Execute KSP syntax parser program
     */
    public execute( document:vscode.TextDocument, argBuilder:KSPCompileBuilder, useTmpFile: Boolean = false, useDiagnostics: boolean = true ) : void
    {
        if( document.languageId !== "ksp" )
        {
            return;
        }

        if( this.running || document.isClosed )
        {
            return;
        }

        if( !useTmpFile && ( document.isUntitled || document.isDirty ) )
        {
            let baseName: string = path.basename( document.fileName );
            vscode.window.showErrorMessage( `KSP: ${baseName} - File is not saved.` );
            return;
        }

        this._delayer.trigger( () => this.executeImpl( document, argBuilder, useTmpFile, useDiagnostics ) );
    }
}
