import * as vscode from 'vscode';

import { OutputChannel } from './constants';

const WhatsNewStateKey = 'rkoubou.ksp.whatsNewShownVersion';
const WhatsNewResourcePath = 'resources/whats_new.md';

export async function showWhatsNewIfUpdated(context: vscode.ExtensionContext) {
    const currentVersion = context.extension.packageJSON?.version as string | undefined;

    if (!currentVersion) {
        OutputChannel.appendLine('Unable to determine extension version for whats_new check.');
        return;
    }

    const lastShownVersion = context.globalState.get<string>(WhatsNewStateKey);

    if (!lastShownVersion) {
        await context.globalState.update(WhatsNewStateKey, currentVersion);
        return;
    }

    if (lastShownVersion === currentVersion) {
        return;
    }

    const whatsNewPath = context.asAbsolutePath(WhatsNewResourcePath);
    const whatsNewUri = vscode.Uri.file(whatsNewPath);

    try {
        await vscode.workspace.openTextDocument(whatsNewUri);
        await vscode.commands.executeCommand('markdown.showPreview', whatsNewUri);
        await context.globalState.update(WhatsNewStateKey, currentVersion);
    } catch (error) {
        OutputChannel.appendLine(`Failed to show whats_new.md: ${error}`);
    }
}
