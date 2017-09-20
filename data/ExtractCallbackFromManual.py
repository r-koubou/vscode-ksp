# coding: utf-8

import re
import ExtractManualNameConfig

INPUT = ExtractManualNameConfig.name

# NOTE:
# KSP Reference Manual.txt created by Acrobat DC (Windows version & locale cp932) and re-save on vscode as utf-8 encoding.
# If created by different locale, change encoding name.
ENCODING = 'utf-8'

REGEX    = r"on\s+[a-z|A-Z|_]+"
wordList = []

IGNORE_WORD_LIST = [
]

def appendWord( word, targetList ):
    if( not word in IGNORE_WORD_LIST and not word in targetList ) :
        targetList.append( word )


f = open( INPUT, 'r', encoding = ENCODING )

line = f.readline()
while( line ):
    line = f.readline()
    m = re.match( re.compile( REGEX ), line )
    if( m == None ):
        continue

    word = m.group( 0 )
    word = re.sub( r"^\s*", "", word )
    word = re.sub( r"\s*$", "", word )

    if( len( word ) == 0 ):
        continue

    appendWord( word, wordList )

f.close()

for i in wordList:
    print( i )
