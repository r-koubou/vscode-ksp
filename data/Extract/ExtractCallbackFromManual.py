# encode: utf-8

import re

INPUT = "KSP Reference Manual.txt"

REGEX    = r"on\s+[a-z|A-Z|_]+"
wordList = []

f = open( INPUT )

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

    if( len( word ) == 0 ):
        continue

    if( not word in wordList ):
        wordList.append( word )

f.close()

for i in wordList:
    print( i )
