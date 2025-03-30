import * as vscode from 'vscode';

import * as constants from './constants';
import { OutputChannel } from './constants';

export async function getDotnetPath(context: vscode.ExtensionContext): Promise<string | null> {

    // Check if the "ms-dotnettools.vscode-dotnet-runtime" extension exists
    const dotnetRuntimeExtension
        = vscode.extensions.getExtension('ms-dotnettools.vscode-dotnet-runtime');

    const dotnetRuntimeExtensionUrl = 'https://marketplace.visualstudio.com/items?itemName=ms-dotnettools.vscode-dotnet-runtime';

    if (!dotnetRuntimeExtension || !dotnetRuntimeExtension.isActive) {
        vscode.window.showErrorMessage(
            `The VSCode Extension ".NET Install Tools" is not installed. Please install it to proceed.`,
            'Open installation page'
        ).then(selection => {
            if (selection === 'Open installation page') {
                vscode.env.openExternal(vscode.Uri.parse(dotnetRuntimeExtensionUrl));
            }
        });
        return Promise.reject(null);
    }

    // Check if installed dotnet version is compatible
    const dotnetPath = await vscode.commands.executeCommand<any | undefined>(
        'dotnet.acquireStatus',
        {
            version: constants.RequiredDotnetVersion,
            requestingExtensionId: context.extension.id
        }
    );

    if (dotnetPath) {
        OutputChannel.appendLine(`Acquired .NET Runtime: ${dotnetPath.dotnetPath}`);
        return Promise.resolve(dotnetPath.dotnetPath);
    }

    return await vscode.window.withProgress({
        location: vscode.ProgressLocation.Notification,
        title: "Acquiring .NET Runtime",
        cancellable: false
    }, async (progress) => {
        try {
            progress.report({ message: `installing .NET Runtime ${constants.RequiredDotnetVersion}` });
            const acquireResult = await vscode.commands.executeCommand<{ dotnetPath: string }>(
                'dotnet.acquire',
                {
                    version: constants.RequiredDotnetVersion,
                    requestingExtensionId: context.extension.id
                }
            );

            if (!acquireResult) {
                vscode.window.showErrorMessage("Failed to acquire .NET Runtime.");
                return Promise.reject(null);
            }

            const dotnetPath = acquireResult.dotnetPath;

            if (!dotnetPath) {
                vscode.window.showErrorMessage("Failed to acquire .NET Runtime.");
                return Promise.reject(null);
            }

            OutputChannel.appendLine(`Acquired .NET Runtime: ${dotnetPath}`);
            return Promise.resolve(dotnetPath);
        } catch {
            vscode.window.showErrorMessage("Failed to acquire .NET Runtime.");
            return Promise.reject(null);
        }
    });
}
