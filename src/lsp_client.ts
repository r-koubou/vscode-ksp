import * as vscode from 'vscode';
import { LanguageClient, LanguageClientOptions, ServerOptions, TransportKind } from 'vscode-languageclient/node';

import * as DotnetInstall from './dotnet_install';
import { OutputChannel } from './constants';

export async function startLspClient(context: vscode.ExtensionContext): Promise<LanguageClient | null | undefined> {

    const result = await DotnetInstall.checkDotnetInstalled();

    if (!result) {
        OutputChannel.appendLine('KSP LSP failed to activate by missing .NET runtime');
        return null;
    }

    OutputChannel.appendLine('KSP extension activating');

    const serverDirectory = context.asAbsolutePath('language_server');
    const serverAssembly = `${serverDirectory}/KSPCompiler.Features.Applications.LanguageServer.LanguageServerFramework.dll`;

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

    context.subscriptions.push(client);

    return client;
}

export async function stopLspClient(context: vscode.ExtensionContext, client: LanguageClient | null | undefined) {
    if (!client) {
        return;
    }

    try {
        await client.stop();
        client.dispose();
    } catch {}

    const index = context.subscriptions.findIndex(d => d === client);
    if (index !== -1) {
        context.subscriptions.splice(index, 1);
    }
    OutputChannel.appendLine('KSP LSP server stopped');
}
