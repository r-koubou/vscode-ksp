from typing import List
from abc import ABC, abstractmethod

import xlrd
from natsort import natsorted

import util

class VerifyExtractedBase( ABC ):

    def __init__(self, xlsx_path: str, extraxted_txt_path: str, output_path) -> None:
        self.xlsx_path          = xlsx_path
        self.extraxted_txt_path = extraxted_txt_path
        self.output_path        = output_path
        self.extracted_words: List[str] = []
        self.xlsx_words: List[str]      = []

        self.xlsx_book          = xlrd.open_workbook( xlsx_path )
        self.xlsx_sheet_names   = self.xlsx_book.sheet_names()

        with open( extraxted_txt_path, "r", encoding="utf-8" ) as f:
            self.extracted_words = [ line.strip() for line in f.readlines() ]
            self.extracted_words = [ i for i in self.extracted_words if len( i ) > 0 ]

            unique_words = dict.fromkeys( self.extracted_words )
            self.extracted_words = list( unique_words.keys() )

    def process(self) -> None:
        for i in range( len( self.xlsx_sheet_names ) ):
                sheet = self.xlsx_book.sheet_by_index( i )
                for row in range( 1, sheet.nrows ):
                    name = util.get_cell_by_colmn( sheet, row, util.HEADER_COMPLETE_NAME ).value.strip()
                    sig  = util.get_cell_by_colmn( sheet, row, util.HEADER_COMPLETE_SIG ).value.strip()

                    if( self.parse_data( name, sig ) ):
                        self.xlsx_words.append( name )

        self._verify()

    @abstractmethod
    def parse_data(self, name: str, signature: str) -> bool:
        raise NotImplementedError()

    def get_additional_words(self) -> List[str]:
        return []

    def _verify(self) -> None:
        # 1: check word in xlsx
        xlsx_words_set = set( self.extracted_words ) - set( self.xlsx_words )
        diff_list      = list( xlsx_words_set )
        if( len( diff_list ) > 0 ):
            print( f"#---------------- Not found in xlsx compare with `{self.extraxted_txt_path}` ----------------" )
            for i in diff_list:
                print( i )

        # 2: check xlsx from word
        xlsx_words_set = set( self.xlsx_words ) - set( self.extracted_words )
        diff_list      = list( xlsx_words_set )
        if( len( diff_list ) > 0 ):
            print( f"#---------------- Not found in `{self.extraxted_txt_path}` compare with xlsx ----------------" )
            for i in diff_list:
                print( i )

        # 3 merged
        print( f"#---------------- Merged ----------------" )
        merged_list = list( self.extracted_words )
        for i in self.xlsx_words:
            if( not i in merged_list ):
                merged_list.append( i )

        merged_list += self.get_additional_words()

        # Output merged text
        print( f"Output merged text to `${self.output_path}`" )
        with open( self.output_path, "w", encoding="utf-8" ) as f:
            for i in natsorted( merged_list ):
                f.write( f"{i}\n" )
