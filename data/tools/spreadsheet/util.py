# encoding: utf-8

# http://pypi.python.org/pypi/xlrd
import xlrd

HEADER_SNIPPET_NAME     = "Snippet:name"
HEADER_SNIPPET_PREFIX   = "Snippet:prefix"
HEADER_SNIPPET_BODY     = "Snippet:body"
HEADER_COMPLETE_NAME    = "Complete:name"
HEADER_COMPLETE_SIG     = "Complete:signature"
HEADER_DESCRIPTION      = "description"

"""
Get a Cell array from given sheet instanse and cell string value of Row 1.
"""
def getRowsFromComnName( sheet, colmnName ):
    for c in range( sheet.ncols ):
        name = sheet.cell( 0, c ).strip()
        if( name == colmnName ):
            return sheet.col_slice( 1, c )
    return []

"""
Get a Cell from given sheet instanse and row index and cell string value of Row 1.
"""
def getCellFromColmnName( sheet, rowIndex, colmnName ):

    colmnName = colmnName

    for c in range( sheet.ncols ):
        cell = sheet.cell( 0, c )
        name = cell.value
        if( name == colmnName ):
            return sheet.cell( rowIndex, c )

    return ""

"""
All new line will be replaced to escaped (\\n)
"""
def append_newline(text):
    array = text.split( "\n" )
    if( len( array ) > 1 ):
        tmp = ""
        for i in array:
            tmp += i + "\\n"
        text = tmp

    return text
