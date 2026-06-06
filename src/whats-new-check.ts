import * as vscode from 'vscode';

import { OutputChannel } from './constants';

const WhatsNewStateKey = 'rkoubou.ksp.whatsNewShownVersion';
const ChangelogUrl = 'https://github.com/r-koubou/vscode-ksp/blob/main/CHANGELOG.md';

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

    OutputChannel.appendLine(`Extension updated from version ${lastShownVersion} to ${currentVersion}. Showing "What's New" notification.`);

    vscode.window
        .showInformationMessage(
            `NI KONTAKT Script extension has been updated to version "${currentVersion}". Would you like to see what's new?`,
            'Show Changelog'
        )
        .then((selection) => {
            if (selection === 'Show Changelog') {
                vscode.env.openExternal(vscode.Uri.parse(ChangelogUrl));
            }
        });
}
