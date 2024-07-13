import os.path
import sys
import json

from typing import List

# http://pypi.python.org/pypi/xlrd
import xlrd

import util

def double_quote_escape( text ):
    return text

def append_snippet_to_json( target: dict, name: str, prefix: str, body: List[str], desc: str ):
    if name in target:
        raise ValueError(f"Duplicate snippet name: {name}")

    target[ name ] = {
        "prefix": prefix,
        "body": body,
        "description": desc
    }

def convert( xlsx_path, output_dir ):

    output_path = os.path.join( output_dir, "ksp.json" )
    book        = xlrd.open_workbook( xlsx_path )
    sheetNames  = book.sheet_names()

    output_json_data = {}

    with open( output_path, 'w' ) as fw:

        sheet_length = len( sheetNames )
        for idx in range( sheet_length ):

            sheet = book.sheet_by_index( idx )

            row_length = sheet.nrows
            for row in range( 1, row_length ):
                name   = util.get_cell_by_colmn( sheet, row, util.HEADER_SNIPPET_NAME ).value.strip()
                prefix = util.get_cell_by_colmn( sheet, row, util.HEADER_SNIPPET_PREFIX ).value.strip()
                body   = util.get_cell_by_colmn( sheet, row, util.HEADER_SNIPPET_BODY ).value.strip()
                desc   = util.get_cell_by_colmn( sheet, row, util.HEADER_DESCRIPTION ).value.strip()

                descArray = desc.split( "\n" )
                if( len( descArray ) > 1 ):
                    tmp = ""
                    for i in descArray:
                        tmp += i + "\\n"
                    desc = tmp

                if( len( prefix ) == 0 or len( body ) == 0 ):
                    continue

                bodyArray = body.split( "\n" )
                append_snippet_to_json( output_json_data, name, prefix, bodyArray, desc )

        fw.write( json.dumps( output_json_data, indent = 4 ) )
        print( "Done: " + output_path )


if __name__ == "__main__":

    argv = sys.argv[1:]

    if( len( argv ) < 2 ):
        print( "Usage: Xlsx2Snippet.py <xlsx file > <output dir>" )
        sys.exit( 1 )

    convert( argv[0], argv[1] )
