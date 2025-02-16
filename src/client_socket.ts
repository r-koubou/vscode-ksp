import * as vscode from 'vscode';
import { LanguageClient, LanguageClientOptions, ServerOptions } from 'vscode-languageclient/node';

import net from 'net';
import { exec, spawn } from 'child_process';

import * as constants from './constants';
import { OutputChannel } from './constants';
import * as property from './properties';

const sleep = async (ms: number) => new Promise((res) => setTimeout(res, ms));

export async function startLspClient(context: vscode.ExtensionContext): Promise<LanguageClient | null | undefined> {

    const result = await checkDotnetInstalled();

    if (!result) {
        OutputChannel.appendLine('KSP LSP failed to activate by missing .NET runtime');
        return null;
    }

    OutputChannel.appendLine('KSP extension activating');

    const serverDirectory = context.asAbsolutePath('language_server');
    const serverAssembly = `${serverDirectory}/KSPCompiler.Applications.LSPServer.Sockets.dll`;

    OutputChannel.appendLine(`Server assembly path: ${serverAssembly}`);

    const port = property.getProperty(property.CONFIG_LSP_SERVER_PORT, 54321);

    const maxRetries = 5;
    const retryInterval = 2000; // 2 seconds

    OutputChannel.appendLine('KSP LSP server creating');

    const serverOptions: ServerOptions = () => {
        return new Promise((resolve, reject) => {
            let retries = 0;
            const tryConnect = () => {
                const socket = net.connect({ port: port }, () => {
                    resolve({
                        reader: socket,
                        writer: socket
                    });
                });

                socket.on('error', (err: any) => {
                    if (retries < maxRetries) {
                        retries++;
                        setTimeout(tryConnect, retryInterval);
                        OutputChannel.appendLine(`Failed to connect to server. Retrying... (${retries}/${maxRetries})`);
                    } else {
                        reject(new Error('Failed to connect to server after multiple attempts'));
                    }
                });
            };

            OutputChannel.appendLine(`Connecting to server on port ${port}`);
            tryConnect();
        });
    };

    //exec(`dotnet ${serverAssembly} --port ${port}`, { cwd: serverDirectory });

    const serverProcess = spawn(
        'dotnet', [serverAssembly, '--port', port.toString()],
        { cwd: serverDirectory }
    );

    await sleep(3000);

    OutputChannel.appendLine('KSP LSP server created');

    OutputChannel.appendLine('KSP LSP client creating');

    const clientOptions: LanguageClientOptions = {
        documentSelector: [
            { scheme: 'file', language: 'ksp' },
            { scheme: 'untitled', language: 'ksp' },
        ]
    };


    const client = new LanguageClient(
        'ksp',
        'ksp',
        serverOptions,
        clientOptions
    );

    OutputChannel.appendLine('KSP LSP client created');

    await client.start().then(() => {
        OutputChannel.appendLine('KSP LSP client started');
    }).catch((reason) => {
        OutputChannel.appendLine('KSP LSP client failed to start');
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
