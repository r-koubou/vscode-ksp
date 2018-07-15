/* =========================================================================

    KSPDocumentSymbolProvider.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import vscode = require( 'vscode' );
import * as path from 'path';

import { KSPSymbolUtil, KSPSymbolInformation, KSPSymbolType, KSPSymbol } from './KSPSymbolUtil';

export class KSPDocumentSymbolProvider implements vscode.DocumentSymbolProvider
{
    constructor()
    {
    }

    public provideDocumentSymbols(
        document: vscode.TextDocument,
        token: vscode.CancellationToken ) : Thenable<vscode.DocumentSymbol[]>
    {
        const filaName = path.basename( document.fileName );
        let result: vscode.DocumentSymbol[]     = [];
        const symbols: KSPSymbolInformation[]   = KSPSymbolUtil.collect( document );

        let createSymbol = function( v:KSPSymbolInformation, kind: vscode.SymbolKind )  {
            let range: vscode.Range          = v.location.range;
            let selectionRange: vscode.Range = v.location.range;
            if( v.range && v.selectionRange )
            {
                range           = v.range;
                selectionRange = v.selectionRange;
            }
            return new vscode.DocumentSymbol( v.name,
                v.KspSymbol.description,
                kind,
                range,
                selectionRange )
        }

        for( const v of symbols )
        {
            const ksp = v.KspSymbol;
            //------------------------------------------------------------------
            // Callback
            //------------------------------------------------------------------
            if( ksp.kspSymbolType == KSPSymbolType.CALLBACK )
            {
                //v.name = "on " + v.name;
                if( ksp.isUI )
                {
                    v.name += " : " + ksp.uiVariableName + " (" + ksp.uiVariableType + ")";
                }
                result.push( createSymbol( v, vscode.SymbolKind.Event ) );
            }
            //------------------------------------------------------------------
            // Variable
            //------------------------------------------------------------------
            if( ksp.kspSymbolType >= KSPSymbolType.VARIABLE_TYPE_BEGIN && ksp.kspSymbolType <= KSPSymbolType.VARIABLE_TYPE_END )
            {
                let sym  = createSymbol( v, vscode.SymbolKind.Variable );
                sym.name = KSPSymbol.variableType2Char( ksp.kspSymbolType ) + sym.name;

                if( ksp.isPolyphonic )
                {
                    sym.name += ": " + ksp.variableTypeName + ", Polyphonic";
                }
                else if( ksp.isUI )
                {
                    sym.name += ": " + ksp.variableTypeName;
                }
                else
                {
                    sym.name += ": " + ksp.variableTypeName;
                }

                if( ksp.isConst )
                {
                    sym.kind = vscode.SymbolKind.Constant;
                }
                else if( KSPSymbol.isArrayVariable( ksp.kspSymbolType ) )
                {
                    sym.kind = vscode.SymbolKind.Array;
                }

                result.push( sym );
            }
            //------------------------------------------------------------------
            // User Fumction
            //------------------------------------------------------------------
            if( ksp.kspSymbolType == KSPSymbolType.USER_FUNCTION )
            {
                //v.name = "function " + v.name;
                result.push( createSymbol( v, vscode.SymbolKind.Function ) );
            }
        }
        return Promise.resolve( result );
    }
}
