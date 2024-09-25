import sys

def process(input_path, output_path):
    type_mappings = {
        '$': "I",
        '~': "R",
        '@': "S",
        '%': "I[]",
        '?': "R[]",
        '!': "S[]",
    }
    variables = []

    with open(input_path, 'r') as f:
        variables  = f.read().splitlines()
        output_tsv = []

        for i in variables:
            name = i[1:]
            type = type_mappings[i[0]]
            output_tsv.append(f"{type}\t{name}\tY")

    with open(output_path, 'w', encoding="utf-8", newline="\n") as f:
        f.write("\n".join(output_tsv))

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python ConvertCompilerVariableSymbolFromDiff.py <path/to/extract_diff_KSPBuiltinVariableNames.ts.txt> <output_path>")
        sys
    input_path  = sys.argv[1]
    output_path = sys.argv[2]
    process(input_path, output_path)
