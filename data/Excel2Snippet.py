# encoding: utf-8

# http://pypi.python.org/pypi/xlrd
import xlrd

import KspExcelUtil

TARGET  = "../snippets/ksp.json"

TEMPLATE = """
    "{name}":
    {{
        "prefix": "{prefix}",
        "body":[
{body}
        ],
        "description": "{description}"
    }},"""

BODY_TEMPLATE = "            \"{body}\""

HEADER = """{
"##!---------------- Auto generated code: Generated by /data/Excel2Snippet.py ----------------##":{},
"## NOTE: Remove comma after last object if you re-Generated":{},
"""

FOOTER = "}"

book       = xlrd.open_workbook( 'KSP.xlsx' )
sheetNames = book.sheet_names()

fw = open( TARGET, 'w' )

fw.write( HEADER )

for idx in range( len( sheetNames ) ):

    sheet     = book.sheet_by_index( idx )
    rowLength = sheet.nrows

    for row in range( 1, sheet.nrows ):
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

        desc = desc.replace( "\"", "\\\"" )

        if( len( prefix ) == 0 or len( body ) == 0 ):
            continue

        bodyArray    = body.split( "\n" )
        bodyArrayLen = len( bodyArray )
        tmp          = ""
        if( bodyArrayLen > 1 ):
            for i in range( bodyArrayLen ):
                tmp += BODY_TEMPLATE.format( body = bodyArray[ i ].replace( "\"", "\\\"" ) )
                if( i + 1 < bodyArrayLen ):
                    tmp += ",\n"
            body = tmp
        else:
            body = BODY_TEMPLATE.format( body = body.replace( "\"", "\\\"" ) )

        text = TEMPLATE.format(
            name        = name,
            prefix      = prefix,
            body        = body,
            description = desc
        )
        fw.write( text )

fw.write( "\n" )
fw.write( FOOTER )
fw.write( "\n" )
fw.close()
print( "Done: " + TARGET )
