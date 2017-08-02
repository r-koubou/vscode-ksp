/* =========================================================================

    KSPSymbolUtil.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import vscode = require( 'vscode' );

export enum KSPSymbolType
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
    public name                 : string = "";
    public kspSymbolType        : KSPSymbolType = KSPSymbolType.UNKNOWN;
    public isConst              : boolean = false;
    public isUI                 : boolean = false;
    public uiVariableName       : string  = ""; // if isUI == true and type == callback, set a uiVariable Name
    public description          : string  = "";
    public lineNumber           : number  = -1;
    public colmn                : number  = -1;

    public toVariableNameFormat( isUI : boolean = false ) : string
    {
        var ret = this.name;
        if( isUI )
        {
            ret = this.uiVariableName;
        }

        switch( this.kspSymbolType )
        {
            case KSPSymbolType.VARIABLE_INTEGR:         return '$' + ret;
            case KSPSymbolType.VARIABLE_REAL:           return '~' + ret;
            case KSPSymbolType.VARIABLE_STRING:         return '@' + ret;
            case KSPSymbolType.VARIABLE_INTEGR_ARRAY:   return '%' + ret;
            case KSPSymbolType.VARIABLE_REAL_ARRAY:     return '?' + ret;
            case KSPSymbolType.VARIABLE_STRING_ARRAY:   return '!' + ret;
            case KSPSymbolType.CALLBACK:                return '$' + ret;
            default:  return ret;
        }
    }

    static variableTypeChar2Type( char : string ) : KSPSymbolType
    {
        switch( char )
        {
            case '$': return KSPSymbolType.VARIABLE_INTEGR;
            case '~': return KSPSymbolType.VARIABLE_REAL;
            case '@': return KSPSymbolType.VARIABLE_STRING;
            case '%': return KSPSymbolType.VARIABLE_INTEGR_ARRAY;
            case '?': return KSPSymbolType.VARIABLE_REAL_ARRAY;
            case '!': return KSPSymbolType.VARIABLE_STRING_ARRAY;
            default:  return KSPSymbolType.UNKNOWN;
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

    static isVariable( name : string ) : boolean
    {
        const type : KSPSymbolType = KSPSymbol.variableTypeChar2Type( name.charAt( 0 ) );
        switch( type )
        {
            case KSPSymbolType.VARIABLE_INTEGR:
            case KSPSymbolType.VARIABLE_REAL:
            case KSPSymbolType.VARIABLE_STRING:
            case KSPSymbolType.VARIABLE_INTEGR_ARRAY:
            case KSPSymbolType.VARIABLE_REAL_ARRAY:
            case KSPSymbolType.VARIABLE_STRING_ARRAY:
                return true;
            default:
                return false;
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

    public setKspSymbolValue( lineNumber : number, colmn : number, isConst : boolean, isUI : boolean, type : KSPSymbolType )
    {
        this.KspSymbol.name          = this.name;
        this.KspSymbol.lineNumber    = lineNumber;
        this.KspSymbol.colmn         = colmn;
        this.KspSymbol.isConst       = isConst;
        this.KspSymbol.isUI          = isUI;
        this.KspSymbol.kspSymbolType = type;
    }

    public isVariable() : boolean
    {
        switch( this.kspSymbol.kspSymbolType )
        {
            case KSPSymbolType.VARIABLE_INTEGR:
            case KSPSymbolType.VARIABLE_REAL:
            case KSPSymbolType.VARIABLE_STRING:
            case KSPSymbolType.VARIABLE_INTEGR_ARRAY:
            case KSPSymbolType.VARIABLE_REAL_ARRAY:
            case KSPSymbolType.VARIABLE_STRING_ARRAY:
                return true;
            default:
                return false;
        }
    }

    public isUserFunction() : boolean
    {
        switch( this.kspSymbol.kspSymbolType )
        {
            case KSPSymbolType.USER_FUNCTION:
                return true;
            default:
                return false;
        }
    }

    get KspSymbol() : KSPSymbol { return this.kspSymbol; }

}

export class KSPSymbolUtil
{
    public static readonly REGEX_SYMBOL_BOUNDARY : RegExp     = /[\s|\(|\)|\{|\}|:|\[|\]|,|\+|-|\/|\*|<|>|\^|"]+/g
    public static readonly REGEX_SYMBOL_BOUNDARY_STR : string = "[\\s|\\(|\\)|\\{|\\}|:|\\[|\\]|,|\\+|-|\\/|\\*|<|>|\\^|\\\"]+";

    static startAt( lineText:string, position:vscode.Position ) : number
    {
        for( var i = position.character - 1; i >= 0; i-- )
        {
            var regex : RegExp = new RegExp( KSPSymbolUtil.REGEX_SYMBOL_BOUNDARY );
            var char  = lineText.charAt( i );
            var match = regex.exec( char );
            if( match )
            {
                return i + 1;
            }
        }
        return position.character;
    }

    static endAt( lineText:string, position:vscode.Position ) : number
    {
        for( var i = position.character + 1; i < lineText.length; i++ )
        {
            var regex : RegExp = new RegExp( KSPSymbolUtil.REGEX_SYMBOL_BOUNDARY );
            var char  = lineText.charAt( i );
            var match = regex.exec( char );
            if( match )
            {
                return i - 1;
            }
        }
        return position.character;
    }

    static parseSymbolAt( document: vscode.TextDocument, position: vscode.Position ) : string
    {
        var textLine : vscode.TextLine = document.lineAt( position.line );
        var line   : string = textLine.text;
        var eolPos : number = line.length;
        var symbol : string = "";
        for( var i = position.character; i < eolPos; i++ )
        {
            var regex : RegExp = KSPSymbolUtil.REGEX_SYMBOL_BOUNDARY;
            var char  = line.charAt( i );
            var match = regex.exec( char );
            if( match )
            {
                break;
            }
            symbol += char;
        }
        for( var i = position.character - 1; i >= 0; i-- )
        {
            var regex : RegExp = KSPSymbolUtil.REGEX_SYMBOL_BOUNDARY;
            var char  = line.charAt( i );
            var match = regex.exec( char );
            if( match )
            {
                break;
            }
            symbol = char + symbol;
        }
        return symbol.trim();
    }

    static collect( document: vscode.TextDocument, token: vscode.CancellationToken, endLineNumber: number = -1 ) : KSPSymbolInformation[]
    {
        var result: KSPSymbolInformation[] = [];

        var count = document.lineCount;
        if( endLineNumber >= 0 )
        {
            count = endLineNumber;
        }
        for( var i = 0; i < count; i++ )
        {
            //-----------------------------------------------------------------
            // check declare variables
            //-----------------------------------------------------------------
            {
                var DECLARE_REGEX = /^\s*declare\s+(ui_[a-zA-Z0-9_]+|const)?\s*([\$%~\?@!][a-zA-Z_][a-zA-Z0-9_]*)/g;

                var text  = document.lineAt( i ).text;
                var match = DECLARE_REGEX.exec( text );
                if( match )
                {
                    var isConst                     = match[ 1 ] && match[ 1 ].toString() == "const";
                    var isUI                        = match[ 1 ] && match[ 1 ].startsWith( "ui_" );
                    var symKind: vscode.SymbolKind  = vscode.SymbolKind.Variable;
                    var name : string               = match[ 2 ];
                    var containerName : string      = "Variable";
                    var colmn : number              = text.indexOf( name );

                    if( isConst )
                    {
                        containerName = "Constant Variable";
                        symKind = vscode.SymbolKind.Constant;
                    }

                    var variableTypeChar            = name.charAt( 0 );
                    var variableType : string       = "(" + KSPSymbol.variableTypeChar2String( variableTypeChar ) + ")";
                    var symbolType : KSPSymbolType  = KSPSymbol.variableTypeChar2Type( variableTypeChar );

                    if( isUI )
                    {
                        containerName = "UI Variable";
                        variableType  = "(" + match[ 1 ].trim() + ")";
                    }

                    var add = new KSPSymbolInformation(
                        name.substr( 1 ),
                        symKind, containerName + " " + variableType,
                        new vscode.Location( document.uri, new vscode.Position( i, colmn ) )
                    );
                    add.setKspSymbolValue( i, text.indexOf( name ), isConst, isUI, symbolType );
                    result.push( add );
                    continue;
                }
            } //~check declare variables
            //-----------------------------------------------------------------
            // check callback ( on #### )
            //-----------------------------------------------------------------
            {
                var DECLARE_REGEX = /^\s*(on\s+)([a-zA-Z0-9_]+)(\s*\(\s*[^\)]+\s*\))?/g;

                var text  = document.lineAt( i ).text;
                var match = DECLARE_REGEX.exec( text );
                if( match )
                {
                    var isUI                        = match[ 2 ] != undefined && match[ 3 ] != undefined && match[ 2 ].startsWith( "ui_" );
                    var uiName                      = null;
                    var symKind: vscode.SymbolKind  = vscode.SymbolKind.Function;
                    var name : string               = match[ 2 ];
                    var containerName : string      = "Callback";
                    var colmn : number              = text.indexOf( name );

                    if( !match[ 2 ] && !match[ 3 ] )
                    {
                        continue;
                    }

                    name = "on " + name;

                    if( isUI )
                    {
                        uiName = match[ 3 ].replace( "(", "" ).replace( ")", "" ).trim();
                        containerName = "UI Callback for " + uiName;
                    }

                    var add = new KSPSymbolInformation(
                        name,
                        symKind, containerName,
                        new vscode.Location( document.uri, new vscode.Position( i, colmn ) )
                    );
                    if( uiName )
                    {
                        add.KspSymbol.uiVariableName = uiName.substr( 1 ) // [0] == variable type character
                    }

                    add.setKspSymbolValue( i, colmn, isConst, isUI, KSPSymbolType.CALLBACK );
                    result.push( add );
                    continue;
                }
            } //~callback
            //-----------------------------------------------------------------
            // check user function ( function #### )
            //-----------------------------------------------------------------
            {
                var DECLARE_REGEX = /^\s*(function\s+)([a-zA-Z0-9_]+)/g;

                var text  = document.lineAt( i ).text;
                var match = DECLARE_REGEX.exec( text );
                if( match )
                {
                    var symKind: vscode.SymbolKind  = vscode.SymbolKind.Function;
                    var name : string               = match[ 2 ];
                    var containerName : string      = "Function";
                    var colmn : number              = text.indexOf( name );

                    var add = new KSPSymbolInformation(
                        name,
                        symKind, containerName,
                        new vscode.Location( document.uri, new vscode.Position( i, colmn ) )
                    );

                    add.setKspSymbolValue( i, colmn, isConst, isUI, KSPSymbolType.USER_FUNCTION );
                    result.push( add );
                    continue;
                }
            } //~function
        }

        return result;
    }
}
