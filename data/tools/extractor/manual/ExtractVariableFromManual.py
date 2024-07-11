import re
import sys

from typing import List

from base import ExtractBase
from base import Word

class ExtractVariableFromManual(ExtractBase):

    _REGEX = r"(\s*[\$|%|!|\~|@|\?][A-Z]+[A-Z|_|0-9|^-]*\s*)+"

    def __init__(self, manual_path: str, output_path: str, output_dump_path: str) -> None:
        super().__init__(manual_path, output_path, output_dump_path)

    def get_ignored_word_list(self) -> List[str]:
        return [
            "$MARK_1",
            "$MARK_2",
            "$MARK_28",
            "$KEY_COLOR_FUCHSI",
            "$CONTROL_PAR_KEY_C",
            # in Sample Code
            "$CUSTOM_EVENT_PAR_4",
            "$HEADER_SIZE",
            "$NUM_SLIDES",
            "$SIZE",
            "$VE",
            "$ARRAY_SIZE",
            "$CHANNEL_L",
            "$CHANNEL_R",
            # Interrupted word
            "$CON",
            "$SIGNA",
            "$NI_DETECT_INSTRU",
            # Beta version only (removed)
            "$EVENT_PAR_MOD_VALUE_ID_FULL",
        ]

    def parse_line(self, line_no: int, line: str) -> None:
        if line.find( "declare" ) >= 0 or line.find( ":=" ) >= 0:
            return

        m = re.findall( ExtractVariableFromManual._REGEX, line )

        if( m == None ):
            return

        for i in m:
            word = i.strip()
            word = word.replace( "/", "" )
            word = re.sub( r"^.\w$", "", word )
            word = re.sub( r"^.\n",  "", word )

            if( word.endswith( "_" ) or word.find( "-" ) >= 0 ):
                continue

            self.appendWord( Word( line_no, word ) )

    def post_parse(self) -> None:
        for i in range( 1, 29 ):
            self.words.append( Word(0, f"$MARK_{i}"))

if __name__ == "__main__":
    if( len( sys.argv ) < 4 ):
        print( "Usage: ExtractVariableFromManual.py <Manual Path> <Output Path> <Output Dump Path>" )
        sys.exit( 1 )

    manual_path     = sys.argv[1]
    output_path     = sys.argv[2]
    output_dump_path= sys.argv[3]

    ExtractVariableFromManual( manual_path, output_path, output_dump_path ).execute()
