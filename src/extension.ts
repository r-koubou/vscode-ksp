import * as vscode from 'vscode';
import * as constants from './constants';
import { exec } from 'child_process';

//import * as cp from 'child_process';
//import { LanguageClient, LanguageClientOptions, ServerOptions, TransportKind } from 'vscode-languageclient/node';

//let client: LanguageClient;

const outputChannel = vscode.window.createOutputChannel(constants.OutputChannelName, 'ksp');

export async function activate(context: vscode.ExtensionContext) {
    outputChannel.appendLine('KSP LSP activating');

    const result = await checkDotnetInstalled();

    if (!result) {
        outputChannel.appendLine('KSP LSP failed to activate by missing .NET runtime');
        return;
    }

    //outputChannel.appendLine(`dotnetPath: ${dotnetPath}`);

    // const serverDll = context.asAbsolutePath("server/KSPCompiler.Apps.LSPServer.Embedded.dll");

    // let serverOptions: ServerOptions = {
    //     run: { command: "dotnet", args: [serverDll], transport: TransportKind.stdio },
    //     debug: { command: "dotnet", args: [serverDll], transport: TransportKind.stdio }
    // };

    // const clientOptions: LanguageClientOptions = {
    //     documentSelector: [
    //         { scheme: 'file', language: 'ksp' },
    //         { scheme: 'untitled', language: 'ksp' },
    //     ]
    // };

    // client = new LanguageClient(
    //     'ksp',
    //     'ksp',
    //     serverOptions,
    //     clientOptions
    // );

    // client.start();

    outputChannel.appendLine('KSP LSP activated');
}

// export function deactivate(): Thenable<void> | undefined {
//     return client ? client.stop() : undefined;
// }

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