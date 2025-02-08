import * as vscode from 'vscode';
import { LanguageClient, LanguageClientOptions, ServerOptions, TransportKind } from 'vscode-languageclient/node';

import { exec } from 'child_process';

import * as constants from './constants';
import { OutputChannel } from './constants';

import * as ObfuscateCommand from './commands/obfuscator';

let client: LanguageClient | null = null;

export async function activate(context: vscode.ExtensionContext) {

    const result = await checkDotnetInstalled();

    if (!result) {
        OutputChannel.appendLine('KSP LSP failed to activate by missing .NET runtime');
        return;
    }

    OutputChannel.appendLine('KSP extension activating');

    const serverAssembly = context.asAbsolutePath('language_server/KSPCompiler.Applications.LSPServer.Embedded.dll');
    OutputChannel.appendLine(`Server assembly path: ${serverAssembly}`);

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

    OutputChannel.appendLine('KSP LSP server creating');

    client = new LanguageClient(
        'ksp',
        'ksp',
        serverOptions,
        clientOptions
    );

    OutputChannel.appendLine('KSP LSP server created');

    client.start().then(() => {
        OutputChannel.appendLine('KSP LSP server started');
    }).catch((reason) => {
        OutputChannel.appendLine('KSP LSP server failed to start');
        OutputChannel.appendLine(reason);
    });

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

async function checkDotnetInstalled(): Promise<boolean> {
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

function registerExtensionCommands(context: vscode.ExtensionContext, client: LanguageClient) {
    ObfuscateCommand.registerCommand(context, client);
}
