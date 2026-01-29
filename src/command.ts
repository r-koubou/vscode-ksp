import * as vscode from 'vscode';

export function registerCommand(
    commandName: string,
    context: vscode.ExtensionContext,
    action: () => Promise<void>
) {
    const disposable = vscode.commands.registerCommand(commandName, async () => {
        try {
            await action();
        } catch {}
    });

    context.subscriptions.push(disposable);
}
