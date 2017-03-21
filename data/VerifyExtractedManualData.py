# encode: utf-8

import xlrd
import KspExcelUtil
import sys

f = open( sys.argv[ 1 ], "r" )

book  = xlrd.open_workbook( 'KSP.xlsx' )
sheet = book.sheet_by_index( 0 )

missingList = []

rowLength   = sheet.nrows
line        = f.readline()
while line:
    line = line.strip()
    for row in range( 1, sheet.nrows ):
        found = False
        name  = KspExcelUtil.getCellFromColmnName( sheet, row, KspExcelUtil.HEADER_COMPLETE_NAME ).value.strip()
        if( len( name ) == 0 ):
            continue

        if( name == line ):
            found = True
            break

    if( found == False ):
        missingList.append( line )

    line = f.readline()

f.close()

if( len( missingList ) > 0 ):
    print( "# Some words are not exist in KSP.xlsx" )
    for i in missingList:
        print( i )
