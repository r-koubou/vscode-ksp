import * as vscode from 'vscode';

export const CONFIG_SECTION_NAME = 'ksp';

export function getProperty<T>(key: string, defaultValue: T): T | undefined {
    const section: vscode.WorkspaceConfiguration
        = vscode.workspace.getConfiguration(CONFIG_SECTION_NAME);

    if (!section) {
        return defaultValue;
    }

    const inspect = section.inspect<T>(key);

    let result: T = defaultValue;

    if (!inspect) {
        return result;
    }

    if (inspect.workspaceValue !== undefined && inspect.workspaceValue !== null) {
        result = inspect.workspaceValue;
    } else if (inspect.globalValue !== undefined && inspect.globalValue !== null) {
        result = inspect.globalValue;
    }

    return result;
}
