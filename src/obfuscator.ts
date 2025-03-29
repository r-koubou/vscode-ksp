import * as vscode from 'vscode';
import { LanguageClient } from 'vscode-languageclient/node';

import * as Constants from './constants';


export async function obfuscate(
    context: vscode.ExtensionContext,
    client: LanguageClient) {

    if (!client) {
        throw new Error('LSP client is not initialized.');
    }

    const editor = vscode.window.activeTextEditor;
    const window = vscode.window;

    if (!editor) {
        return;
    }

    if (editor.document.languageId !== Constants.LanguageId) {
        window.showErrorMessage('Language mode is not KSP.');
        return;
    }

    if (editor.document.isDirty) {
        window.showErrorMessage('Please save the file before obfuscating.');
        return;
    }

    if (!client) {
        window.showErrorMessage('KSP Language server is not running.');
        return;
    }

    const document = editor.document;
    const documentUri = document.uri.toString();

    try {

        // LSPサーバーにコマンドを送信
        const result: string = await client.sendRequest('workspace/executeCommand', {
            command: 'ksp.obfuscate',
            arguments: [documentUri]
        });

        if (!result) {
            window.showErrorMessage('Obfuscation failed. Check error in script.');
            return;
        }

        // オブファスケート後のファイル名を構成
        // 'original.txt' -> 'original_obfuscated.txt'
        const originalUri = document.uri;
        const originalFileName = originalUri.path.split('/').pop();
        const extIndex = originalFileName?.lastIndexOf('.') ?? -1;

        let obfuscatedFileName = originalFileName ?
            (extIndex > 0 ?
                originalFileName.slice(0, extIndex) + '_obfuscated' + originalFileName.slice(extIndex) :
                originalFileName + '_obfuscated')
            : 'obfuscated.txt';

        // 「名前をつけて保存」ダイアログ
        const uri = await window.showSaveDialog({
            filters: { 'KSP Script': ['ksp', 'txt'], 'All Files': ['*'] },
            title: 'Save Obfuscated Code',
            defaultUri: originalUri.with({ path: originalUri.path.replace(originalFileName!, obfuscatedFileName) }),
        });

        if (!uri) {
            // Cancelled
            return;
        }

        // ファイルに書き込み
        try {
            await vscode.workspace.fs.writeFile(uri, Buffer.from(result, 'utf8'));
            window.showInformationMessage(
                `Saved obfuscated text to ${uri.fsPath}`,
                'Open script'
            ).then((value) => {
                if (value === 'Open script') {
                    vscode.workspace.openTextDocument(uri).then((doc) => {
                        vscode.window.showTextDocument(doc);
                    });
                }
            });
        } catch (err) {
            window.showErrorMessage(`Failed to save file: ${err}`);
        }

    } catch (error) {
        window.showErrorMessage(`Error: ${error}`);
    }
}
