import * as vscode from 'vscode';
import { LanguageClient } from 'vscode-languageclient/node';

import { OutputChannel } from './constants';
import * as ObfuscateCommand from './commands/obfuscator';
import { startLspClient } from './lsp_client';

let client: LanguageClient | null | undefined = null;

export async function activate(context: vscode.ExtensionContext) {

    if (context.extensionMode === vscode.ExtensionMode.Development) {
        OutputChannel.show();
    }

    OutputChannel.appendLine('KSP extension activating');

    client = await startLspClient(context);

    if (!client) {
        OutputChannel.appendLine('Failed to create KSP LSP client');
        return;
    }

    context.subscriptions.push(client);
    registerExtensionCommands(context, client);
    OutputChannel.appendLine('KSP extension activated');
}

export function deactivate(): Thenable<void> | undefined {
    OutputChannel.appendLine('KSP LSP server stopping');
    return client?.stop().then(() => {
        client = null;
        OutputChannel.appendLine('KSP LSP server stopped');
    });
}

function registerExtensionCommands(context: vscode.ExtensionContext, client: LanguageClient) {
    ObfuscateCommand.registerCommand(context, client);
}
