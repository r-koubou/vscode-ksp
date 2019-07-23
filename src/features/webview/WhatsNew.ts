import vscode  = require( 'vscode' );
import * as path            from 'path';
import * as fs              from 'fs';
import * as tmp             from 'tmp';
import * as constant        from '../KSPExtensionConstants';

export function show()
{
    const WEBVIEW_ROOT = path.join(
        constant.EXTENTION_DIR,
        "resources",
        "webview"
    );

    //let resourceRoot: vscode.Uri[] = [ vscode.Uri.file( WEBVIEW_ROOT ).with( {scheme: "vscode-resource"} ) ];
    let panel: vscode.WebviewPanel = vscode.window.createWebviewPanel(
        "whatsnewksp",
        "KSP - What's new",
        vscode.ViewColumn.One,
        {
            enableScripts: false,
            //localResourceRoots: resourceRoot
        }
    );

    panel.webview.html = fs.readFileSync( path.join(
        WEBVIEW_ROOT,
        "whatsnew.html")
    ).toString();
}
