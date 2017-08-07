/* =========================================================================

    KSPSyntaxUtil.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import vscode = require( 'vscode' );

export const BASIC_KEYWORDS : string[] = [
    "on",
    "end",
    "function",
    "declare",
    "const",
    "polyphonic",
    "if",
    "else",
    "select",
    "case",
    "to",
    "while",
    "mod",
    "and",
    "or",
    "not",
    ".and.",
    ".or.",
    ".not.",
    "call",
]

export const REGEXP_DECIMAL     : RegExp = /\b(0|[1-9][0-9]*)/;
export const REGEXP_HEXADECIMAL : RegExp = /\b9[0-9a-fA-F]h/;
export const REGEXP_REAL        : RegExp = /\b(0|[1-9][0-9]*)\.[0-9]*/;
export const REGEXP_STRING      : RegExp = /"[^"]+"/;

export default class KSPSyntaxUtil
{
    private constructor(){}

    static matchKeyword( text: string ) : boolean
    {
        console.log( "matchKeyword: " + ( BASIC_KEYWORDS.indexOf( text ) != -1 ) );
        return BASIC_KEYWORDS.indexOf( text ) != -1;
    }

    static matchDecimal( text: string ) : boolean
    {
        console.log( "matchDecimal: " + ( text.match( REGEXP_DECIMAL ) != null ) );
        return text.match( REGEXP_DECIMAL ) != null;
    }

    static matchHexadecimal( text: string ) : boolean
    {
        console.log( "matchHexadecimal: " + ( text.match( REGEXP_HEXADECIMAL ) != null ) );
        return text.match( REGEXP_HEXADECIMAL ) != null;
    }

    static matchReal( text: string ) : boolean
    {
        console.log( "matchReal: " + ( text.match( REGEXP_REAL ) != null ) );
        return text.match( REGEXP_REAL ) != null;
    }

    static matchString( text: string ) : boolean
    {
        console.log( "matchString: " + ( text.match( REGEXP_STRING ) != null ) );
        return text.match( REGEXP_STRING ) != null;
    }

    static matchLiteral( text: string ) : boolean
    {
        return KSPSyntaxUtil.matchDecimal( text ) ||
               KSPSyntaxUtil.matchHexadecimal( text ) ||
               KSPSyntaxUtil.matchReal( text ) ||
               KSPSyntaxUtil.matchString( text );
    }

}
