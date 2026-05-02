import * as vscode from 'vscode';

export const LanguageId             = 'ksp';
export const OutputChannelName      = 'KSP';
export const RequiredDotnetVersion  = '10.0';
export const DotnetInstallUrl       = 'https://dotnet.microsoft.com/download';

export const OutputChannel          = vscode.window.createOutputChannel(OutputChannelName, LanguageId);
