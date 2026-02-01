import * as vscode from 'vscode';
import { LanguageClient } from 'vscode-languageclient/node';

import { OutputChannel } from './constants';
import * as LspClient from './lsp_client';
import * as Command from './command';
import * as Obfuscator from './obfuscator';

let client: LanguageClient | null | undefined = null;

export async function activate(context: vscode.ExtensionContext) {

    // Announcement message for the official release of v1.0.0
    vscode.window.showWarningMessage(
        '[KSP v1.0.0-Preview] This extension is deprecated. Please read the README for details.',
        { modal: false }
    );

    const uri = vscode.Uri.joinPath(context.extensionUri, 'README.md');
    try {
        vscode.commands.executeCommand('markdown.showPreview', uri);
    } catch {
        const doc = await vscode.workspace.openTextDocument(uri);
        vscode.window.showTextDocument(doc, { preview: false });
    }
    // ~Announcement message for the official release of v1.0.0

    if (context.extensionMode === vscode.ExtensionMode.Development) {
        OutputChannel.show();
    }

    OutputChannel.appendLine('KSP extension activating');

    client = await LspClient.startLspClient(context);

    if (!client) {
        OutputChannel.appendLine('Failed to create KSP LSP client');
        return;
    }

    registerExtensionCommands(context, client);
    OutputChannel.appendLine('KSP extension activated');
}

export function deactivate(): Thenable<void> | undefined {
    return Promise.resolve();
}

function checkClientInitialized() {
    if (!client) {
        OutputChannel.appendLine('LSP client is not initialized.');
        return;
    }
}

function registerExtensionCommands(context: vscode.ExtensionContext, client: LanguageClient) {
    Command.registerCommand('ksp.lsp.restart', context, async () => {
        await handleLspServerRestart(context);
    });

    Command.registerCommand('ksp.obfuscate', context, async () => {
        await handleObfuscateCommand(context);
    });
}

async function handleLspServerRestart(context: vscode.ExtensionContext) {

    checkClientInitialized();

    await vscode.window.withProgress({
        location: vscode.ProgressLocation.Notification,
        title: 'Restarting KSP LSP server',
        cancellable: false
    }, async (progress) => {
        try {
            progress.report({ message: 'Stopping KSP LSP server...' });
            OutputChannel.appendLine('Restarting KSP LSP server');
            await LspClient.stopLspClient(context, client);

            progress.report({ message: 'Starting KSP LSP server...' });
            client = null;
            client = await LspClient.startLspClient(context);
            if (!client) {
                OutputChannel.appendLine('Failed to create KSP LSP client');
                return;
            }
        } catch (error) {
            OutputChannel.appendLine(`Error restarting KSP LSP server: ${error}`);
        }
    });
}

async function handleObfuscateCommand(context: vscode.ExtensionContext) {
    checkClientInitialized();
    await Obfuscator.obfuscate(context, client!);
}
