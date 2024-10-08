import sys
import string

from typing import List

def process(input_path: str, output_path: str, variable_name: str, excludes: List[str]):

    template = string.Template( "    \"${name}\",\n" )

    template_header = string.Template( """//
// Generated by Text2TSArray.py
//
export const ${variable_name} : string[] = [
""")

    template_footer = string.Template( "];\n" )


    lines = []
    with open(input_path, "r", encoding="utf-8") as f:
        lines = f.readlines()

    with open(output_path, "w", encoding="utf-8", newline="\n") as f:

        f.write( template_header.safe_substitute( variable_name = variable_name ) )


        for line in lines:
            line = line.strip()

            if( line in excludes ):
                None
            else:
                f.write( template.substitute( name = line ) )

        f.write( template_footer.substitute() )

if __name__ == "__main__":
    argv = sys.argv[1:]

    if( len(argv) < 2 ):
        print( "Usage: Text2TSArray.py <input file> <output file> <variable name> [excludes...]" )
        sys.exit(1)

    input_path      = argv[0]
    output_path     = argv[1]
    variable_name   = argv[2]
    excludes        = []

    if( len(argv) > 3 ):
        for i in argv[3:] :
            excludes.append( i.strip() )

    process( input_path, output_path, variable_name, excludes )
