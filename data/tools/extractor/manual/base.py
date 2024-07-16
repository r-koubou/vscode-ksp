import re
from typing import List
from abc import ABC, ABCMeta, abstractmethod
from natsort import natsorted

class Word:
    def __init__(self, line_no: int, word: str) -> None:
        self.line_no    = line_no
        self.word       = word

class ExtractBase(metaclass = ABCMeta ):

    def __init__(self, manual_path: str, output_path: str, output_dump_path: str) -> None:
        self.manual_path                = manual_path
        self.output_path                = output_path
        self.output_dump_path           = output_dump_path
        self.words: List[Word]          = []
        self.ignored_words: List[str]   = self.get_ignored_word_list()

    def import_ignored_words(self, list_path:str ) -> List[str]:
        line_comment_regex     = r"^\s*#.*"
        line_end_comment_regex = r"^([^#]+)(#.*)"
        result = []
        with open( list_path, 'r' ) as f:
            for line in f.read().splitlines():
                line = line.strip()
                if(len( line ) == 0 or line.startswith("#")):
                    continue
                if re.match(line_comment_regex, line):
                    continue
                m = re.match(line_end_comment_regex, line)
                if m:
                    line = m.group(1).strip()
                result.append(line)

        return result

    @abstractmethod
    def get_ignored_word_list(self) -> List[str]:
        raise NotImplementedError

    @abstractmethod
    def parse_line(self, line_no: int, line: str) -> None:
        raise NotImplementedError

    def pre_parse(self) -> None:
        pass

    def post_parse(self) -> None:
        pass

    def appendWord( self, word: Word ) -> bool:
        if( len( word.word ) > 0 and not word.word in self.ignored_words and not word.word in self.words ) :
            self.words.append( word )
            return True
        return False


    def output_words(self) -> None:
        word_list = set([ i.word for i in self.words])

        with open( self.output_path, 'w', encoding = 'utf-8' ) as f:
            for i in natsorted( word_list ):
                f.write( f"{i}\n" )

    def output_dump(self) -> None:
        with open( self.output_dump_path, 'w', encoding = 'utf-8' ) as f:
            for i in self.words:
                f.write( f"line {i.line_no}:\t{i.word}\n" )

    def execute(self) -> None:
        self.pre_parse()
        with open( self.manual_path, 'r', encoding = 'utf-8' ) as f:
            line_no = 0
            while( True ):
                line = f.readline()
                line_no += 1
                if not line:
                    break

                self.parse_line( line_no, line )
        self.post_parse()
        self.output_words()
        self.output_dump()
