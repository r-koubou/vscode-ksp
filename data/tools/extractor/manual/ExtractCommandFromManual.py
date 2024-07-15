import os
import re
import sys

from typing import List

from base import ExtractBase
from base import Word

THIS_SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))

class ExtractCommandFromManual(ExtractBase):

        _REGEX = r"(\s*[a-z|A-Z|_]+\(\)\s*)+"

        def __init__(self, manual_path: str, output_path: str, output_dump_path: str) -> None:
            super().__init__(manual_path, output_path, output_dump_path)

        def get_ignored_word_list(self) -> List[str]:
            return self.import_ignored_words( os.path.join( THIS_SCRIPT_DIR, 'IgnoreCommandWords.txt' ) )

        def parse_line(self, line_no: int, line: str) -> None:
            m = re.findall( ExtractCommandFromManual._REGEX, line )

            if( m == None ):
                return

            for i in m:
                word = i.strip()
                word = re.sub( r".*?\s+.*", "", word )
                word = re.sub( r"\s*\(\s*", "", word )
                word = re.sub( r"\s*\)\s*", "", word )

                if word.find( "-" ) >= 0:
                    continue

                self.appendWord( Word( line_no, word.lower() ) )

if __name__ == "__main__":
    if( len( sys.argv ) < 4 ):
        print( "Usage: ExtractCommandFromManual.py <Manual Path> <Output Path> <Output Dump Path>" )
        sys.exit( 1 )

    manual_path     = sys.argv[1]
    output_path     = sys.argv[2]
    output_dump_path= sys.argv[3]

    ExtractCommandFromManual( manual_path, output_path, output_dump_path ).execute()
