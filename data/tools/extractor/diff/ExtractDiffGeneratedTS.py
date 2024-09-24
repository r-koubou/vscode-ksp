import re
import sys

def process( patch_file_path:str, output_path ) -> None:

    ignore_regex_list = [
        r"^[^\+]+",
        r".*export const*"
    ]

    match_regex_list = [
        r"^\+\s+"
    ]

    extract_regex = r"^\+\s*\"([^\"]+)\""

    added_variable_lines = []


    with open( patch_file_path, 'r' ) as f:
        lines = f.readlines()

    for i in lines:
        for r in ignore_regex_list:
            if re.match( r, i ):
                break
        for r in match_regex_list:
            if re.match( r, i ):
                m = re.match( extract_regex, i )
                if m:
                    added_variable_lines.append( m.group(1) )
                else:
                    added_variable_lines.append( i )
                break

    added_variable_lines.sort()

    with open( output_path, 'w', encoding="utf-8", newline="\n" ) as f:
        f.write( "\n".join( added_variable_lines ) )

if __name__ == "__main__":
    if( len( sys.argv ) < 3 ):
        print( "Usage: python ExtractDiffVariables.py <patch_file_path> <output_path>" )
        sys.exit(1)

    process( sys.argv[1], sys.argv[2] )
