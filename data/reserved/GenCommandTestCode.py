# encoding: utf-8

#
# GenCommandTestCode.py
# Copyright (c) R-Koubou
#

# Test statement here.
COMMAND_STATEMENT = """

"""

TEMPLATE = """

{{ Paste to Kontakt Script Editor }}

on init

{{ primitive variables }}
declare $i1
declare $i2
declare %ia1[ 2 ]
declare %ia2[ 2 ]

declare ~r1
declare ~r2
declare ?ra1[ 2 ]
declare ?ra2[ 2 ]

declare @s1
declare @s2
declare !sa1[ 2 ]
declare !sa2[ 2 ]

{{ ui variable }}
declare ui_knob $knob( 0, 1000, 1 )
declare ui_menu $menu
declare ui_label $label (1,1)
declare ui_level_meter $level
declare ui_waveform $waveform(6,6)

{testcode}

end on

on async_complete
{testcode}
end on

on controller
{testcode}
end on

on listener
{testcode}
end on

on note
{testcode}
end on

on persistence_changed
{testcode}
end on

on pgs_changed
{testcode}
end on

on poly_at
{testcode}
end on

on release
{testcode}
end on

on rpn
{testcode}
end on

on nrpn
{testcode}
end on

on ui_control( $knob )
{testcode}
end on

on ui_update
{testcode}
end on

"""

print( TEMPLATE.format(
    testcode = COMMAND_STATEMENT
))
