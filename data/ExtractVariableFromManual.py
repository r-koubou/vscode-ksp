# coding: utf-8

import re
import sys
from natsort import natsorted

INPUT = sys.argv[ 1 ]

# NOTE: This script for Kontakt6!
# KSP Reference Manual.txt created by Acrobat DC (Windows version & locale cp932) and re-save on vscode as utf-8 encoding.
# If created by different locale, change encoding name.
ENCODING = 'utf-8'

REGEX    = r"(\s*[\$|%|!|\~|@|\?][A-Z]+[A-Z|_|0-9|^-]*\s*)+"
wordList = []

IGNORE_WORD_LIST = [
"$MARK_1",
"$MARK_2",
"$MARK_28",
"$KEY_COLOR_FUCHSI",
# in Sample Code
"$HEADER_SIZE",
"$NUM_SLIDES",
"$SIZE",
"$VE",
"$ARRAY_SIZE",
]

def appendWord( word, targetList ):
    if( len( word ) > 0 and not word in IGNORE_WORD_LIST and not word in targetList ) :
        targetList.append( word )


f = open( INPUT, 'r', encoding = ENCODING )

while( True ):
    line = f.readline()
    if not line:
        break

    if line.find( "declare" ) >= 0 or line.find( ":=" ) >= 0:
        continue

    m = re.findall( REGEX, line )

    if( m == None ):
        continue

    for i in m:
        word = i.strip()
        word = word.replace( "/", "" )
        word = re.sub( r"^.\w$", "", word )
        word = re.sub( r"^.\n",  "", word )

        if( word.endswith( "_" ) or word.find( "-" ) >= 0 ):
            continue

        appendWord( word, wordList )

f.close()

for i in range( 1, 29 ):
    wordList.append( "$MARK_{i}".format( i = i ) )

for i in natsorted( wordList ):
    print( i )
