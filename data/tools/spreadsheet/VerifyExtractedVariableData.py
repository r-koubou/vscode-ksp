import sys
import re
from typing import List

from VerifyExtractedBase import VerifyExtractedBase

class VerifyExtractedVariableData(VerifyExtractedBase):

    def __init__(self, xlsx_path: str, extraxted_txt_path: str, output_path: str) -> None:
        super().__init__( xlsx_path, extraxted_txt_path, output_path )

    def parse_data(self, name: str, _: str) -> bool:
        if( re.match( re.compile( r"^[\$|%|!|\~|@|\?]" ), name ) == None ):
            return False
        return True

    def get_additional_words(self) -> List[str]:
        return []


if __name__ == "__main__":
    argv = sys.argv[1:]

    if( len(argv) < 3 ):
        print( "Usage: VerifyExtractedVariableData.py <xlsx file> <extracted text file> <output file>" )
        sys.exit(1)

    xlsx_path          = argv[0]
    extracted_txt_path = argv[1]
    output_path        = argv[2]

    VerifyExtractedVariableData( xlsx_path, extracted_txt_path, output_path ).process()
