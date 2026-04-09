import * as vscode from 'vscode';
import * as Configuration from './configurations';
import { LanguageClient } from 'vscode-languageclient/node';
import { OutputChannel } from './constants';

import * as LspClient from './lsp-client';
import * as Command from './command';
import * as Obfuscator from './obfuscator';
import { showWhatsNewIfUpdated } from './whats-new-check';

let client: LanguageClient | null | undefined = null;

export async function activate(context: vscode.ExtensionContext) {

    if (context.extensionMode === vscode.ExtensionMode.Development) {
        OutputChannel.show();
    }

    OutputChannel.appendLine('KSP extension activating');

    await showWhatsNewIfUpdated(context);

    client = await LspClient.startLspClient(context);

    if (!client) {
        OutputChannel.appendLine('Failed to create KSP LSP client');
        return;
    }

    registerConfigValueSubscriptions(context);
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

function registerConfigValueSubscriptions(context: vscode.ExtensionContext) {
    const configChangeDisposable = vscode.workspace.onDidChangeConfiguration(async (event) => {
        //----------------------------------------------------------------------
        // Changed prefer snippet insertion configuration
        //----------------------------------------------------------------------
        const preferSnippetConfigKey = Configuration.getConfigName(Configuration.CONFIG_COMPLETION_PREFER_SNIPPET_INSERTION);
        if (event.affectsConfiguration(preferSnippetConfigKey)) {
            OutputChannel.appendLine(`Configuration changed: ${preferSnippetConfigKey}`);
            await handlePreferSnippetInsertionConfigChange(context);
        }
    });

    context.subscriptions.push(configChangeDisposable);
}

async function handlePreferSnippetInsertionConfigChange(context: vscode.ExtensionContext) {
    if (!client) {
        return;
    }
    await handleLspServerRestart(context);
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
