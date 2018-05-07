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
    VARIABLE_TYPE_BEGIN,
    VARIABLE_INTEGR = VARIABLE_TYPE_BEGIN,
    VARIABLE_REAL,
    VARIABLE_STRING,
    VARIABLE_INTEGR_ARRAY,
    VARIABLE_REAL_ARRAY,
    VARIABLE_STRING_ARRAY,
    VARIABLE_TYPE_END = VARIABLE_STRING_ARRAY,

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
        let ret = this.name;
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
        for( let i = position.character - 1; i >= 0; i-- )
        {
            let regex : RegExp = new RegExp( KSPSymbolUtil.REGEX_SYMBOL_BOUNDARY );
            let char  = lineText.charAt( i );
            let match = regex.exec( char );
            if( match )
            {
                return i + 1;
            }
        }
        return position.character;
    }

    static endAt( lineText:string, position:vscode.Position ) : number
    {
        for( let i = position.character + 1; i < lineText.length; i++ )
        {
            let regex : RegExp = new RegExp( KSPSymbolUtil.REGEX_SYMBOL_BOUNDARY );
            let char  = lineText.charAt( i );
            let match = regex.exec( char );
            if( match )
            {
                return i - 1;
            }
        }
        return position.character;
    }

    static parseSymbolAt( document: vscode.TextDocument, position: vscode.Position ) : string
    {
        let textLine : vscode.TextLine = document.lineAt( position.line );
        let line   : string = textLine.text;
        let eolPos : number = line.length;
        let symbol : string = "";
        for( let i = position.character; i < eolPos; i++ )
        {
            let regex : RegExp = KSPSymbolUtil.REGEX_SYMBOL_BOUNDARY;
            let char  = line.charAt( i );
            let match = regex.exec( char );
            if( match )
            {
                if( char == '"' )
                {
                    // Literal String
                    symbol += '"';
                }
                break;
            }
            symbol += char;
        }
        for( let i = position.character - 1; i >= 0; i-- )
        {
            let regex : RegExp = KSPSymbolUtil.REGEX_SYMBOL_BOUNDARY;
            let char  = line.charAt( i );
            let match = regex.exec( char );
            if( match )
            {
                if( char == '"' )
                {
                    // Literal String
                    symbol = '"' + symbol;
                }
                break;
            }
            symbol = char + symbol;
        }
        return symbol.trim();
    }

    static collect( document: vscode.TextDocument, endLineNumber: number = -1 ) : KSPSymbolInformation[]
    {
        let result: KSPSymbolInformation[] = [];

        let count = document.lineCount;
        if( endLineNumber >= 0 )
        {
            count = endLineNumber;
        }
        for( let i = 0; i < count; i++ )
        {
            let isConst: boolean = false;
            //-----------------------------------------------------------------
            // check declare variables
            //-----------------------------------------------------------------
            {
                let DECLARE_REGEX = /^\s*declare\s+(ui_[a-zA-Z0-9_]+|const)?\s*([\$%~\?@!][a-zA-Z0-9_]+)/g;

                let text  = document.lineAt( i ).text;
                let match = DECLARE_REGEX.exec( text );
                if( match )
                {
                    isConst                         = match[ 1 ] && match[ 1 ].toString() == "const";
                    let isUI                        = match[ 1 ] && match[ 1 ].startsWith( "ui_" );
                    let symKind: vscode.SymbolKind  = vscode.SymbolKind.Variable;
                    let name : string               = match[ 2 ];
                    let containerName : string      = "Variable";
                    let colmn : number              = text.indexOf( name );

                    if( isConst )
                    {
                        containerName = "Constant Variable";
                        symKind = vscode.SymbolKind.Constant;
                    }

                    let variableTypeChar            = name.charAt( 0 );
                    let variableType : string       = "(" + KSPSymbol.variableTypeChar2String( variableTypeChar ) + ")";
                    let symbolType : KSPSymbolType  = KSPSymbol.variableTypeChar2Type( variableTypeChar );

                    if( isUI )
                    {
                        containerName = "UI Variable";
                        variableType  = "(" + match[ 1 ].trim() + ")";
                    }

                    let add = new KSPSymbolInformation(
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
                let DECLARE_REGEX = /^\s*(on\s+)([a-zA-Z0-9_]+)(\s*\(\s*[^\)]+\s*\))?/g;

                let text  = document.lineAt( i ).text;
                let match = DECLARE_REGEX.exec( text );
                if( match )
                {
                    let isUI                        = match[ 2 ] != undefined && match[ 3 ] != undefined && match[ 2 ].startsWith( "ui_" );
                    let uiName                      = null;
                    let symKind: vscode.SymbolKind  = vscode.SymbolKind.Function;
                    let name : string               = match[ 2 ];
                    let containerName : string      = "Callback";
                    let colmn : number              = text.indexOf( name );

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

                    let add = new KSPSymbolInformation(
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
                let DECLARE_REGEX = /^\s*(function\s+)([a-zA-Z0-9_]+)/g;

                let text  = document.lineAt( i ).text;
                let match = DECLARE_REGEX.exec( text );
                if( match )
                {
                    let symKind: vscode.SymbolKind  = vscode.SymbolKind.Function;
                    let name : string               = match[ 2 ];
                    let containerName : string      = "Function";
                    let colmn : number              = text.indexOf( name );

                    let add = new KSPSymbolInformation(
                        name,
                        symKind, containerName,
                        new vscode.Location( document.uri, new vscode.Position( i, colmn ) )
                    );

                    add.setKspSymbolValue( i, colmn, isConst, false, KSPSymbolType.USER_FUNCTION );
                    result.push( add );
                    continue;
                }
            } //~function
        }

        return result;
    }
}
