import * as vscode from 'vscode';
import { LanguageClient, LanguageClientOptions, ServerOptions, TransportKind } from 'vscode-languageclient/node';

import * as DotnetInstall from './dotnet-install';
import { OutputChannel } from './constants';

export async function startLspClient(context: vscode.ExtensionContext): Promise<LanguageClient | null | undefined> {

    const dotnetPath = await DotnetInstall.getDotnetPath(context);

    if (!dotnetPath) {
        OutputChannel.appendLine('KSP Language Server failed to activate by missing .NET runtime');
        return null;
    }

    const serverDirectory = context.asAbsolutePath('language_server');
    const serverAssembly = `${serverDirectory}/KSPCompiler.Features.Applications.LanguageServer.LanguageServerFramework.dll`;

    OutputChannel.appendLine(`Server assembly path: ${serverAssembly}`);

    const serverOptions: ServerOptions = {
        run: { command: dotnetPath, args: [serverAssembly], transport: TransportKind.stdio, options: { cwd: serverDirectory } },
        debug: { command: dotnetPath, args: [serverAssembly], transport: TransportKind.stdio, options: { cwd: serverDirectory } }
    };

    const clientOptions: LanguageClientOptions = {
        documentSelector: [
            { scheme: 'file', language: 'ksp' },
            { scheme: 'untitled', language: 'ksp' },
        ]
    };

    OutputChannel.appendLine('KSP Language Server creating');

    const client = new LanguageClient(
        'ksp',
        'ksp',
        serverOptions,
        clientOptions
    );

    OutputChannel.appendLine('KSP Language server created');

    await client.start().then(() => {
        OutputChannel.appendLine('KSP Language Server started');
    }).catch((reason) => {
        OutputChannel.appendLine('KSP Language Server failed to start');
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
    } catch { }

    const index = context.subscriptions.findIndex(d => d === client);
    if (index !== -1) {
        context.subscriptions.splice(index, 1);
    }
    OutputChannel.appendLine('KSP Language Server stopped');
}
