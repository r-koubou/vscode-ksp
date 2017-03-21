# encode: utf-8

import xlrd
import KspExcelUtil
import sys

REPORT_TITLE = "#---------------- {title} ----------------"

book  = xlrd.open_workbook( 'KSP.xlsx' )
sheet = book.sheet_by_index( 0 )

wordList    = []
xlsxList    = []

# Builtin-Commands but no arguments
EXCEPT_COMMANDS = [
    "exit",
    "ignore_controller",
    "reset_ksp_timer",
    "make_perfview",
    "ignore_midi",
]

rowLength   = sheet.nrows

f       = open( sys.argv[ 1 ], "r" )
line    = f.readline()
while line:
    word = line.strip()
    if( not word in wordList ):
        wordList.append( word )

    line = f.readline()
f.close()

for row in range( 1, sheet.nrows ):
    found = False
    name  = KspExcelUtil.getCellFromColmnName( sheet, row, KspExcelUtil.HEADER_COMPLETE_NAME ).value.strip()
    sig   = KspExcelUtil.getCellFromColmnName( sheet, row, KspExcelUtil.HEADER_COMPLETE_SIG ).value.strip()

    if( len( name ) == 0 or len( sig ) == 0 ):
        continue
    xlsxList.append( name )

# 1: check word in xlsx
setWordXlsx = set( wordList ) - set( xlsxList )
diffList    = list( setWordXlsx )
if( len( diffList ) > 0 ):
    print( REPORT_TITLE.format( title = "Not found in xlsx compare with " + sys.argv[ 1 ] ) )
    for i in diffList:
        print( i )

# 2: check xlsx from word
setXlsxWord = set( xlsxList ) - set( wordList )
diffList    = list( setXlsxWord )
if( len( diffList ) > 0 ):
    print( REPORT_TITLE.format( title = "Not found in " + sys.argv[ 1 ] + " compare with xlsx" ) )
    for i in diffList:
        print( i )

# 3 merged
print( REPORT_TITLE.format( title = "Merged" ) )
merged = wordList #[ x for sublist in [ wordList, xlsxList ] for x in sublist ]
for i in xlsxList:
    if( not i in merged ):
        merged.append( i )

merged += EXCEPT_COMMANDS

for i in sorted( merged ):
    print( i )
