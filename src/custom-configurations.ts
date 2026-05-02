import * as vscode from 'vscode';
import * as constants from './constants';

export const CONFIG_SECTION_NAME = 'ksp';

type ConfigItem<T> = {
    key: string;
    defaultValue: T;
};

// #region Configuration keys

/**
 * Whether to show the "What's New" page after extension updates.
 */
export const CUSTOM_CONFIG_WHATSNEW_ENABLED: ConfigItem<boolean> = {
    key: 'whatsnew.enabled',
    defaultValue: false,
};

// #endregion

export function getCustomConfigValue<T>(item: ConfigItem<T>): T {
    const exttension = vscode.extensions.getExtension(constants.PackageId);

    if (!exttension) {
        return item.defaultValue;
    }

    const customConfig = exttension.packageJSON['ksp.custom.configuration'] as Record<string, any> | undefined;

    if (!customConfig) {
        return item.defaultValue;
    }

    const valuue = customConfig[item.key];

    if (valuue === undefined || valuue === null) {
        return item.defaultValue;
    }

    if (typeof valuue !== typeof item.defaultValue) {
        return item.defaultValue;
    }

    return valuue as T;
}
