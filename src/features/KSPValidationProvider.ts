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
import { KSPConfigurationManager}   from './KSPConfigurationManager';
import { KSPCompileExecutor }       from './KSPCompileExecutor';
import { KSPCompileBuilder}         from './KSPCompileBuilder';


const PARSER_MESSAGE_DELIMITER: string      = "\t";
const REGEX_PARSER_MESSAGE_NEWLINE: RegExp  = /[\r]?\n/;

export class KSPValidationProvider
{
    private static REGEX_VARIABLE_SYMBOL: RegExp = /[\$|%|\~|\?|@|!]/;
    private static REGEX_TOKEN_MGR_ERROR: RegExp = /.*?TokenMgrError\: Lexical error at line (\d+)/;
    private static REGEX_PARSE_EXCEPTION: RegExp = /.*?ParseException\:.*?at line (\d+)/;

    private validationEnabled: boolean          = config.DEFAULT_ENABLE_VALIDATE;
    private realtimeValidationEnabled: boolean  = config.DEFAULT_REALTIME_VALIDATE;
    private realtimeValidationDelay:number      = config.DEFAULT_VALIDATE_DELAY;

    private executable: string                  = config.DEFAULT_JAVA_LOCATION;
    private pauseValidation: boolean            = false;
    private realtimeTrigger: boolean            = false;

    private onSaveListener: vscode.Disposable;
    private onChangedListener: vscode.Disposable;
    private compilerList:    { [key: string]: KSPCompileExecutor };
    private delayersList:    { [key: string]: ThrottledDelayer<void> };

    /**
     * ctor
     */
    constructor( private workspaceStore: vscode.Memento )
    {
        this.compilerList = Object.create( null );
        this.delayersList = Object.create( null );
    }

    /**
     * provider activated
     */
    public activate( context:vscode.ExtensionContext )
    {
        const subscriptions: vscode.Disposable[] = context.subscriptions;

        this.initConfiguration();
        this.loadConfiguration();
        vscode.workspace.onDidChangeConfiguration( this.loadConfiguration, this, subscriptions );
        vscode.workspace.onDidOpenTextDocument( this.triggerValidate, this, subscriptions );
        vscode.workspace.onDidCloseTextDocument( (textDocument) =>
        {
            this.clearDiagnosticCollection( textDocument );
            delete this.compilerList[ textDocument.uri.toString() ];
            delete this.delayersList[ textDocument.uri.toString() ];
        }, null, subscriptions );
    }

    /**
     * Get DiagnosticCollection from active TextDocument
     */
    private getCompiler( textDocument: vscode.TextDocument ): KSPCompileExecutor
    {
        if( !textDocument )
        {
            throw "textDocument is null";
        }
        let key = textDocument.uri.toString();
        let p: KSPCompileExecutor = this.compilerList[ key ];
        if( p )
        {
            return p;
        }
        p = new KSPCompileExecutor();
        this.compilerList[ key ] = p;
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
        let p   = this.compilerList[ key ];
        if( p )
        {
            p.diagnosticCollection.clear();
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
            this.validationEnabled          = KSPConfigurationManager.getConfig<boolean>( config.KEY_ENABLE_VALIDATE, config.DEFAULT_ENABLE_VALIDATE );
            this.executable                 = KSPConfigurationManager.getConfig<string>( config.KEY_JAVA_LOCATION, config.DEFAULT_JAVA_LOCATION );
            this.realtimeValidationEnabled  = KSPConfigurationManager.getConfig<boolean>( config.KEY_ENABLE_REALTIME_VALIDATE, config.DEFAULT_REALTIME_VALIDATE );
            KSPConfigurationManager.getConfigComplex<number>( config.KEY_REALTIME_VALIDATE_DELAY, config.DEFAULT_VALIDATE_DELAY, (v, user) =>{
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
                delayer                     = new ThrottledDelayer<void>( 0 );
                this.delayersList[ key ]    = delayer;
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
            let src: string                     = textDocument.fileName;
            let compiler: KSPCompileExecutor    = this.getCompiler( textDocument );
            let argBuilder: KSPCompileBuilder   = new KSPCompileBuilder( src );

            compiler.onError = (text:string) => {
                if( this.pauseValidation )
                {
                    resolve();
                    return;
                }
                this.pauseValidation = true;
                resolve();
            };

            compiler.onStdout = (text:string) => {
                resolve();
            };

            compiler.onStderr = (text:string) => {
                resolve();
            };

            compiler.onEnd = () => {
                resolve();
            };

            compiler.execute( textDocument, argBuilder, this.realtimeValidationEnabled, true );
        });
    }
}
