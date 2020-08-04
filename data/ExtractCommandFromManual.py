# coding: utf-8

import sys
import re
from natsort import natsorted

INPUT = sys.argv[ 1 ]

# NOTE: This script for Kontakt6!
# KSP Reference Manual.txt created by Acrobat DC (Windows version & locale cp932) and re-save on vscode as utf-8 encoding.
# If created by different locale, change encoding name.
ENCODING = 'utf-8'

REGEX    = r"(\s*[a-z|A-Z|_]+\(\)\s*)+"
wordList = []

IGNORE_WORD_LIST = [
    "select",
    "while",
    "ui_waveform",
    # in Explain, Examples
    "array",
    "by_mark", # miss?
    "change_xxx",
    "get_keyrange_xxx",
    "if",
    "it",
    "low_group",
    "ray_idx",
    "set_condition", #lower
    "range", # not exist
    # Interrupted word
    "group",
    "idx",
    "par_str_arr",
    "tach_zone", # line separated (-> attach_zone() )
    "ui_control", # It is callback
    "trol_par_str_arr", # extract miss (expexted: control_par_str_arr)
]

def appendWord( word, targetList ):
    if( len( word ) > 0 and not word in IGNORE_WORD_LIST and not word in targetList ) :
        targetList.append( word )


f = open( INPUT, 'r', encoding = ENCODING )

while( True ):
    line = f.readline()
    if( not line ):
        break

    m = re.findall( REGEX, line )

    if( m == None ):
        continue

    for i in m:
        word = i.strip()
        word = re.sub( r".*?\s+.*", "", word )
        word = re.sub( r"\s*\(\s*", "", word )
        word = re.sub( r"\s*\)\s*", "", word )

        if word.find( "-" ) >= 0:
            continue

        appendWord( word.lower(), wordList )

f.close()

for i in natsorted( wordList ):
    print( i )
