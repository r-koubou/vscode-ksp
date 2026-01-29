import * as vscode from 'vscode';

export const CONFIG_SECTION_NAME = 'ksp';

export function getProperty<T>(key: string, defaultValue: T): T {
    const section: vscode.WorkspaceConfiguration
        = vscode.workspace.getConfiguration(CONFIG_SECTION_NAME);

    if (!section) {
        return defaultValue;
    }

    const instpect = section.inspect<T>(key);

    let result: T = defaultValue;

    if (!instpect) {
        return result;
    }

    if (instpect.workspaceValue !== undefined && instpect.workspaceValue !== null) {
        result = instpect.workspaceValue;
    } else if (instpect.globalValue !== undefined && instpect.globalValue !== null) {
        result = instpect.globalValue;
    }

    return result;
}
