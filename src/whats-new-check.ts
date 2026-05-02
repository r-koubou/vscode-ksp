import * as vscode from 'vscode';
import MarkdownIt from 'markdown-it';
import * as path from 'path';

import { OutputChannel } from './constants';

const WhatsNewStateKey = 'rkoubou.ksp.whatsNewShownVersion';
const MarkdownResourcePath = 'resources/whats-new/whats-new.md';
const HtmlTemplatePath = 'resources/whats-new/template.html';
const CssResourcePath = 'resources/whats-new/whats-new.css';
const WebViewType = 'rkoubou.ksp.whatsNew';
const WebViewTitle = "NI KONTAKT Script: What's New";

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

    const whatsNewPath = context.asAbsolutePath(MarkdownResourcePath);
    const whatsNewUri = vscode.Uri.file(whatsNewPath);
    const whatsNewDirUri = vscode.Uri.file(path.dirname(whatsNewPath));

    try {
        const markdownBytes = await vscode.workspace.fs.readFile(whatsNewUri);
        const markdownText = new TextDecoder('utf-8').decode(markdownBytes);
        const panel = vscode.window.createWebviewPanel(WebViewType, WebViewTitle, vscode.ViewColumn.Active, {
            enableScripts: false,
            localResourceRoots: [whatsNewDirUri],
        });

        const markdownIt = new MarkdownIt({
            html: false,
            linkify: true,
            typographer: false,
        });

        const webview = panel.webview;
        const renderedHtml = renderMarkdownToHtml(context, webview, markdownIt, markdownText);

        panel.webview.html = await buildWhatsNewHtml(context, panel.webview, renderedHtml);
        await context.globalState.update(WhatsNewStateKey, currentVersion);
    } catch (error) {
        OutputChannel.appendLine(`Failed to show whats_new.md: ${error}`);
    }
}

function renderMarkdownToHtml(
    context: vscode.ExtensionContext,
    webview: vscode.Webview,
    markdownIt: MarkdownIt,
    markdownText: string,
) {
    const whatsNewPath = context.asAbsolutePath(MarkdownResourcePath);
    const whatsNewDirUri = vscode.Uri.file(path.dirname(whatsNewPath));

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

    return markdownIt.render(markdownText);
}

async function buildWhatsNewHtml(context: vscode.ExtensionContext, webview: vscode.Webview, contentHtml: string) {
    const htmlTemplateFsPath = context.asAbsolutePath(HtmlTemplatePath);
    const htmlTemplateBytes = await vscode.workspace.fs.readFile(vscode.Uri.file(htmlTemplateFsPath));
    let html = new TextDecoder('utf-8').decode(htmlTemplateBytes);

    const cssFsPath = context.asAbsolutePath(CssResourcePath);
    const cssPath = vscode.Uri.file(cssFsPath);
    const cssUri = webview.asWebviewUri(cssPath);

    html = html.replace('{cssUri}', cssUri.toString());
    html = html.replace(/{cspSource}/g, webview.cspSource);
    html = html.replace('{markdown}', contentHtml);

    if (context.extensionMode === vscode.ExtensionMode.Development) {
        OutputChannel.appendLine('WhatsNew HTML:');
        OutputChannel.appendLine(html);
    }

    return html;
}
