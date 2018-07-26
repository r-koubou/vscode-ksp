/* =========================================================================

    KSPCompileExecutor.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import vscode = require( 'vscode' );

import * as tmp                     from 'tmp';
import * as fs                      from 'fs';
import * as cp                      from 'child_process';
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
export class KSPCompileExecutor
{

    private _onError:(txt:string)=>void = undefined;
    private _onException:(e:Error)=>void = undefined;
    private _onStdout:(txt:string)=>void = undefined;
    private _onStderr:(txt:string)=>void = undefined;
    private _onEnd:()=>void = undefined;
    private _onExit:(exitCode:number)=>void = undefined;

    private document: vscode.TextDocument;
    private tempFile;
    private _diagnosticCollection: vscode.DiagnosticCollection = vscode.languages.createDiagnosticCollection();
    private diagnostics: vscode.Diagnostic[] = [];

    /**
     * ctor.
     */
    constructor()
    {
    }

    //--------------------------------------------------------------------------
    // setter for callbacks
    //--------------------------------------------------------------------------
    set onError( error:(txt:string)=>void )
    {
        this._onError = error;
    }
    set onException( exception:(e:Error)=>void )
    {
        this._onException = exception;
    }
    set onStdout( stdout:(txt:string)=>void )
    {
        this._onStdout = stdout;
    }
    set onStderr( stderr:(txt:string)=>void )
    {
        this._onStderr = stderr;
    }
    set onEnd( end:()=>void )
    {
        this._onEnd = end;
    }
    set onExit( exit:(exitCode:number)=>void )
    {
        this._onExit = exit;
    }

    get diagnosticCollection(): vscode.DiagnosticCollection
    {
        return this._diagnosticCollection;
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
     * Execute KSP syntax parser program
     */
    public execute( document:vscode.TextDocument, argBuilder:KSPCompileBuilder, useTmpFile: Boolean = false, useDiagnostics: boolean = true ) : void
    {
        if( !KSPConfigurationManager.getConfig<boolean>( config.KEY_ENABLE_VALIDATE, config.DEFAULT_ENABLE_VALIDATE ) )
        {
            vscode.window.showErrorMessage( 'KSP: Validate is disabled. See Preference of KSP' );
            return;
        }

        this.diagnostics = [];
        this.document    = document;

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
            });

            if( childProcess.pid )
            {
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
                });
                // process finished with exit code
                childProcess.on( 'exit', (exitCode) =>
                {
                    if( this._onExit )
                    {
                        this._onExit( exitCode );
                    }
                });
                // process finished
                childProcess.stdout.on( 'end', () =>
                {
                    this.removeTempfile();

                    if( useDiagnostics )
                    {
                        if( this._diagnosticCollection )
                        {
                            this._diagnosticCollection.set( document.uri, this.diagnostics );
                        }
                    }

                    if( this._onEnd )
                    {
                        this._onEnd();
                    }
                });
            }
            else
            {
                if( this.onError )
                {
                    this._onError( "childProcess is invalid" )
                }
            }
        }
        catch( e )
        {
            this._diagnosticCollection.set( document.uri, undefined );
            if( this._onException )
            {
                this._onException( e );
            }
        }
        finally
        {
            if( processFailed )
            {
                this.removeTempfile();
            }
        }
    }
}
