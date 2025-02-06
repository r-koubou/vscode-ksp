import * as vscode from 'vscode';
import { LanguageClient, LanguageClientOptions, ServerOptions, TransportKind } from 'vscode-languageclient/node';

import { exec } from 'child_process';
import * as cp from 'child_process';

import * as constants from './constants';

let client: LanguageClient | null = null;

const outputChannel = vscode.window.createOutputChannel(constants.OutputChannelName, 'ksp');

export async function activate(context: vscode.ExtensionContext) {

    const result = await checkDotnetInstalled();

    if (!result) {
        outputChannel.appendLine('KSP LSP failed to activate by missing .NET runtime');
        return;
    }

    if (client) {
        return;
    }


    outputChannel.appendLine('KSP extension activating');

    const serverAssembly = context.asAbsolutePath('language_server/KSPCompiler.Apps.LSPServer.Embedded.dll');
    outputChannel.appendLine(`Server assembly path: ${serverAssembly}`);

    const serverOptions: ServerOptions = {
        run: { command: "dotnet", args: [serverAssembly], transport: TransportKind.stdio },
        debug: { command: "dotnet", args: [serverAssembly], transport: TransportKind.stdio }
    };

    const clientOptions: LanguageClientOptions = {
        documentSelector: [
            { scheme: 'file', language: 'ksp' },
            { scheme: 'untitled', language: 'ksp' },
        ]
    };

    outputChannel.appendLine('KSP LSP server creating');

    client = new LanguageClient(
        'ksp',
        'ksp',
        serverOptions,
        clientOptions
    );

    outputChannel.appendLine('KSP LSP server created');

    client.start().then(() => {
        outputChannel.appendLine('KSP LSP server started');
    }).catch((reason) => {
        outputChannel.appendLine('KSP LSP server failed to start');
        outputChannel.appendLine(reason);
    });

    context.subscriptions.push(client);

    outputChannel.appendLine('KSP extension activated');
}

export function deactivate(): Thenable<void> | undefined {
    outputChannel.appendLine('KSP extension deactivating');
    if (!client) {
        return undefined;
    }
    outputChannel.appendLine('KSP LSP server stopping');
    return client.stop().then(() => {
        client = null;
    });
}

export async function checkDotnetInstalled(): Promise<boolean> {
    return new Promise((resolve, reject) => {
        exec('dotnet --version', (error, stdout, stderr) => {
            const version = stdout.trim();
            if (error) {
                vscode.window.showErrorMessage(
                    `.NET Runtime is not installed. (Required version ${constants.RequiredDotnetVersion}.x for this extension)`,
                    'Open installation page'
                ).then(selection => {
                    if (selection === 'Open installation page') {
                        vscode.env.openExternal(vscode.Uri.parse(constants.DotnetInstallUrl));
                    }
                });
                resolve(false);
            } else {
                resolve(true);
            }
        });
    });
}