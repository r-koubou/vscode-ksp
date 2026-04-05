import * as vscode from 'vscode';

export const CONFIG_SECTION_NAME = 'ksp';

type ConfigItem<T> = {
    key: string;
    defaultValue: T;
};

// #region Configuration keys

/**
 * Whether to prefer snippet insertion.
 */
export const CONFIG_COMPLETION_PREFER_SNIPPET_INSERTION: ConfigItem<boolean> = {
    key: 'completion.preferSnippetInsertion',
    defaultValue: true,
};

// #endregion

export function getConfigValue<T>(item: ConfigItem<T>): T {
    const section: vscode.WorkspaceConfiguration = vscode.workspace.getConfiguration(CONFIG_SECTION_NAME);

    if (!section) {
        return item.defaultValue;
    }

    const inspect = section.inspect<T>(item.key);

    let result: T = item.defaultValue;

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
