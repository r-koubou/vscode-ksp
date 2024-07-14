import re
import sys

from typing import List

from base import ExtractBase
from base import Word

class ExtractCallbackFromManual(ExtractBase):

    _REGEX = r"on\s+[a-z|A-Z|_]+"

    def __init__(self, manual_path: str, output_path: str, output_dump_path: str) -> None:
        super().__init__(manual_path, output_path, output_dump_path)

    def get_ignored_word_list(self) -> List[str]:
        return []

    def parse_line(self, line_no: int, line: str) -> None:
        m = re.match( re.compile( ExtractCallbackFromManual._REGEX ), line )
        if( m == None ):
            return

        word = m.group( 0 )
        word = re.sub( r"^\s*", "", word )
        word = re.sub( r"\s*$", "", word )

        if( len( word ) == 0 ):
            return

        self.appendWord( Word( line_no, word) )

if __name__ == "__main__":
    if( len( sys.argv ) < 4 ):
        print( "Usage: ExtractCallbackFromManual.py <Manual Path> <Output Path> <Output Dump Path>" )
        sys.exit( 1 )

    manual_path     = sys.argv[1]
    output_path     = sys.argv[2]
    output_dump_path= sys.argv[3]

    ExtractCallbackFromManual( manual_path, output_path, output_dump_path ).execute()
