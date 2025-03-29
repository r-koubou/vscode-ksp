import * as vscode from 'vscode';
import { exec } from 'child_process';
import * as constants from './constants';

export async function checkDotnetInstalled(): Promise<boolean> {
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
