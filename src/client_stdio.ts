import * as vscode from 'vscode';
import { LanguageClient, LanguageClientOptions, ServerOptions, TransportKind } from 'vscode-languageclient/node';

import { exec } from 'child_process';

import * as constants from './constants';
import { OutputChannel } from './constants';

export async function startLspClient(context: vscode.ExtensionContext): Promise<LanguageClient | null | undefined > {

    const result = await checkDotnetInstalled();

    if(!result) {
        OutputChannel.appendLine('KSP LSP failed to activate by missing .NET runtime');
        return null;
    }

    OutputChannel.appendLine('KSP extension activating');

    const serverDirectory = context.asAbsolutePath('language_server');
    const serverAssembly = `${serverDirectory}/KSPCompiler.Applications.LSServer.LanguageServerFramework.dll`;

    OutputChannel.appendLine(`Server assembly path: ${serverAssembly}`);

    const serverOptions: ServerOptions = {
        run: { command: "dotnet", args: [serverAssembly], transport: TransportKind.stdio, options: { cwd: serverDirectory } },
        debug: { command: "dotnet", args: [serverAssembly], transport: TransportKind.stdio, options: { cwd: serverDirectory } }
    };

    const clientOptions: LanguageClientOptions = {
        documentSelector: [
            { scheme: 'file', language: 'ksp' },
            { scheme: 'untitled', language: 'ksp' },
        ]
    };

    OutputChannel.appendLine('KSP LSP server creating');

    const client = new LanguageClient(
        'ksp',
        'ksp',
        serverOptions,
        clientOptions
    );

    OutputChannel.appendLine('KSP LSP server created');

    await client.start().then(() => {
        OutputChannel.appendLine('KSP LSP server started');
    }).catch((reason) => {
        OutputChannel.appendLine('KSP LSP server failed to start');
        OutputChannel.appendLine(reason);
    });

    return client;
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
