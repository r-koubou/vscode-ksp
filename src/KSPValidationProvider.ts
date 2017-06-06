/* =========================================================================

    KSPValidationProvider.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

// Based on PHP Validation Provider implementation. (validationProvider.ts)

import * as vscode          from 'vscode';
import * as process         from 'child_process';
import { ThrottledDelayer } from './libs/async';


const CheckedExecutablePath = 'ksp.validate.checkedExecutablePath';

export class KSPValidationProvider
{
    private static MESSAGE_DELIMITER: string = "\t";

    private validationEnabled: boolean = true;
	private executable: string;
	private pauseValidation: boolean   = false;

    private diagnosticCollection: vscode.DiagnosticCollection;
    private delayers: { [key: string]: ThrottledDelayer<void> };

    /**
     * ctor
     */
	constructor( private workspaceStore: vscode.Memento )
    {}

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
            vscode.commands.registerCommand('ksp.untrustValidationExecutable',
            this.untrustValidationExecutable, this )
        );
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
    }

    /**
     * Do syntax parser program
     */
    private doValidate(textDocument: vscode.TextDocument): Promise<void>
    {
        return new Promise<void>( (resolve, reject) =>
        {
        });
    }

    private showError( error: any, executable: string ): void
    {
    }

}
