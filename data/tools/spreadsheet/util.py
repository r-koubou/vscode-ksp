# http://pypi.python.org/pypi/xlrd
import xlrd

HEADER_SNIPPET_NAME     = "Snippet:name"
HEADER_SNIPPET_PREFIX   = "Snippet:prefix"
HEADER_SNIPPET_BODY     = "Snippet:body"
HEADER_COMPLETE_NAME    = "Complete:name"
HEADER_COMPLETE_SIG     = "Complete:signature"
HEADER_DESCRIPTION      = "description"


"""
Get a Cell from given sheet instanse and row index and cell string value of Row 1.
"""
def get_cell_by_colmn( sheet: xlrd.sheet.Sheet, rowIndex: int, colmn_name: str ) -> xlrd.sheet.Cell:

    for c in range( sheet.ncols ):
        cell = sheet.cell( 0, c )
        name = cell.value
        if( name == colmn_name ):
            return sheet.cell( rowIndex, c )

    return None

"""
All new line will be replaced to escaped (\\n)
"""
def append_newline(text: str) -> str:
    array = text.split( "\n" )
    if( len( array ) > 1 ):
        tmp = ""
        for i in array:
            tmp += i + "\\n"
        text = tmp

    return text
