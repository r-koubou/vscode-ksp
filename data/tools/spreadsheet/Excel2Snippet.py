import os.path
import sys
import string

# http://pypi.python.org/pypi/xlrd
import xlrd

import KspExcelUtil

THIS_SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
TEMPLATE_DIR    = os.path.join(THIS_SCRIPT_DIR, "template", "Excel2Snippet")

def double_quote_escape( text ):
    return text.replace( "\"", "\\\"" )

def convert( xlsx_path, output_dir ):

    output_path = os.path.join( output_dir, "ksp.json" )

    template_header = string.Template( open( os.path.join( TEMPLATE_DIR, "template_header.txt" ) ).read() )
    template_footer = string.Template( open( os.path.join( TEMPLATE_DIR, "template_footer.txt" ) ).read() )
    template      = string.Template( open( os.path.join( TEMPLATE_DIR, "template.txt" ) ).read() )
    template_body = string.Template( open( os.path.join( TEMPLATE_DIR, "template_body.txt" ) ).read() )

    book       = xlrd.open_workbook( xlsx_path )
    sheetNames = book.sheet_names()

    with open( output_path, 'w' ) as fw:

        fw.write( template_header.substitute() )

        sheet_length = len( sheetNames )
        for idx in range( sheet_length ):

            sheet = book.sheet_by_index( idx )

            row_length = sheet.nrows
            for row in range( 1, row_length ):
                name   = KspExcelUtil.getCellFromColmnName( sheet, row, KspExcelUtil.HEADER_SNIPPET_NAME ).value.strip()
                prefix = KspExcelUtil.getCellFromColmnName( sheet, row, KspExcelUtil.HEADER_SNIPPET_PREFIX ).value.strip()
                body   = KspExcelUtil.getCellFromColmnName( sheet, row, KspExcelUtil.HEADER_SNIPPET_BODY ).value.strip()
                desc   = KspExcelUtil.getCellFromColmnName( sheet, row, KspExcelUtil.HEADER_DESCRIPTION ).value.strip()

                descArray = desc.split( "\n" )
                if( len( descArray ) > 1 ):
                    tmp = ""
                    for i in descArray:
                        tmp += i + "\\n"
                    desc = tmp

                desc = double_quote_escape( desc )

                if( len( prefix ) == 0 or len( body ) == 0 ):
                    continue

                bodyArray    = body.split( "\n" )
                bodyArrayLen = len( bodyArray )
                tmp          = ""
                if( bodyArrayLen > 1 ):
                    for i in range( bodyArrayLen ):
                        tmp += template_body.substitute( body = double_quote_escape( bodyArray[ i ] ) )
                        if( i + 1 < bodyArrayLen ):
                            tmp += ",\n"
                    body = tmp
                else:
                    body = template_body.substitute( body = double_quote_escape( body ) )

                text = template.substitute(
                    name        = name,
                    prefix      = prefix,
                    body        = body,
                    description = desc,
                    # 最後のJSONオブジェクトの場合はカンマを付けない
                    comma       = '' if idx == sheet_length - 1 and row == row_length - 1 else ','
                )
                fw.write( text )

        fw.write( "\n" )
        fw.write( template_footer.substitute() )
        fw.write( "\n" )
        print( "Done: " + output_path )


if __name__ == "__main__":

    argv = sys.argv[1:]

    if( len( argv ) < 2 ):
        print( "Usage: Excel2Snippet.py <xlsx file > <output dir>" )
        sys.exit( 1 )

    convert( argv[0], argv[1] )
