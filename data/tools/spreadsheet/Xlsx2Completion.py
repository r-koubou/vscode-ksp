import os
import sys
import json

# http://pypi.python.org/pypi/xlrd
import xlrd

import util

def append_snippet_to_json( target: dict, name: str, snippet: str, signature: str, desc: str ):
    if name in target:
        raise ValueError(f"Duplicate snippet name: {name}")

    target[ name ] = {
        "snippet_string": snippet,
        "signature": signature,
        "description": desc
    }

def convert( input, output, sheet_name ):

    HEADER = """//
// Generated by Xlsx2Completion.py
//
export const CompletionList ="""

    FOOTER = ";"

    output_json_data = {}
    book = xlrd.open_workbook( input )

    with open( output, 'w', encoding="utf-8", newline="\n" ) as fw:

        sheet     = book.sheet_by_name( sheet_name )
        rowLength = sheet.nrows

        for row in range( 1, rowLength ):
            name    = util.get_cell_by_colmn( sheet, row, util.HEADER_COMPLETE_NAME ).value.strip()
            snippet = util.get_cell_by_colmn( sheet, row, util.HEADER_SNIPPET_BODY ).value.strip()
            sig     = util.get_cell_by_colmn( sheet, row, util.HEADER_COMPLETE_SIG ).value.strip()
            desc    = util.get_cell_by_colmn( sheet, row, util.HEADER_DESCRIPTION ).value.strip()

            desc = util.append_newline( desc )
            snippet = util.append_newline( snippet )

            if( len( name ) == 0 or name.startswith( "*" ) ):
                continue

            sig  = sig.replace( "( ", "(" )
            sig  = sig.replace( " )", ")" )
            sig  = sig.replace( ", ", "," )

            append_snippet_to_json( output_json_data, name, snippet, sig, desc )


        fw.write( f"{HEADER}\n" )
        fw.write( json.dumps( output_json_data, indent = 4 ) )
        fw.write( f"{FOOTER}\n" )

        print( "Done: " + output )


if __name__ == "__main__":
    argv = sys.argv[1:]

    if( len(argv) < 2 ):
        print( "Usage: Xlsx2Completion.py <xlsx file> <output dir>" )
        sys.exit(1)

    xlsx_path  = argv[0]
    output_dir = argv[1]

    convert_list = [
        # 0: Input
        # 1: Target
        # 2: Sheet name
        [ xlsx_path, os.path.join( output_dir, "KSPCompletionCommand.ts" ),   "Command" ],
        [ xlsx_path, os.path.join( output_dir, "KSPCompletionVariable.ts" ),   "Variable" ],
    ]
    for i in convert_list:
        convert( i[0], i[1], i[2] )
