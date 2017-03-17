/* =========================================================================

    KSPSymbolUtil.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import vscode = require( 'vscode' );

export enum KSPSymvolType
{
    UNKNOWN,
    VARIABLE_INTEGR,
    VARIABLE_REAL,
    VARIABLE_STRING,
    VARIABLE_INTEGR_ARRAY,
    VARIABLE_REAL_ARRAY,
    VARIABLE_STRING_ARRAY,

    CALLBACK,
    USER_FUNCTION
}

/**
 * General Symbol information for KSP
 */
export class KSPSymbol
{
    public kspSymbolType        : KSPSymvolType = KSPSymvolType.UNKNOWN;
    public isConst              : boolean = false;
    public isUI                 : boolean = false;
    public description          : string  = "";
    public lineNumber           : number  = -1;

    static variableTypeChar2Type( char : string ) : KSPSymvolType
    {
        switch( char )
        {
            case '$': return KSPSymvolType.VARIABLE_INTEGR;
            case '~': return KSPSymvolType.VARIABLE_REAL;
            case '@': return KSPSymvolType.VARIABLE_STRING;
            case '%': return KSPSymvolType.VARIABLE_INTEGR_ARRAY;
            case '?': return KSPSymvolType.VARIABLE_REAL_ARRAY;
            case '!': return KSPSymvolType.VARIABLE_STRING_ARRAY;
            default:  return KSPSymvolType.UNKNOWN;
        }
    }

    static variableTypeChar2String( char : string ) : string
    {
        switch( char )
        {
            case '$': return "Integer";
            case '~': return "Real";
            case '@': return "String";
            case '%': return "Integer Array";
            case '?': return "Real Array";
            case '!': return "String Array";
            default:  return "Unkown";
        }
    }
}

/**
 * Symbol information for KSP (vscode.SymbolInformation extension)
 */
export class KSPSymbolInformation extends vscode.SymbolInformation
{
    private kspSymbol : KSPSymbol;

    constructor( name: string,
                 kind: vscode.SymbolKind,
                 containerName: string,
                 location: vscode.Location )
    {
        super( name, kind,containerName, location );
        this.containerName              = containerName;
        this.kspSymbol                  = new KSPSymbol();
        this.kspSymbol.description      = containerName;
    }

    public setKspSymbolValue( lineNumber : number, isConst : boolean, isUI : boolean, type : KSPSymvolType = KSPSymvolType.UNKNOWN )
    {
        this.KspSymbol.lineNumber    = lineNumber;
        this.KspSymbol.isConst       = isConst;
        this.KspSymbol.isUI          = isUI;
        this.KspSymbol.kspSymbolType = type;
    }

    get KspSymbol() : KSPSymbol { return this.kspSymbol; }

}

export class KSPSymbolUtil
{
    static collect( document: vscode.TextDocument, token: vscode.CancellationToken ) : KSPSymbolInformation[]
    {
        var result: KSPSymbolInformation[] = [];

        let count = document.lineCount;
        for( var i = 0; i < count; i++ )
        {
            //-----------------------------------------------------------------
            // check declare variables
            //-----------------------------------------------------------------
            {
                var DECLARE_REGEX = /^\s*declare\s+(ui_[a-z|A-Z]+|const)?\s*([\$%~\?@!][a-zA-Z_\x7f-\xff][a-zA-Z0-9_\x7f-\xff]*)/g;

                var text  = document.lineAt( i ).text;
                var match = DECLARE_REGEX.exec( text );
                if( match )
                {
                    var isConst                     = match[ 1 ] && match[ 1 ].toString() == "const";
                    var isUI                        = match[ 1 ] && match[ 1 ].startsWith( "ui_" );
                    var symKind: vscode.SymbolKind  = vscode.SymbolKind.Variable;
                    var name : string               = match[ 2 ];
                    var containerName : string      = "Variable";

                    if( isConst )
                    {
                        containerName = "Constant Variable";
                        symKind = vscode.SymbolKind.Constant;
                    }

                    var variableTypeChar            = name.charAt( 0 );
                    var variableType : string       = "(" + KSPSymbol.variableTypeChar2String( variableTypeChar ) + ")";
                    var symbolType : KSPSymvolType  = KSPSymbol.variableTypeChar2Type( variableTypeChar );

                    if( isUI )
                    {
                        containerName = "UI Variable";
                        variableType  = "(" + match[ 1 ].trim() + ")";
                    }

                    var add = new KSPSymbolInformation(
                        name.substr( 1 ),
                        symKind, containerName + " " + variableType,
                        new vscode.Location( document.uri, new vscode.Position( i, i ) )
                    );

                    add.setKspSymbolValue( i, isConst, isUI, symbolType );
                    result.push( add );
                    continue;
                }
            } //~check declare variables
            //-----------------------------------------------------------------
            // check callback ( on #### )
            //-----------------------------------------------------------------
            {
                var DECLARE_REGEX = /^\s*(on\s+)([a-z|A-Z|_]+)(\s*\(\s*[^\)]+\s*\))?/g;

                var text  = document.lineAt( i ).text;
                var match = DECLARE_REGEX.exec( text );
                if( match )
                {
                    var isUI                        = match[ 2 ] != undefined && match[ 3 ] != undefined && match[ 2 ].startsWith( "ui_" );
                    var symKind: vscode.SymbolKind  = vscode.SymbolKind.Function;
                    var name : string               = match[ 2 ];
                    var containerName : string      = "Callback";

                    if( !match[ 2 ] && !match[ 3 ] )
                    {
                        continue;
                    }

                    name = "on " + name;

                    if( isUI )
                    {
                        var uiName = match[ 3 ].replace( "(", "" ).replace( ")", "" ).trim();
                        containerName = "UI Callback for " + uiName;
                    }

                    var add = new KSPSymbolInformation(
                        name,
                        symKind, containerName,
                        new vscode.Location( document.uri, new vscode.Position( i, i ) )
                    );

                    add.setKspSymbolValue( i, isConst, isUI, KSPSymvolType.CALLBACK );
                    result.push( add );
                    continue;
                }
            } //~callback
            //-----------------------------------------------------------------
            // check user function ( function #### )
            //-----------------------------------------------------------------
            {
                var DECLARE_REGEX = /^\s*(function\s+)([a-z|A-Z|_]+)/g;

                var text  = document.lineAt( i ).text;
                var match = DECLARE_REGEX.exec( text );
                if( match )
                {
                    var symKind: vscode.SymbolKind  = vscode.SymbolKind.Function;
                    var name : string               = match[ 2 ];
                    var containerName : string      = "Function";
                    var add = new KSPSymbolInformation(
                        name,
                        symKind, containerName,
                        new vscode.Location( document.uri, new vscode.Position( i, i ) )
                    );

                    add.setKspSymbolValue( i, isConst, isUI, KSPSymvolType.USER_FUNCTION );
                    result.push( add );
                    continue;
                }
            } //~function
        }

        return result;
    }
}
