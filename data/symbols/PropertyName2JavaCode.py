# encoding: utf-8

#
# PropertyName2JavaCode.py
# Copyright (c) R-Koubou
#

# Property Names here
PROPERTIES = [
    "error.semantic.variable.invalid.arrayinitilizer"
]

TEMPLATE = "static public final String PROPERTY_{name} = \"{value}\";"

for i in PROPERTIES:
    n = i.replace( ".", "_" )
    n = n.upper()
    print( TEMPLATE.format(
        name  = n,
        value = i
    ))
