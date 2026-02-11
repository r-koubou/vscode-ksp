import * as vscode from 'vscode';
import MarkdownIt from 'markdown-it';
import * as path from 'path';

import { OutputChannel } from './constants';

const WhatsNewStateKey = 'rkoubou.ksp.whatsNewShownVersion';
const WhatsNewResourcePath = 'resources/whats-new/whats-new.md';
const WhatsNewWebViewType = 'rkoubou.ksp.whatsNew';
const WhatsNewWebViewTitle = "NI KONTAKT Script: What's New";

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
    const whatsNewDirUri = vscode.Uri.file(path.dirname(whatsNewPath));

    try {
        const markdownBytes = await vscode.workspace.fs.readFile(whatsNewUri);
        const markdownText = new TextDecoder('utf-8').decode(markdownBytes);
        const panel = vscode.window.createWebviewPanel(
            WhatsNewWebViewType,
            WhatsNewWebViewTitle,
            vscode.ViewColumn.Active,
            {
                enableScripts: false,
                localResourceRoots: [whatsNewDirUri],
            },
        );

        const markdownIt = new MarkdownIt({
            html: false,
            linkify: true,
            typographer: false,
        });

        const webview = panel.webview;
        const defaultImageRule =
            markdownIt.renderer.rules.image ??
            ((tokens, idx, options, _env, self) => self.renderToken(tokens, idx, options));

        markdownIt.renderer.rules.image = (tokens, index, options, env, self) => {
            const token = tokens[index];
            const src = token.attrGet('src');
            if (!src || src.startsWith('#')) {
                return defaultImageRule(tokens, index, options, env, self);
            }

            const schemeMatch = /^[a-zA-Z][a-zA-Z0-9+.-]*:/.exec(src);
            if (schemeMatch) {
                return defaultImageRule(tokens, index, options, env, self);
            }

            const suffixIndex = src.search(/[?#]/);
            const pathPart = suffixIndex === -1 ? src : src.slice(0, suffixIndex);
            const suffix = suffixIndex === -1 ? '' : src.slice(suffixIndex);

            if (!pathPart || pathPart.startsWith('/')) {
                return defaultImageRule(tokens, index, options, env, self);
            }

            const resolvedFsPath = path.resolve(whatsNewDirUri.fsPath, pathPart);
            const dirPrefix = whatsNewDirUri.fsPath.endsWith(path.sep)
                ? whatsNewDirUri.fsPath
                : `${whatsNewDirUri.fsPath}${path.sep}`;

            if (!resolvedFsPath.startsWith(dirPrefix)) {
                return defaultImageRule(tokens, index, options, env, self);
            }

            const resourceUri = vscode.Uri.file(resolvedFsPath);
            const webviewUri = webview.asWebviewUri(resourceUri);

            token.attrSet('src', `${webviewUri.toString()}${suffix}`);

            return defaultImageRule(tokens, index, options, env, self);
        };

        const renderedHtml = markdownIt.render(markdownText);

        panel.webview.html = buildWhatsNewHtml(panel.webview, renderedHtml);
        await context.globalState.update(WhatsNewStateKey, currentVersion);
    } catch (error) {
        OutputChannel.appendLine(`Failed to show whats_new.md: ${error}`);
    }
}

function buildWhatsNewHtml(webview: vscode.Webview, contentHtml: string) {
    const nonce = createNonce();

    return `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="Content-Security-Policy" content="default-src 'none'; style-src ${webview.cspSource}; img-src ${webview.cspSource};">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>KSP: What's New</title>
    <style nonce="${nonce}">
        :root {
            color-scheme: light dark;
        }

        body {
            padding: 24px;
            line-height: 1.6;
        }

        h1, h2, h3 {
            margin-top: 1.2em;
        }

        pre {
            padding: 12px;
            overflow-x: auto;
            border-radius: 6px;
        }
    </style>
</head>
<body>
    ${contentHtml}
</body>
</html>`;
}

function createNonce() {
    const possible = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let value = '';

    for (let i = 0; i < 32; i += 1) {
        value += possible.charAt(Math.floor(Math.random() * possible.length));
    }

    return value;
}
