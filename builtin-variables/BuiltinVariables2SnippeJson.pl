#!/usr/bin/perl

use strict;
use warnings;

open( my $fp, '<', 'BuiltinVariables.txt' ) or die "cannot open!";

my @lines = <$fp>;

foreach my $line( @lines )
{
    my ( $name, $body ) = split /\t/, $line;
    chop( $body );

    $body =~ s/^\$/\\\\\$/;

    print << "EOM";
    "$name":
    {
        "prefix": "$name",
        "body":[
            "$body"
        ]
    },
EOM
}

close( $fp );
