# encode: utf-8

import xlrd
import KspExcelUtil
import re
import sys

ARGV = sys.argv[1:]

veriftVariable = False

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

if( len( ARGV ) > 1 and ARGV[ 1 ] == "-v" ):
    veriftVariable = True

rowLength   = sheet.nrows

f       = open( ARGV[ 0 ], "r" )
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

    if( veriftVariable ):
        if( re.match( re.compile( r"^[\$|%|!|\~|@|\?]" ), name ) == None ):
            continue
        else:
            xlsxList.append( name )
    else:
        if( len( name ) == 0 or len( sig ) == 0 ):
            continue

    xlsxList.append( name )

# 1: check word in xlsx
setWordXlsx = set( wordList ) - set( xlsxList )
diffList    = list( setWordXlsx )
if( len( diffList ) > 0 ):
    print( REPORT_TITLE.format( title = "Not found in xlsx compare with " + ARGV[ 0 ] ) )
    for i in diffList:
        print( i )

# 2: check xlsx from word
setXlsxWord = set( xlsxList ) - set( wordList )
diffList    = list( setXlsxWord )
if( len( diffList ) > 0 ):
    print( REPORT_TITLE.format( title = "Not found in " + ARGV[ 0 ] + " compare with xlsx" ) )
    for i in diffList:
        print( i )

# 3 merged
print( REPORT_TITLE.format( title = "Merged" ) )
merged = wordList #[ x for sublist in [ wordList, xlsxList ] for x in sublist ]
for i in xlsxList:
    if( not i in merged ):
        merged.append( i )

if( veriftVariable != True ):
    merged += EXCEPT_COMMANDS

print( "Output merged text > __mergeed__.txt" )
f = open( "__mergeed__.txt", "w" )
for i in sorted( merged ):
    f.write( i + "\n" )
f.close()
