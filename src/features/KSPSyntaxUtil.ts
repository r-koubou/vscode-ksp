/* =========================================================================

    KSPSyntaxUtil.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import vscode = require( 'vscode' );

import * as cp                      from 'child_process';
import * as config                  from './KSPConfigurationConstants';
import { KSPConfigurationManager}   from './KSPConfigurationManager';
import { KSPCompileBuilder}         from './KSPCompileBuilder';

export const BASIC_KEYWORDS : string[] = [
    "on",
    "end",
    "function",
    "declare",
    "const",
    "polyphonic",
    "if",
    "else",
    "select",
    "case",
    "to",
    "while",
    "mod",
    "and",
    "or",
    "not",
    ".and.",
    ".or.",
    ".not.",
    "call",
]

export const REGEXP_DECIMAL     : RegExp = /\b(0|[1-9][0-9]*)/;
export const REGEXP_HEXADECIMAL : RegExp = /\b9[0-9a-fA-F]h/;
export const REGEXP_REAL        : RegExp = /\b(0|[1-9][0-9]*)\.[0-9]*/;
export const REGEXP_STRING      : RegExp = /"[^"]+"/;

export default class KSPSyntaxUtil
{
    private constructor(){}

    static matchKeyword( text: string ) : boolean
    {
        return BASIC_KEYWORDS.indexOf( text ) != -1;
    }

    static matchDecimal( text: string ) : boolean
    {
        return text.match( REGEXP_DECIMAL ) != null;
    }

    static matchHexadecimal( text: string ) : boolean
    {
        return text.match( REGEXP_HEXADECIMAL ) != null;
    }

    static matchReal( text: string ) : boolean
    {
        return text.match( REGEXP_REAL ) != null;
    }

    static matchString( text: string ) : boolean
    {
        return text.match( REGEXP_STRING ) != null;
    }

    static matchLiteral( text: string ) : boolean
    {
        return KSPSyntaxUtil.matchDecimal( text ) ||
               KSPSyntaxUtil.matchHexadecimal( text ) ||
               KSPSyntaxUtil.matchReal( text ) ||
               KSPSyntaxUtil.matchString( text );
    }

}

/**
 * Execute KSPSyntaxParser program
 */
export class KSPSyntaxParserExecutor
{
    private _onError:(txt:string)=>void = undefined;
    private _onException:(e:Error)=>void = undefined;
    private _onStdout:(txt:string)=>void = undefined;
    private _onStderr:(txt:string)=>void = undefined;
    private _onEnd:()=>void = undefined;
    private _onExit:(exitCode:number)=>void = undefined;

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

    /**
     * Execute KSP syntax parser program
     */
    public execSyntaxParser( args:string[] ) : void
    {
        try
        {
            let exec         = KSPConfigurationManager.getConfig<string>( config.KEY_JAVA_LOCATION, config.DEFAULT_JAVA_LOCATION );
            let childProcess = cp.spawn( exec, args, undefined );

            childProcess.on( 'error', (error: Error) =>
            {
                vscode.window.showErrorMessage( 'Command "java" not found' );
                if( this._onError )
                {
                    this._onError( 'Command "java" not found' );
                }
            });

            if( childProcess.pid )
            {
                // handling stdout
                childProcess.stdout.on( 'data', (data: Buffer) =>
                {
                    if( this._onStdout )
                    {
                        this._onStdout( data.toString() );
                    }
                });
                // handling stderr
                childProcess.stderr.on( 'data', (data: Buffer) =>
                {
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
                    if( this._onEnd )
                    {
                        this._onEnd();
                    }
                });
            }
        }
        catch( e )
        {
            if( this._onException )
            {
                this._onException( e );
            }
        }
    }
}
