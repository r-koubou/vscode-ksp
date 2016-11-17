#!/usr/bin/perl

use strict;
use warnings;

open( my $fp, '<', 'BuiltinVariables.txt' ) or die "cannot open!";

my @lines = <$fp>;

print "exports.builtinVariables = {\n";

foreach my $line( @lines )
{
    my ( $name, $body, $comp, $desc ) = split /\t/, $line;
    chop( $desc );

    print << "EOM";
    "$comp":
    {
        "description": "$desc"
    },
EOM
}
print "};\n";

close( $fp );
