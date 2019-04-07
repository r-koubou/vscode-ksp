/* =========================================================================

    KSPValidationProvider.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

// Implemented based on Part of PHP Validation Provider implementation. (validationProvider.ts)

import * as vscode          from 'vscode';
import { ThrottledDelayer } from './libs/async';
import * as config          from './KSPConfigurationConstants';
import { KSPConfigurationManager}   from './KSPConfigurationManager';
import { KSPCompileExecutor }       from './KSPCompileExecutor';
import { KSPCompileBuilder}         from './KSPCompileBuilder';

export class KSPValidationProvider
{
    private validationEnabled: boolean          = config.DEFAULT_ENABLE_VALIDATE;
    private realtimeValidationEnabled: boolean  = config.DEFAULT_REALTIME_VALIDATE;
    private realtimeValidationDelay:number      = config.DEFAULT_VALIDATE_DELAY;

    private executable: string                  = config.DEFAULT_JAVA_LOCATION;
    private pauseValidation: boolean            = false;
    private realtimeTrigger: boolean            = false;

    private onSaveListener: vscode.Disposable;
    private onChangedListener: vscode.Disposable;

    /**
     * ctor
     */
    constructor( private workspaceStore: vscode.Memento )
    {
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
        vscode.workspace.onDidOpenTextDocument( (document) => {
            this.doValidate( document );
        }, null, subscriptions );
        vscode.workspace.onDidCloseTextDocument( (textDocument) =>
        {
             KSPCompileExecutor.dispose( textDocument );
        }, null, subscriptions );
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
                this.realtimeTrigger = false;
                this.doValidate( e );
            }, this );

            this.onChangedListener = vscode.workspace.onDidChangeTextDocument( (e) => {
                if( this.realtimeValidationEnabled )
                {
                    this.realtimeTrigger = true;
                    this.doValidate( e.document );
                }
            });

            if( this.validationEnabled )
            {
                let documents: vscode.TextDocument[] = vscode.workspace.textDocuments;
                if( documents )
                {
                    documents.forEach( (doc:vscode.TextDocument)=>{
                        this.doValidate( doc );
                    });
                }
            }

        }
    }

    /**
     * Execute syntax parser program
     */
    private doValidate( textDocument: vscode.TextDocument ): void//Promise<void>
    {
        if( !this.validationEnabled )
        {
            return;
        }

        //return new Promise<void>( (resolve, reject) =>
        {
            let src: string                     = textDocument.fileName;
            let compiler: KSPCompileExecutor    = KSPCompileExecutor.getCompiler( textDocument ).init();
            let argBuilder: KSPCompileBuilder   = new KSPCompileBuilder( src );
            let delayer: ThrottledDelayer<void> = compiler.Delayer;
            let delay = this.realtimeTrigger ? this.realtimeValidationDelay : 0;

            if( this.realtimeValidationEnabled )
            {
                delayer.defaultDelay = delay;
            }

            compiler.OnError = (text:string) => {
                if( this.pauseValidation )
                {
                    //resolve();
                    return;
                }
                this.pauseValidation = true;
                //resolve();
            };

            compiler.OnStdout = (text:string) => {
                //resolve();
            };

            compiler.OnStderr = (text:string) => {
                //resolve();
            };

            compiler.OnEnd = () => {
                //resolve();
            };

            compiler.execute( textDocument, argBuilder, this.realtimeValidationEnabled, true );
        }//);
    }
}
