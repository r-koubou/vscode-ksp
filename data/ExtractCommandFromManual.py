# coding: utf-8

import re
from natsort import natsorted
import ExtractManualNameConfig

INPUT = ExtractManualNameConfig.name

# NOTE:
# KSP Reference Manual.txt created by Acrobat DC (Windows version & locale cp932) and re-save on vscode as utf-8 encoding.
# If created by different locale, change encoding name.
ENCODING = 'utf-8'

REGEX    = r"[a-z|A-Z|_]+\(\).*"
REGEX2   = r"(.*)\/(.*)"
wordList = []

IGNORE_WORD_LIST = [
    "select",
    "while",
    "ui_waveform",
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

    word = m.group( 0 ).strip()
    word = re.sub( r".*?\s+.*", "", word )
    word = re.sub( r"\s*\(\s*", "", word )
    word = re.sub( r"\s*\)\s*", "", word )

    if( len( word ) == 0 ):
        continue

    m = re.match( re.compile( REGEX2 ), word )
    if( m == None ):
        appendWord( word, wordList )
    else:
        for i in m.groups():
            appendWord( i.strip(), wordList )

f.close()

for i in natsorted( wordList ):
    print( i )
