# encode: utf-8

import re

INPUT    = "KSP Reference Manual.txt"

# NOTE:
# KSP Reference Manual.txt created by Acrobat DC (Windows version & locale cp932) and re-save on vscode as utf-8 encoding.
# If created by different locale, change encoding name.
ENCODING = 'utf-8'

REGEX    = r"[a-z|A-Z|_]+\(\).*"
wordList = []

f = open( INPUT, 'r', encoding = ENCODING )

print( "TODO: A character \"/\" replace manually" )

line = f.readline()
while( line ):
    line = f.readline()
    m = re.match( re.compile( REGEX ), line )
    if( m == None ):
        continue

    word = m.group( 0 )
    word = re.sub( r"^\s*", "", word )
    word = re.sub( r"\s*$", "", word )
    word = re.sub( r".*?\s+.*", "", word )

    if( len( word ) == 0 ):
        continue

    if( not word in wordList ):
        wordList.append( word )

f.close()

for i in wordList:
    print( i )
