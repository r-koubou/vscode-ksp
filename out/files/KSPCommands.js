//
// Generated by /data/Excel2CompleteCommands.py
// NOTE: Remove comma after last object if you re-Generated.
exports.commands = {
    "on async_complete":
    {
        "signature":   "",
        "description": "async complete callback, triggered after the execution of any load/save command"
    },
    "on controller":
    {
        "signature":   "",
        "description": "MIDI controller callback, executed whenever a CC, pitch bend or channel pressure message is received"
    },
    "on init":
    {
        "signature":   "",
        "description": "initialization callback, executed when the script was successfully analyzed"
    },
    "on listener":
    {
        "signature":   "",
        "description": "listener callback, executed at definable time intervals or whenever a transport command is received"
    },
    "on note":
    {
        "signature":   "",
        "description": "note callback, executed whenever a note on message is received"
    },
    "on persistence_changed":
    {
        "signature":   "",
        "description": "executed after the init callback or whenever a snapshot has been loaded"
    },
    "on pgs_changed":
    {
        "signature":   "",
        "description": "executed whenever any pgs_set_key_val() command is executed in any script"
    },
    "on poly_at":
    {
        "signature":   "",
        "description": "polyphonic aftertouch callback, executed whenever a polyphonic aftertouch message is received"
    },
    "on release":
    {
        "signature":   "",
        "description": "release callback, executed whenever a note off message is received"
    },
    "on rpn":
    {
        "signature":   "",
        "description": "rpn(registered parameter number) callback message is received"
    },
    "on nrpn":
    {
        "signature":   "",
        "description": "nrpn(unregistered parameter number) callback message is received"
    },
    "on ui_control":
    {
        "signature":   "",
        "description": "UI callback, executed whenever the user changes the respective UI element"
    },
    "on ui_update":
    {
        "signature":   "",
        "description": "UI update callback, executed with every GUI change in KONTAKT"
    },
    "declare":
    {
        "signature":   "",
        "description": "declare a user-defined variable"
    },
    "make_instr_persistent":
    {
        "signature":   "(variable)",
        "description": "retain the value of a variable only with the instrument"
    },
    "make_persistent":
    {
        "signature":   "(variable)",
        "description": "retain the value of a variable whith the instrument and snapshot"
    },
    "read_persistent_var":
    {
        "signature":   "(variable)",
        "description": "instantly reloads the value of a variable that was saved via the make_persistent()command"
    },
    "ui_button":
    {
        "signature":   "",
        "description": "create a user interface button"
    },
    "ui_knob":
    {
        "signature":   "(min,max,displayRatio)",
        "description": "create a user interface button"
    },
    "ui_file_selector":
    {
        "signature":   "",
        "description": "create a user interface button"
    },
    "ui_label":
    {
        "signature":   "(width,height)",
        "description": "create a user interface text label"
    },
    "ui_level_meter":
    {
        "signature":   "",
        "description": "create a level meter"
    },
    "ui_menu":
    {
        "signature":   "",
        "description": "create a user interface drop-down menu"
    },
    "ui_slider":
    {
        "signature":   "(min,max)",
        "description": "create a user interface slider"
    },
    "ui_switch":
    {
        "signature":   "",
        "description": "create a user interface switch"
    },
    "ui_table":
    {
        "signature":   "(width,height,range)",
        "description": "create a user interface switch"
    },
    "ui_text_edit":
    {
        "signature":   "",
        "description": "create a text edit field"
    },
    "ui_value_edit":
    {
        "signature":   "(min,max,displayRatio)",
        "description": "create a user interface number box"
    },
    "ui_waveform":
    {
        "signature":   "(width,height)",
        "description": "create a waveform control to display zones and slices. Can also be used to control specific parameters per slice and for MIDI drag & drop functionality."
    },
    "ui_xy":
    {
        "signature":   "",
        "description": "create an XY pad"
    },
    "if":
    {
        "signature":   "",
        "description": "if statement"
    },
    "ifelse":
    {
        "signature":   "",
        "description": "if...else statement"
    },
    "select":
    {
        "signature":   "",
        "description": "if statement"
    },
    "while":
    {
        "signature":   "",
        "description": "while statement"
    },
    "boolean_op_gt":
    {
        "signature":   "",
        "description": "Boolean Operator: grater than or equal"
    },
    "boolean_op_lt":
    {
        "signature":   "",
        "description": "Boolean Operator: less than or equal"
    },
    "boolean_op_eq":
    {
        "signature":   "",
        "description": "Boolean Operator: equal"
    },
    "boolean_op_not_eq":
    {
        "signature":   "",
        "description": "Boolean Operator: not equal"
    },
    "in_range":
    {
        "signature":   "(x,y,z)",
        "description": "Boolean Operator: true if x is between y and z"
    },
    "boolean_op_not":
    {
        "signature":   "",
        "description": "Boolean Operator: true if a is false and vice versa"
    },
    "boolean_op_and":
    {
        "signature":   "",
        "description": "Boolean Operator: true if a is true and b is true"
    },
    "boolean_op_or":
    {
        "signature":   "",
        "description": "Boolean Operator: true if a is true or b is true"
    },
    "inc":
    {
        "signature":   "(x)",
        "description": "increment an expression by 1 (x + 1)"
    },
    "dec":
    {
        "signature":   "(x)",
        "description": "decrement an expression by 1 (x – 1)"
    },
    "mod":
    {
        "signature":   "",
        "description": "modulo; returns the remainder of a division"
    },
    "exp":
    {
        "signature":   "(x)",
        "description": "exponential function (returns the value of e^x)"
    },
    "log":
    {
        "signature":   "(x)",
        "description": "logarithmic function"
    },
    "pow":
    {
        "signature":   "(x,y)",
        "description": "power (returns the value of x^y)"
    },
    "sqrt":
    {
        "signature":   "(x)",
        "description": "square root"
    },
    "ceil":
    {
        "signature":   "(x)",
        "description": "ceiling (round up) ceil(2.3) = 3.0"
    },
    "floor":
    {
        "signature":   "(x)",
        "description": "floor (round down) floor(2.8) = 2.0"
    },
    "round":
    {
        "signature":   "(x)",
        "description": "round (round to nearest) round(2.3) = 2.0 round(2.8) = 3.0"
    },
    "cos":
    {
        "signature":   "(x)",
        "description": "cosine function"
    },
    "sin":
    {
        "signature":   "(x)",
        "description": "sine function"
    },
    "tan":
    {
        "signature":   "(x)",
        "description": "tangent function"
    },
    "acos":
    {
        "signature":   "(x)",
        "description": "arccosine function"
    },
    "asin":
    {
        "signature":   "(x)",
        "description": "arcsine (inverse sine function)"
    },
    "atan":
    {
        "signature":   "(x)",
        "description": "arctangent (inverse tangent function)"
    },
    "bitwise_and":
    {
        "signature":   "",
        "description": "Bitwise Operator: \"and\""
    },
    "bitwise_or":
    {
        "signature":   "",
        "description": "Bitwise Operator: \"or\""
    },
    "bitwise_not":
    {
        "signature":   "",
        "description": "Bitwise Operator: \"negation\""
    },
    "sh_left":
    {
        "signature":   "(expression,shiftBits)",
        "description": "shifts the bits in <expression> by the amount of <shift-bits> to the left"
    },
    "sh_right":
    {
        "signature":   "(expression,shiftBits)",
        "description": "shifts the bits in <expression> by the amount of <shift-bits> to the right"
    },
    "random":
    {
        "signature":   "(min,max)",
        "description": "generate a random integer in the range <min> to <max>"
    },
    "int_to_real":
    {
        "signature":   "(integerValue)",
        "description": "converts an integer value into a real number"
    },
    "real_to_int":
    {
        "signature":   "(realValue)",
        "description": "converts a real number into an integer"
    },
    "msb":
    {
        "signature":   "(value)",
        "description": "return the MSB portion (most significant byte) of a 14 bit value"
    },
    "lsb":
    {
        "signature":   "(value)",
        "description": "return the LSB portion (least significant byte) of a 14 bit value"
    },
    "exit":
    {
        "signature":   "",
        "description": "immediately stops a callback or exits a function"
    },
    "ignore_controller":
    {
        "signature":   "",
        "description": "ignore a controller event in a controller callback"
    },
    "message":
    {
        "signature":   "(value)",
        "description": "display text in the status line of KONTAKT"
    },
    "note_off":
    {
        "signature":   "(IDNumber)",
        "description": "send a note off message to a specific note"
    },
    "play_note":
    {
        "signature":   "(noteNumber,velocity,sampleOffset,duration)",
        "description": "generate a MIDI note, i.e. generate a note on message followed by a note off message"
    },
    "set_controller":
    {
        "signature":   "(MIDI_CC_number,value)",
        "description": "send a MIDI CC, pitchbend or channel pressure value"
    },
    "set_rpn":
    {
        "signature":   "(address,value)",
        "description": "send a rpn message"
    },
    "set_nrpn":
    {
        "signature":   "(address,value)",
        "description": "send a nrpn message"
    },
    "by_marks":
    {
        "signature":   "(bitMask)",
        "description": "a user defined group of events (or event IDs)"
    },
    "change_note":
    {
        "signature":   "(IDNumber,noteNumber)",
        "description": "change the note number of a specific note event"
    },
    "change_pan":
    {
        "signature":   "(IDNumber,panorama,relativeBit)",
        "description": "change the pan position of a specific note event"
    },
    "change_tune":
    {
        "signature":   "(IDNumber,tuneAmount(millicents),relativeBit)",
        "description": "change the tuning of a specific note event in millicent"
    },
    "change_velo":
    {
        "signature":   "(IDNumber,velocity)",
        "description": "change the velocity of a specific note event"
    },
    "change_vol":
    {
        "signature":   "(IDNumber,volume,relativeBit)",
        "description": "change the volume of a specific note event in millidecibel"
    },
    "delete_event_mark":
    {
        "signature":   "(IDNumber,$MARK_n)",
        "description": "delete an event mark, i.e. ungroup the specified event from an event group"
    },
    "event_status":
    {
        "signature":   "(IDNumber)",
        "description": "retrieve the status of a particular note event (or MIDI event in the multi script)"
    },
    "fade_in":
    {
        "signature":   "(IDNumber,fadeTime(microseconds))",
        "description": "perform a fade-in for a specific note event"
    },
    "fade_out":
    {
        "signature":   "(IDNumber,fadeTime(microseconds))",
        "description": "perform a fade-out for a specific note event"
    },
    "get_event_ids":
    {
        "signature":   "(arrayName)",
        "description": "fills the specified array with all active event IDs."
    },
    "get_event_par":
    {
        "signature":   "(IDNumber,parameter)",
        "description": "return the value of a specific event parameter of the specified event"
    },
    "get_event_par_arr":
    {
        "signature":   "(IDNumber,parameter,groupIndex)",
        "description": "special form of get_event_par(), used to retrieve the group allow state of the specified event"
    },
    "ignore_event":
    {
        "signature":   "(IDNumber)",
        "description": "ignore a note event in a note on or note off callback"
    },
    "set_event_mark":
    {
        "signature":   "(IDNumber,$MARK_n)",
        "description": "ignore a note event in a note on or note off callback"
    },
    "set_event_par":
    {
        "signature":   "(IDNumber,parameter,value)",
        "description": "assign a parameter to a specific event"
    },
    "set_event_par_arr":
    {
        "signature":   "(IDNumber,parameter,value,groupIndex)",
        "description": "special form of set_event_par(), used to set the group allow state of the specified event"
    },
    "array_equal":
    {
        "signature":   "(arrayVariable,arrayVariable)",
        "description": "checks the values of two arrays, true if all values are equal, false if not"
    },
    "num_elements":
    {
        "signature":   "(arrayVariable)",
        "description": "returns the number of elements in an array"
    },
    "search":
    {
        "signature":   "(arrayVariable,value)",
        "description": "searches the specified array for the specified value and returns the index of its first position."
    },
    "sort":
    {
        "signature":   "(arrayVariable,direction)",
        "description": "searches the specified array for the specified value and returns the index of its first position."
    },
    "allow_group":
    {
        "signature":   "(groupIndex)",
        "description": "allows the specified group, i.e. makes it available for playback"
    },
    "disallow_group":
    {
        "signature":   "(groupIndex)",
        "description": "disallows the specified group, i.e. makes it unavailable for playback"
    },
    "find_group":
    {
        "signature":   "(groupName)",
        "description": "returns the group index for the specified group name"
    },
    "get_purge_state":
    {
        "signature":   "(groupIndex)",
        "description": "returns the purge state of the specified group. 0=purged, 1=not purged"
    },
    "group_name":
    {
        "signature":   "(groupIndex)",
        "description": "returns the group name for the specified group"
    },
    "purge_group":
    {
        "signature":   "(groupIndex,mode)",
        "description": "purges (i.e. unloads from RAM) the samples of the specified group"
    },
    "change_listener_par":
    {
        "signature":   "(signalType,parameter)",
        "description": "changes the parameters of the on listener callback. Can be used in every callback."
    },
    "ms_to_ticks":
    {
        "signature":   "(microseconds)",
        "description": "converts a microseconds value into a tempo dependent ticks value"
    },
    "set_listener":
    {
        "signature":   "(signalType,parameter)",
        "description": "Sets the signals on which the listener callback should react to. Can only be used in the init callback."
    },
    "stop_wait":
    {
        "signature":   "(callback_ID,parameter)",
        "description": "stops wait commands in the specified callback"
    },
    "reset_ksp_timer":
    {
        "signature":   "",
        "description": "resets the KSP timer ($KSP_TIMER) to zero"
    },
    "ticks_to_ms":
    {
        "signature":   "(ticks)",
        "description": "converts a tempo dependent ticks value into a microseconds value"
    },
    "wait":
    {
        "signature":   "(waitTime(microseconds))",
        "description": "pauses the callback for the specified time in microseconds"
    },
    "wait_ticks":
    {
        "signature":   "(waitTime(ticks))",
        "description": "pauses the callback for the specified time in ticks"
    },
    "add_menu_item":
    {
        "signature":   "(variable,text,value)",
        "description": "create a menu entry"
    },
    "add_text_line":
    {
        "signature":   "(variable,text)",
        "description": "add a new text line in the specified label without erasing existing text"
    },
    "attach_level_meter":
    {
        "signature":   "(ui_ID,group,slot,channel,bus)",
        "description": "attach a level meter to a certain position within the instrument to read volume data"
    },
    "attach_zone":
    {
        "signature":   "(variable,zoneId,flags)",
        "description": "connects the corresponding zone to the waveform so that it shows up within the display"
    },
    "hide_part":
    {
        "signature":   "(variable,hideMask)",
        "description": "hide specific parts of user interface controls"
    },
    "fs_get_filename":
    {
        "signature":   "(ui_ID,returnParameter)",
        "description": "return the filename of the last selected file in the UI file browser."
    },
    "fs_navigate":
    {
        "signature":   "(ui_ID,direction)",
        "description": "jump to the next/previous file in an ui file selector and trigger its callback."
    },
    "get_control_par":
    {
        "signature":   "(ui_ID,controlParameter)",
        "description": "retrieve various parameters of the specified gui control"
    },
    "get_menu_item_str":
    {
        "signature":   "(menu_ID,index)",
        "description": "returns the string value of the menu's entry."
    },
    "get_menu_item_value":
    {
        "signature":   "(menu_ID,index)",
        "description": "returns the value of the menu's entry."
    },
    "get_menu_item_visibility":
    {
        "signature":   "(menu_ID,index)",
        "description": "returns 1 if the menu entry is visible, otherwise 0."
    },
    "get_ui_id":
    {
        "signature":   "(variable)",
        "description": "retrieve the ID number of an ui control"
    },
    "get_ui_wf_property":
    {
        "signature":   "(variable,property>},index)",
        "description": "returns the value of the waveform’s different properties."
    },
    "make_perfview":
    {
        "signature":   "",
        "description": "activates the performance view for the respective script"
    },
    "move_control":
    {
        "signature":   "(variable,xPosition(0 to 6),yPosition(0 to 16))",
        "description": "position ui elements in the standard KONTAKT grid"
    },
    "move_control_px":
    {
        "signature":   "(variable,xPosition(px),yPosition(px))",
        "description": "position ui elements in pixels"
    },
    "set_control_help":
    {
        "signature":   "(variable,text)",
        "description": "assigns a text string to be displayed when hovering the ui control. The text will appear in KONTAKT's info pane."
    },
    "set_control_par":
    {
        "signature":   "(ui_ID,controlParameter,value)",
        "description": "change various parameters of the specified gui control"
    },
    "set_control_par_str":
    {
        "signature":   "(ui_ID,controlParameter,value,index)",
        "description": "A variation of the command for usage with text strings. ( see: set_control_par() Remarks)"
    },
    "set_control_par_arr":
    {
        "signature":   "(ui_ID,controlParameter,value,index)",
        "description": "change various parameters of an element within an array based gui control (for example: cursors in the XY pad)"
    },
    "set_control_par_str_arr":
    {
        "signature":   "(ui_ID,controlParameter,value,index)",
        "description": "A variation of the command for usage with text strings. ( see: set_control_par_arr() Remarks)"
    },
    "set_knob_defval":
    {
        "signature":   "(variable,value)",
        "description": "assign a default value to a knob to which the knob is reset when Cmd-clicking (mac) or Ctrl-clicking (PC) the knob."
    },
    "set_knob_label":
    {
        "signature":   "(variable,text)",
        "description": "assign a text string to a knob"
    },
    "set_knob_unit":
    {
        "signature":   "(variable,knobUnitConstant)",
        "description": "assign a unit mark to a knob."
    },
    "set_menu_item_str":
    {
        "signature":   "(menuId,index,string)",
        "description": "sets the value of a menu entry."
    },
    "set_menu_item_value":
    {
        "signature":   "(menuId,index,value)",
        "description": "sets the value of a menu entry."
    },
    "set_menu_item_visibility":
    {
        "signature":   "(menuId,index,visibility)",
        "description": "sets the visibility of a menu entry."
    },
    "set_table_steps_shown":
    {
        "signature":   "(variable,numOfSteps)",
        "description": "changes the number of displayed columns in an ui table"
    },
    "set_script_title":
    {
        "signature":   "(text)",
        "description": "set the script title"
    },
    "set_skin_offset":
    {
        "signature":   "(offsetInPixel)",
        "description": "offsets the chosen background picture file by the specified number of pixels"
    },
    "set_text":
    {
        "signature":   "(variable,text)",
        "description": "when applied to a label: delete the text currently visible in the specified label and add new text.\nwhen applied to knobs, buttons, switches and value edits: set the display name of the ui element.\n"
    },
    "set_ui_color":
    {
        "signature":   "(hex values(9RRGGBBh)),text)",
        "description": "set the main background color of the performance view"
    },
    "set_ui_height":
    {
        "signature":   "(height(1 to 8))",
        "description": "set the height of a script performance view in grid units"
    },
    "set_ui_height_px":
    {
        "signature":   "(height(50px to 750px))",
        "description": "set the height of a script performance view in pixels"
    },
    "set_ui_width_px":
    {
        "signature":   "(width(633px to 1000px))",
        "description": "set the width of a script performance view in pixels"
    },
    "set_ui_wf_property":
    {
        "signature":   "(variable,property,index,value)",
        "description": "sets different properties for the waveform control"
    },
    "get_key_color":
    {
        "signature":   "(noteNr)",
        "description": "returns the color constant of the specified note number"
    },
    "get_key_name":
    {
        "signature":   "(noteNr)",
        "description": "returns the name of the specified key"
    },
    "get_key_triggerstate":
    {
        "signature":   "(noteNr)",
        "description": "returns the pressed state of the specified note number (i.e. key) on the KONTAKT keyboard, can be either 1 (key pressed) or 0 (key released)"
    },
    "get_key_type":
    {
        "signature":   "(noteNr)",
        "description": "returns the key type constant of the specified key."
    },
    "get_keyrange_min_note":
    {
        "signature":   "(noteNr)",
        "description": "returns the lowest note of the specified key range"
    },
    "get_keyrange_max_note":
    {
        "signature":   "(noteNr)",
        "description": "returns the highest note of the specified key range"
    },
    "get_keyrange_name":
    {
        "signature":   "(noteNr)",
        "description": "returns the name of the specified key range"
    },
    "set_key_color":
    {
        "signature":   "(noteNr,keyColorConstant)",
        "description": "sets the color of the specified key (i.e. MIDI note) on the KONTAKT keyboard. use $KEY_COLOR_****"
    },
    "set_key_name":
    {
        "signature":   "(noteNr,name)",
        "description": "assigns a text string to the specified key"
    },
    "set_key_pressed":
    {
        "signature":   "(noteNr,value)",
        "description": "sets the trigger state of the specified key on KONTAKT's keyboard either to pressed/on (1) or released/off (0)"
    },
    "set_key_pressed_support":
    {
        "signature":   "(mode)",
        "description": "sets the pressed state support mode for KONTAKT'S keyboard. The available modes are:\n0: KONTAKT handles all pressed states, set_key_pressed() commands are ignored (default mode)\n1: KONTAKT's keyboard is only affected by set_key_pressed() commands\n"
    },
    "set_key_type":
    {
        "signature":   "(noteNr,keyTypeConstant)",
        "description": "assigns a key type to the specified key.\nThe following key types are available:\n$NI_KEY_TYPE_DEFAULT (i.e. normal mapped notes that produce sound)\n$NI_KEY_TYPE_CONTROL (i.e. key switches or other notes that do not produce sound)\n$NI_KEY_TYPE_NONE (resets the key to its normal KONTAKT behaviour)\n"
    },
    "set_keyrange":
    {
        "signature":   "(minNote,maxNote,name)",
        "description": "assigns a text string to the specified range of keys."
    },
    "remove_keyrange":
    {
        "signature":   "(noteNr)",
        "description": "assigns a text string to the specified range of keys"
    },
    "find_mod":
    {
        "signature":   "(groupIndex,modName)",
        "description": "returns the slot index of an internal modulator or external modulation slot"
    },
    "find_target":
    {
        "signature":   "(groupIndex,modIndex,targetName)",
        "description": "returns the slot index of a modulation slot of an internal modulator"
    },
    "get_engine_par":
    {
        "signature":   "(parameter,group,slot,generic)",
        "description": "returns the value of a specific engine parameter"
    },
    "get_engine_par_disp":
    {
        "signature":   "(parameter,group,slot,generic)",
        "description": "returns the displayed string of a specific engine parameter"
    },
    "get_voice_limit":
    {
        "signature":   "(voiceType)",
        "description": "retunrs the voice limit for the Time Machine Pro mode of the source module"
    },
    "output_channel_name":
    {
        "signature":   "(outputNumber)",
        "description": "returns the channel name for the specified output"
    },
    "set_engine_par":
    {
        "signature":   "(parameter,value,group,slot,generic)",
        "description": "control automatable KONTAKT parameters and bypass buttons"
    },
    "set_voice_limit":
    {
        "signature":   "(voiceType,value)",
        "description": "sets the voice limit for the Time Machine Pro mode of the source module"
    },
    "get_folder":
    {
        "signature":   "(pathVariable)",
        "description": "returns the path specified with the built-in path variable"
    },
    "load_array":
    {
        "signature":   "(arrayVariable,mode)",
        "description": "loads an array from an external file (.nka file)"
    },
    "load_array_str":
    {
        "signature":   "(arrayVariable,path)",
        "description": "loads an array from an external file (.nka file) using the file's absolute path"
    },
    "load_ir_sample":
    {
        "signature":   "(filePath,slot,generic)",
        "description": "loads an impulse response sample into KONTAKT's convolution effect"
    },
    "save_array":
    {
        "signature":   "(arrayVariable,mode)",
        "description": "saves an array to an external file (i.e. an .nka file)"
    },
    "save_array_str":
    {
        "signature":   "(arrayVariable,path)",
        "description": "saves an array to an external file (i.e. an .nka file), using the specified absolute path"
    },
    "save_midi_file":
    {
        "signature":   "(path)",
        "description": "saves a MIDI file with a range specified by the mf_set_export_area() command."
    },
    "mf_insert_file":
    {
        "signature":   "(path,trackOffset,positionOffset,mode)",
        "description": "inserts a MIDI file into the MIDI object."
    },
    "mf_set_export_area":
    {
        "signature":   "(name,startPos,endPos,startTrack,endTrack)",
        "description": "defines the part of the MIDI object that will be exported when using a drag and drop area, or the save_midi_file() command."
    },
    "mf_set_buffer_size":
    {
        "signature":   "(size)",
        "description": "defines a number of inactive MIDI events, that can be activated and edited"
    },
    "mf_get_buffer_size":
    {
        "signature":   "(void)",
        "description": "returns the size of the MIDI event buffer"
    },
    "mf_reset":
    {
        "signature":   "(void)",
        "description": "resets the MIDI object, sets the event buffer to zero, and removes all events"
    },
    "mf_insert_event":
    {
        "signature":   "(track,pos,command,byte1,byte2)",
        "description": "activates an inactive MIDI event in the MIDI object. However, because the command and position are defined in this command, it can be considered as an insertion."
    },
    "mf_remove_event":
    {
        "signature":   "(eventId)",
        "description": "deactivates an event in the MIDI object, effectively removing it"
    },
    "mf_set_event_par":
    {
        "signature":   "(eventId,parameter,value)",
        "description": "sets an event parameter"
    },
    "mf_get_event_par":
    {
        "signature":   "(eventId,parameter)",
        "description": "returns the value of an event parameter"
    },
    "mf_get_id":
    {
        "signature":   "(void)",
        "description": "returns the ID of the currently selected event (when using the navigation commands like mf_get_first(), and mf_get_next(), etc)"
    },
    "mf_set_mark":
    {
        "signature":   "(eventId,mark,status)",
        "description": "marks an event, so that you may groups events together and process that group quickly"
    },
    "mf_get_mark":
    {
        "signature":   "(eventId,mark)",
        "description": "checks if an event is marked or not. Returns 1 if it is marked, or 0 if it is not."
    },
    "by_track":
    {
        "signature":   "(track)",
        "description": "can be used to group events by their track number"
    },
    "mf_get_first":
    {
        "signature":   "(trackIndex)",
        "description": "moves the position marker to the first event in the MIDI track"
    },
    "mf_get_last":
    {
        "signature":   "(trackIndex)",
        "description": "moves the position marker to the last event in the MIDI track"
    },
    "mf_get_next":
    {
        "signature":   "(trackIndex)",
        "description": "moves the position marker to the next event in the MIDI track"
    },
    "mf_get_next_at":
    {
        "signature":   "(trackIndex,pos)",
        "description": "moves the position marker to the next event in the MIDI track right after the defined position."
    },
    "mf_get_prev":
    {
        "signature":   "(trackIndex)",
        "description": "moves the position marker to the previous event in the MIDI track"
    },
    "mf_get_prev_at":
    {
        "signature":   "(trackIndex,pos)",
        "description": "moves the position marker to the first event before the defined position"
    },
    "mf_get_num_tracks":
    {
        "signature":   "(void)",
        "description": "returns the number of tracks in a MIDI object."
    },
    "SET_CONDITION":
    {
        "signature":   "(conditionSymbol)",
        "description": "define a symbol to be used as a condition"
    },
    "RESET_CONDITION":
    {
        "signature":   "(conditionSymbol)",
        "description": "delete a definition"
    },
    "USE_CODE_IF":
    {
        "signature":   "(conditionSymbol)",
        "description": "interpret code when <condition> is defined"
    },
    "USE_CODE_IF_NOT":
    {
        "signature":   "(conditionSymbol)",
        "description": "interpret code when <condition> is not defined"
    },
    "END_USE_CODE":
    {
        "signature":   "",
        "description": "end of USE_CODE_IF"
    },
    "NO_SYS_SCRIPT_GROUP_START":
    {
        "signature":   "",
        "description": "condition; if defined with SET_CONDITION(), the system script which handles all group start options will be bypassed"
    },
    "NO_SYS_SCRIPT_PEDAL":
    {
        "signature":   "",
        "description": "condition; if defined with SET_CONDITION(), the system script which sustains notes when CC# 64 is received will be bypassed"
    },
    "NO_SYS_SCRIPT_RLS_TRIG":
    {
        "signature":   "",
        "description": "condition; if defined with SET_CONDITION(), the system script which triggers samples upon the release of a key is bypassed"
    },
    "reset_rls_trig_counter":
    {
        "signature":   "(note)",
        "description": "dresets the release trigger counter (used by the release trigger system script)"
    },
    "will_never_terminate":
    {
        "signature":   "(eventId)",
        "description": ""
    },
    "pgs_create_key":
    {
        "signature":   "(keyId,size)",
        "description": "It is possible to send and receive values from one script to another, discarding the usual left-to-right order by using the Program Global Storage (PGS) commands. PGS is a dynamic memory that can be read/written by any script."
    },
    "pgs_key_exists":
    {
        "signature":   "(keyId)",
        "description": "It is possible to send and receive values from one script to another, discarding the usual left-to-right order by using the Program Global Storage (PGS) commands. PGS is a dynamic memory that can be read/written by any script."
    },
    "pgs_set_key_val":
    {
        "signature":   "(keyId,index,value)",
        "description": "It is possible to send and receive values from one script to another, discarding the usual left-to-right order by using the Program Global Storage (PGS) commands. PGS is a dynamic memory that can be read/written by any script."
    },
    "pgs_get_key_val":
    {
        "signature":   "(keyId,index)",
        "description": "It is possible to send and receive values from one script to another, discarding the usual left-to-right order by using the Program Global Storage (PGS) commands. PGS is a dynamic memory that can be read/written by any script."
    },
    "pgs_create_str_key":
    {
        "signature":   "(keyId)",
        "description": ""
    },
    "pgs_str_key_exists":
    {
        "signature":   "(keyId)",
        "description": "It is possible to send and receive values from one script to another, discarding the usual left-to-right order by using the Program Global Storage (PGS) commands. PGS is a dynamic memory that can be read/written by any script."
    },
    "pgs_set_str_key_val":
    {
        "signature":   "(keyId,stringvalue)",
        "description": ""
    },
    "find_zone":
    {
        "signature":   "(zoneName)",
        "description": "returns the zone ID for the specified zone name. Only availabe in the init callback."
    },
    "get_sample_length":
    {
        "signature":   "(zone_ID)",
        "description": "returns the length of the specified zone's sample in microseconds"
    },
    "num_slices_zone":
    {
        "signature":   "(zone_ID)",
        "description": "returns the number of slices of the specified zone"
    },
    "zone_slice_length":
    {
        "signature":   "(zone_ID,sliceIndex)",
        "description": "returns the length in microseconds of the specified slice with respect to the current tempo"
    },
    "zone_slice_start":
    {
        "signature":   "(zone_ID,sliceIndex)",
        "description": "returns the absolute start point of the specified slice in microseconds, independent of the current tempo"
    },
    "zone_slice_idx_loop_start":
    {
        "signature":   "(zone_ID,loopIndex)",
        "description": "returns the index number of the slice at the loop start"
    },
    "zone_slice_idx_loop_end":
    {
        "signature":   "(zone_ID,loopIndex)",
        "description": "returns the index number of the slice at the loop end"
    },
    "zone_slice_loop_count":
    {
        "signature":   "(zone_ID,loopIndex)",
        "description": "returns the loop count of the specified loop"
    },
    "dont_use_machine_mode":
    {
        "signature":   "(IDNumber)",
        "description": "play the specified event in sampler mode"
    },
    "function":
    {
        "signature":   "",
        "description": "declares a function"
    },
    "call":
    {
        "signature":   "functionName",
        "description": "calls a previously declares function"
    },
    "ignore_midi":
    {
        "signature":   "",
        "description": "Like ignore_event(), ignore_midi is a very \"strong\" command. Keep in mind that ignore_midi will ignore all incoming MIDI events. If you simply want to change the MIDI channel and/or any of the MIDI bytes, you can also use set_event_par()."
    },
    "on midi_in":
    {
        "signature":   "",
        "description": "Like ignore_event(), ignore_midi is a very \"strong\" command. Keep in mind that ignore_midi will ignore all incoming MIDI events. If you simply want to change the MIDI channel and/or any of the MIDI bytes, you can also use set_event_par()."
    },
    "set_midi":
    {
        "signature":   "(channel,command,byte1,byte2)",
        "description": "create any type of MIDI event. If you simply want to change the MIDI channel and/or any of the MIDI bytes, you can also use set_event_par()."
    },
};
