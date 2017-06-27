# encoding: utf-8

#
# GenCommandTestCode.py
# Copyright (c) R-Koubou
#

# Test statement here.
COMMAND_STATEMENT = """
set_ui_wf_property( $waveform, 0, 0, 0 )
"""

TEMPLATE = """

{{ Paste to Kontakt Script Editor }}

on init

{{ primitive variables }}
declare $i
declare %ia[ 2 ]

declare ~r
declare ?ra[ 2 ]

declare @s
declare !sa[ 2 ]

{{ ui variable }}
declare ui_button $button
declare ui_knob $knob( 0, 1000, 1 )
declare ui_file_selector $selector
declare ui_label $label(1,1)
declare ui_level_meter $level_meter
declare ui_menu $menu
declare ui_slider $slider (0,100)
declare ui_switch $switch
declare ui_table %table[10](2,2,100)
declare ui_text_edit @text_edit
declare ui_value_edit $value_edit(0,100,$VALUE_EDIT_MODE_NOTE_NAMES)
declare ui_waveform $waveform(6,6)
declare ui_xy ?xy[4]

{{

ui variable names

$button
$knob
$selector
$label
$level
$menu
$slider
$switch
%table[10]
@text_edit
$value_edit
$waveform
?xy
}}

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
