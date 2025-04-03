# Changelog

## Version 0.9.0

### 1st release of 1.0.0 PREVIEW version

Redesigned and implemented from scratch.

#### Commpiler Improvements

- Language server protocol (LSP) supported
    - Code Completion
    - Document Symbol
    - Go to Definition
    - Find References in script
    - Find Symbols
    - Folding
    - Hover
    - Document Symbol
    - Rename Refactoring
    - Signature Help
- Support for commands with argument overload
- Support for document comments if comment is before declaration
    - Variable
    - User function

#### Command

- Command and argument descriptions are now displayed when hover is displayed. (if indicated in the KSP manual).
- As of April 1, 2025, we rechecked the KSP manual and added all commands that we had missed.


<details>
<summary>Previous versions</summary>

## Version 0.7.30

### KONTAKT 8.1.0 ready (& includes omissions for variables added in K8.0)

#### Variable

<details>
<summary>Click to expand</summary>

- $ENGINE_PAR_BASSINVADER_MONO
- $ENGINE_PAR_CS_DISTORTION
- $ENGINE_PAR_CS_HIGH
- $ENGINE_PAR_CS_LEVEL
- $ENGINE_PAR_CS_LOW
- $ENGINE_PAR_CS_MONO
- $ENGINE_PAR_KOLOR_BASS
- $ENGINE_PAR_KOLOR_BASS_SAVER
- $ENGINE_PAR_KOLOR_BOOST
- $ENGINE_PAR_KOLOR_BOOST_LEVEL
- $ENGINE_PAR_KOLOR_DRIVE
- $ENGINE_PAR_KOLOR_HPF
- $ENGINE_PAR_KOLOR_LPF
- $ENGINE_PAR_KOLOR_MID
- $ENGINE_PAR_KOLOR_MID_FREQ
- $ENGINE_PAR_KOLOR_MID_Q
- $ENGINE_PAR_KOLOR_MIX
- $ENGINE_PAR_KOLOR_MODE
- $ENGINE_PAR_KOLOR_TREBLE
- $ENGINE_PAR_SF100_BASS
- $ENGINE_PAR_SF100_BRIGHT
- $ENGINE_PAR_SF100_CRUNCH
- $ENGINE_PAR_SF100_DEPTH
- $ENGINE_PAR_SF100_MASTER
- $ENGINE_PAR_SF100_MID
- $ENGINE_PAR_SF100_MONO
- $ENGINE_PAR_SF100_NORMAL_GAIN
- $ENGINE_PAR_SF100_OD_GAIN
- $ENGINE_PAR_SF100_OVEDRIVE
- $ENGINE_PAR_SF100_PRESENCE
- $ENGINE_PAR_SF100_TREBLE
- $ENGINE_PAR_SKDLX_BASS
- $ENGINE_PAR_SKDLX_DRIVE
- $ENGINE_PAR_SKDLX_MID
- $ENGINE_PAR_SKDLX_MODE
- $ENGINE_PAR_SKDLX_MONO
- $ENGINE_PAR_SKDLX_TONE
- $ENGINE_PAR_SKDLX_TREBLE
- $NI_KOLOR_ANALOG_AURA
- $NI_KOLOR_CRUNCH
- $NI_KOLOR_DIODE
- $NI_KOLOR_FULL_ON
- $NI_KOLOR_HARMONIC_CTRL
- $NI_KOLOR_SATURAVER
- $NI_KOLOR_SMOOTH_FUZZ
- $NI_KOLOR_SOLID_CONSOLE
- $NI_KOLOR_SUPER_AMP
- $NI_KOLOR_TAPE
- $NI_SKDLX_MODE_CLASSIC
- $NI_SKDLX_MODE_EQ
- $NI_SKDLX_MODE_LED

</details>

#### Command

- load_komplete_ui
- get_engine_par_disp_ext

### Bug fixes

- Snippet text for `set_control_par_str` command


## Version 0.7.29

### Hotfix

- Fixed internal compiler bug
  - Obfuscator cannot recognize the `continue` statement.

## Version 0.7.28

### KONTAKT 8.0.0 ready & continue statement supported

- `continue` statement which added in KONTAKT 7.5 is now supported in internal compiler.
- Added Syntax highlighting

#### Variable

<details>
<summary>Click to expand</summary>

- $ENGINE_PAR_VOICE_GROUP
- $ENGINE_PAR_BEATMASHER_GATE
- $ENGINE_PAR_BEATMASHER_LENGTH
- $ENGINE_PAR_BEATMASHER_MASH
- $ENGINE_PAR_BEATMASHER_MIX
- $ENGINE_PAR_BEATMASHER_REVERSE
- $ENGINE_PAR_BEATMASHER_ROTATE
- $ENGINE_PAR_BEATMASHER_WRAP
- $ENGINE_PAR_BEATSLICER_BUZZ
- $ENGINE_PAR_BEATSLICER_GATE
- $ENGINE_PAR_BEATSLICER_MIX
- $ENGINE_PAR_BEATSLICER_PATTERN
- $ENGINE_PAR_BEATSLICER_SLICE
- $ENGINE_PAR_BEATSLICER_STYLE
- $ENGINE_PAR_BEATSLICER_TWOBARS
- $ENGINE_PAR_GATER_GATE
- $ENGINE_PAR_GATER_MIX
- $ENGINE_PAR_GATER_MUTE_INPUT
- $ENGINE_PAR_GATER_NOISE
- $ENGINE_PAR_GATER_RATE
- $ENGINE_PAR_GATER_RATE_SYNC
- $ENGINE_PAR_GATER_SHAPE
- $ENGINE_PAR_GATER_STUTTER
- $ENGINE_PAR_RDL_ACCENT
- $ENGINE_PAR_RDL_DUCKING_AMOUNT
- $ENGINE_PAR_RDL_DUCKING_RELEASE
- $ENGINE_PAR_RDL_DUCKING_SENSE
- $ENGINE_PAR_RDL_FEEL
- $ENGINE_PAR_RDL_LR_OFFSET
- $ENGINE_PAR_RDL_PAN
- $ENGINE_PAR_RDL_PINGPONG_FLIP
- $ENGINE_PAR_RDL_SHUFFLE
- $ENGINE_PAR_RDL_WIDTH
- $ENGINE_PAR_RG_FORWARD
- $ENGINE_PAR_RG_GRAIN
- $ENGINE_PAR_RG_INVERT_GRAINS
- $ENGINE_PAR_RG_MIX
- $ENGINE_PAR_RG_PITCH
- $ENGINE_PAR_RG_REVERSE
- $ENGINE_PAR_RG_SPEED
- $ENGINE_PAR_TS_AMOUNT
- $ENGINE_PAR_TS_GRAIN
- $ENGINE_PAR_TS_KEY
- $ENGINE_PAR_TS_MIX
- $ENGINE_PAR_TS_SIZE
- $ENGINE_PAR_TS_STRETCH
- $ENGINE_PAR_TS_TWOBARS
- $ENGINE_PAR_WT_FORM2
- $ENGINE_PAR_WT_FORM2_MODE
- $ENGINE_PAR_WT_MOD_TUNE
- $ENGINE_PAR_WT_MOD_TUNE_UNIT
- $ENGINE_PAR_WT_MOD_TYPE
- $ENGINE_PAR_WT_MOD_WAVE
- $NI_BEATMASHER_LENGTH_16TH
- $NI_BEATMASHER_LENGTH_32ND
- $NI_BEATMASHER_LENGTH_8TH
- $NI_BEATMASHER_LENGTH_8TH_DOTTED
- $NI_BEATMASHER_LENGTH_BAR
- $NI_BEATMASHER_LENGTH_HALF
- $NI_BEATMASHER_LENGTH_HALF_DOTTED
- $NI_BEATMASHER_LENGTH_QUARTER
- $NI_BEATMASHER_LENGTH_QUARTER_DOTTED
- $NI_WT_MOD_TUNE_UNIT_HZ
- $NI_WT_MOD_TUNE_UNIT_RATIO
- $NI_WT_MOD_TUNE_UNIT_SEMITONES
- $NI_WT_MOD_TYPE_FM1
- $NI_WT_MOD_TYPE_FM2
- $NI_WT_MOD_TYPE_FM3
- $NI_WT_MOD_TYPE_MIX
- $NI_WT_MOD_TYPE_OFF
- $NI_WT_MOD_TYPE_PM1
- $NI_WT_MOD_TYPE_PM2
- $NI_WT_MOD_TYPE_PM3
- $NI_WT_MOD_TYPE_RM
- $NI_WT_MOD_WAVE_SINE
- $NI_WT_MOD_WAVE_TRIANGLE
- $NI_WT_MOD_WAVE_TX2
- $NI_WT_MOD_WAVE_TX3
- $NI_WT_MOD_WAVE_TX4
- $NI_WT_MOD_WAVE_TX5
- $NI_WT_MOD_WAVE_TX6
- $NI_WT_MOD_WAVE_TX7
- $NI_WT_MOD_WAVE_TX8

</details>

#### Command

- set_note_controller
- set_poly_at
- detect_key
- detect_tempo

#### Callback

- on note_controller

## Version 0.7.27

### KONTAKT 7.10.0 ready

Added Syntax highlighting. (Kontakt 7.8.0 - 7.10.0 etc)

#### Variable

<details>
<summary>Click to expand</summary>

- $CONTROL_PAR_DND_ACCEPT_MIDI
- $CONTROL_PAR_IDENTIFIER
- $CONTROL_PAR_SHORT_NAME
- $CONTROL_PAR_VALUEPOS_Y
- $CONTROL_PAR_WAVETABLE
- $EFFECT_TYPE_BIGFUZZ
- $EFFECT_TYPE_BITE
- $EFFECT_TYPE_DIRT
- $EFFECT_TYPE_EP_PREAMPS
- $EFFECT_TYPE_FREAK
- $EFFECT_TYPE_FUZZ
- $EFFECT_TYPE_RAUM
- $EFFECT_TYPE_STEREO_TUNE
- $EFFECT_TYPE_TWINDELAY
- $EFFECT_TYPE_VIBRATO_CHORUS
- $EFFECT_TYPE_WOWFLUTTER
- $ENGINE_PAR_BIGFUZZ_BASS
- $ENGINE_PAR_BIGFUZZ_MONO
- $ENGINE_PAR_BIGFUZZ_SUSTAIN
- $ENGINE_PAR_BIGFUZZ_TONE
- $ENGINE_PAR_BIGFUZZ_TREBLE
- $ENGINE_PAR_EPP_DRIVE
- $ENGINE_PAR_EPP_DRIVE_MODE
- $ENGINE_PAR_EPP_EQ_BASS
- $ENGINE_PAR_EPP_EQ_MID
- $ENGINE_PAR_EPP_EQ_MODE
- $ENGINE_PAR_EPP_EQ_TREBLE
- $ENGINE_PAR_EPP_MONO
- $ENGINE_PAR_EPP_PASSIVE_BASS
- $ENGINE_PAR_EPP_TREMOLO_AMOUNT
- $ENGINE_PAR_EPP_TREMOLO_MODE
- $ENGINE_PAR_EPP_TREMOLO_RATE
- $ENGINE_PAR_EPP_TREMOLO_RATE_UNIT
- $ENGINE_PAR_EPP_TREMOLO_WAVE
- $ENGINE_PAR_EPP_TREMOLO_WIDTH
- $ENGINE_PAR_FLEXENV_LOOP_END
- $ENGINE_PAR_FLEXENV_LOOP_START
- $ENGINE_PAR_FLEXENV_NUM_STAGES
- $ENGINE_PAR_FLEXENV_STAGE_LEVEL
- $ENGINE_PAR_FLEXENV_STAGE_SLOPE
- $ENGINE_PAR_FLEXENV_STAGE_TIME
- $ENGINE_PAR_FUZZ_AMOUNT
- $ENGINE_PAR_FUZZ_BASS
- $ENGINE_PAR_FUZZ_MONO
- $ENGINE_PAR_FUZZ_TREBLE
- $ENGINE_PAR_RINGMOD_LFO_RATE_UNIT
- $ENGINE_PAR_SEQ_HP_FREQ
- $ENGINE_PAR_STEPSEQ_STEPS
- $ENGINE_PAR_STEREOTUNE_DRIFT
- $ENGINE_PAR_STEREOTUNE_MIX
- $ENGINE_PAR_STEREOTUNE_SPLIT
- $ENGINE_PAR_STEREOTUNE_SPREAD
- $EVENT_PAR_MIDI_CHANNNEL
- $NI_CB_TYPE_UI_CONTROLS
- $NI_CONTROL_TYPE_BUTTON
- $NI_CONTROL_TYPE_FILE_SELECTOR
- $NI_CONTROL_TYPE_KNOB
- $NI_CONTROL_TYPE_LABEL
- $NI_CONTROL_TYPE_LEVEL_METER
- $NI_CONTROL_TYPE_MENU
- $NI_CONTROL_TYPE_MOUSE_AREA
- $NI_CONTROL_TYPE_NONE
- $NI_CONTROL_TYPE_PANEL
- $NI_CONTROL_TYPE_SLIDER
- $NI_CONTROL_TYPE_SWITCH
- $NI_CONTROL_TYPE_TABLE
- $NI_CONTROL_TYPE_TEXT_EDIT
- $NI_CONTROL_TYPE_VALUE_EDIT
- $NI_CONTROL_TYPE_WAVEFORM
- $NI_CONTROL_TYPE_WAVETABLE
- $NI_CONTROL_TYPE_XY
- $NI_DATE_DAY
- $NI_DATE_MONTH
- $NI_DATE_YEAR
- $NI_EPP_DRIVE_MODE_BYPASS
- $NI_EPP_DRIVE_MODE_DE_TUBE
- $NI_EPP_DRIVE_MODE_TAPE
- $NI_EPP_DRIVE_MODE_TRANSISTOR
- $NI_EPP_DRIVE_MODE_US_TUBE
- $NI_EPP_EQ_MODE_70S
- $NI_EPP_EQ_MODE_80S
- $NI_EPP_EQ_MODE_BYPASS
- $NI_EPP_EQ_MODE_E_GRAND
- $NI_EPP_EQ_MODE_PASSIVE
- $NI_EPP_TREMOLO_MODE_70S
- $NI_EPP_TREMOLO_MODE_80S
- $NI_EPP_TREMOLO_MODE_BYPASS
- $NI_EPP_TREMOLO_MODE_E_GRAND
- $NI_EPP_TREMOLO_MODE_GUITAR
- $NI_EPP_TREMOLO_MODE_SYNTH
- $NI_EPP_TREMOLO_WAVE_SAW_DOWN
- $NI_EPP_TREMOLO_WAVE_SAW_UP
- $NI_EPP_TREMOLO_WAVE_SINE
- $NI_EPP_TREMOLO_WAVE_SQUARE
- $NI_EPP_TREMOLO_WAVE_TRIANGLE
- $NI_HQI_MODE_HIGH
- $NI_HQI_MODE_PERFECT
- $NI_HQI_MODE_STANDARD
- $NI_S1200_FILTER_HIGH
- $NI_S1200_FILTER_HIGH_MID
- $NI_S1200_FILTER_LOW
- $NI_S1200_FILTER_LOW_MID
- $NI_S1200_FILTER_NONE
- $NI_SOURCE_MODE_BEAT_MACHINE
- $NI_SOURCE_MODE_DFD
- $NI_SOURCE_MODE_MP60_MACHINE
- $NI_SOURCE_MODE_S1200_MACHINE
- $NI_SOURCE_MODE_SAMPLER
- $NI_SOURCE_MODE_TIME_MACHINE_1
- $NI_SOURCE_MODE_TIME_MACHINE_2
- $NI_SOURCE_MODE_TIME_MACHINE_PRO
- $NI_SOURCE_MODE_TONE_MACHINE
- $NI_SOURCE_MODE_WAVETABLE
- $NI_TIME_HOUR
- $NI_TIME_MINUTE
- $NI_TIME_SECOND
- $NI_WT_FORM_2BLINDS
- $NI_WT_FORM_4BLINDS
- $NI_WT_FORM_6BLINDS
- $NI_WT_FORM_8BLINDS
- $NI_WT_FORM_ASYM2M
- $NI_WT_FORM_ASYM2MP
- $NI_WT_FORM_ASYM2P
- $NI_WT_FORM_BEND2M
- $NI_WT_FORM_BEND2MP
- $NI_WT_FORM_BEND2P
- $NI_WT_FORM_EXP
- $NI_WT_FORM_FOLD
- $NI_WT_FORM_LOG
- $NI_WT_FORM_LOGEXP
- $NI_WT_FORM_SATURATE
- $NI_WT_FORM_SEESAW
- $NI_WT_FORM_SYNC4
- $NI_WT_FORM_SYNC5
- $NI_WT_FORM_SYNC6
- $NI_WT_FORM_WRAP
- $UI_WAVEFORM_TABLE_IS_BIPOL
- $UI_WF_PROP_TABLE_IDX_HIGHL

</details>

## Version 0.7.26

Typo in Changelog 0.7.25

`on ui_updates` -\> `on ui_controls`

## Version 0.7.25

### KONTAKT 7.8.0 ready

Added Syntax highlighting. (Kontakt 7.6.0 - 7.8.0)

#### Callback

- on ui_controls

#### Variable

- $CONTROL_PAR_TYPE
- $CONTROL_PAR_CUSTOM_ID
- $NI_UI_ID

## Version 0.7.24

### Fixed internal compiler program bug

- https://github.com/r-koubou/KSPSyntaxParser/pull/13

### KONTAKT 7.5.0 ready

Added Syntax highlighting. (Kontakt 7.2.0 - 7.5.0)

#### Limitation

- `continue statement` is not supported yet in internal compiler.

#### Variable

- 7.2.0
  - $ENGINE_PAR_HP_FREQ
  - $ENGINE_PAR_HQI_MODE
  - $ENGINE_PAR_S1200_FILTER_MODE
  - $ENGINE_PAR_SEQ_HP
  - $ENGINE_PAR_SEQ_LP
  - $ENGINE_PAR_SEQ_LP_FREQ
  - $ENGINE_PAR_TMPRO_KEEP_FORMANTS
  - $ENGINE_PAR_TRACKING
  - $NI_KONTAKT_IS_STANDALONE

- 7.5.0
  - $ENGINE_PAR_BITE_BITS
  - $ENGINE_PAR_BITE_CRUNCH
  - $ENGINE_PAR_BITE_DC_QUANT
  - $ENGINE_PAR_BITE_DITHER
  - $ENGINE_PAR_BITE_EXPAND
  - $ENGINE_PAR_BITE_FREQUENCY
  - $ENGINE_PAR_BITE_HPF
  - $ENGINE_PAR_BITE_JITTER
  - $ENGINE_PAR_BITE_MIX
  - $ENGINE_PAR_BITE_POSTFILTER
  - $ENGINE_PAR_BITE_PREFILTER
  - $ENGINE_PAR_BITE_SATURATE
  - $ENGINE_PAR_DIRT_AMOUNTA
  - $ENGINE_PAR_DIRT_AMOUNTB
  - $ENGINE_PAR_DIRT_BIASA
  - $ENGINE_PAR_DIRT_BIASB
  - $ENGINE_PAR_DIRT_BLEND
  - $ENGINE_PAR_DIRT_DRIVEA
  - $ENGINE_PAR_DIRT_DRIVEB
  - $ENGINE_PAR_DIRT_MIX
  - $ENGINE_PAR_DIRT_MODEA
  - $ENGINE_PAR_DIRT_MODEB
  - $ENGINE_PAR_DIRT_ROUTING
  - $ENGINE_PAR_DIRT_SAFETYA
  - $ENGINE_PAR_DIRT_SAFETYB
  - $ENGINE_PAR_DIRT_TILTA
  - $ENGINE_PAR_DIRT_TILTB
  - $ENGINE_PAR_FREAK_ANTIFOLD
  - $ENGINE_PAR_FREAK_BP_FILTER
  - $ENGINE_PAR_FREAK_BP_FREQ
  - $ENGINE_PAR_FREAK_CARRIER
  - $ENGINE_PAR_FREAK_CONTOUR
  - $ENGINE_PAR_FREAK_DEMOD
  - $ENGINE_PAR_FREAK_FEEDBACK
  - $ENGINE_PAR_FREAK_FREQUENCY
  - $ENGINE_PAR_FREAK_GATE
  - $ENGINE_PAR_FREAK_HARMONICS
  - $ENGINE_PAR_FREAK_MIX
  - $ENGINE_PAR_FREAK_MODE
  - $ENGINE_PAR_FREAK_RELEASE
  - $ENGINE_PAR_FREAK_STEREO
  - $ENGINE_PAR_FREAK_TUNING
  - $ENGINE_PAR_FREAK_TYPE
  - $ENGINE_PAR_FREAK_WIDE_RANGE
  - $ENGINE_PAR_FREAK_WIDTH
  - $ENGINE_PAR_RAUM_DAMPING
  - $ENGINE_PAR_RAUM_DECAY
  - $ENGINE_PAR_RAUM_DIFFUSION
  - $ENGINE_PAR_RAUM_FEEDBACK
  - $ENGINE_PAR_RAUM_FREEZE
  - $ENGINE_PAR_RAUM_HIGHCUT
  - $ENGINE_PAR_RAUM_LOWSHELF
  - $ENGINE_PAR_RAUM_MOD
  - $ENGINE_PAR_RAUM_PREDELAY
  - $ENGINE_PAR_RAUM_PREDELAY_UNIT
  - $ENGINE_PAR_RAUM_RATE
  - $ENGINE_PAR_RAUM_REVERB
  - $ENGINE_PAR_RAUM_SIZE
  - $ENGINE_PAR_RAUM_SPARSE
  - $ENGINE_PAR_RAUM_TYPE
  - $ENGINE_PAR_TDL_CROSS_FEEDBACK
  - $ENGINE_PAR_TDL_FEEDBACK_L
  - $ENGINE_PAR_TDL_FEEDBACK_R
  - $ENGINE_PAR_TDL_LEVEL_L
  - $ENGINE_PAR_TDL_LEVEL_R
  - $ENGINE_PAR_TDL_PREDELAY_L
  - $ENGINE_PAR_TDL_PREDELAY_L_UNIT
  - $ENGINE_PAR_TDL_PREDELAY_R
  - $ENGINE_PAR_TDL_PREDELAY_R_UNIT
  - $ENGINE_PAR_TDL_TIME_L
  - $ENGINE_PAR_TDL_TIME_L_UNIT
  - $ENGINE_PAR_TDL_TIME_R
  - $ENGINE_PAR_TDL_TIME_R_UNIT
  - $ENGINE_PAR_TDL_WIDTH
  - $ENGINE_PAR_VC_BLEND
  - $ENGINE_PAR_VC_COLOR
  - $ENGINE_PAR_VC_DEPTH
  - $ENGINE_PAR_VC_MIX
  - $ENGINE_PAR_VC_RATE
  - $ENGINE_PAR_VC_WIDTH
  - $ENGINE_PAR_WOWFLUTTER_AGE
  - $ENGINE_PAR_WOWFLUTTER_FLUTTER
  - $ENGINE_PAR_WOWFLUTTER_GATE
  - $ENGINE_PAR_WOWFLUTTER_MIX
  - $ENGINE_PAR_WOWFLUTTER_SATURATION
  - $ENGINE_PAR_WOWFLUTTER_SCRAPE
  - $ENGINE_PAR_WOWFLUTTER_SPEED
  - $ENGINE_PAR_WOWFLUTTER_STEREO
  - $ENGINE_PAR_WOWFLUTTER_WOW
  - $NI_BITE_HPF_MODE_100
  - $NI_BITE_HPF_MODE_200
  - $NI_BITE_HPF_MODE_5
  - $NI_DIRT_MODE_I
  - $NI_DIRT_MODE_I
  - $NI_DIRT_MODE_II
  - $NI_DIRT_MODE_II
  - $NI_DIRT_MODE_III
  - $NI_DIRT_MODE_III
  - $NI_DIRT_ROUTING_ATOB
  - $NI_DIRT_ROUTING_BTOA
  - $NI_DIRT_ROUTING_PARALLEL
  - $NI_FREAK_MODE_OSCILLATOR
  - $NI_FREAK_MODE_RADIO
  - $NI_FREAK_MODE_SIDECHAIN
  - $NI_RAUM_TYPE_AIRY
  - $NI_RAUM_TYPE_COSMIC
  - $NI_RAUM_TYPE_GROUNDED
  - $NI_VC_COLOR_TYPE_A
  - $NI_VC_COLOR_TYPE_B
  - $NI_VC_COLOR_TYPE_C
  - $NI_VC_DEPTH_1
  - $NI_VC_DEPTH_2
  - $NI_VC_DEPTH_3
  - $NI_VC_DEPTH_4
  - $NI_VC_DEPTH_5
  - $NI_VC_DEPTH_6
  - $ENGINE_PAR_SOURCE_MODE
  - $ZONE_PAR_SELECTED

#### Command

- 7.2.0
  - mf_get_last_filename
- 7.5.0
  - get_sel_zones_idx
  - set_map_editor_event_color

## Version 0.7.23

Fixed internal compiler program bug

## Version 0.7.22

Fixed internal compiler definition file

## Version 0.7.21

### Fixed incorrect variable name

- [#16](https://github.com/r-koubou/vscode-ksp/issues/16)
  - `$EVENT_PAR_MOD_VALUE_ID_FULL` -> `$EVENT_PAR_MOD_VALUE_ID`

## Version 0.7.20

### KONTAKT 7.1.0 ready

Added Syntax highlighting.

#### Variable

- $BUS_IDX
- $CHANNEL_L
- $CHANNEL_R
- $EFFECT_TYPE_PSYCHEDELAY
- $EFFECT_TYPE_RINGMOD
- $ENGINE_PAR_FLEXENV_LOOP
- $ENGINE_PAR_FLEXENV_ONESHOT
- $ENGINE_PAR_IRC_AUTO_GAIN
- $ENGINE_PAR_LFO_PHASE
- $ENGINE_PAR_MOD_TARGET_MP_INTENSITY
- $ENGINE_PAR_PSYDL_CROSS_FEEDBACK
- $ENGINE_PAR_PSYDL_DETUNE
- $ENGINE_PAR_PSYDL_DETUNE_STEREO
- $ENGINE_PAR_PSYDL_FEEDBACK
- $ENGINE_PAR_PSYDL_LR_OFFSET
- $ENGINE_PAR_PSYDL_PITCH
- $ENGINE_PAR_PSYDL_REVERSE
- $ENGINE_PAR_PSYDL_REVERSE_STEREO
- $ENGINE_PAR_PSYDL_TIME
- $ENGINE_PAR_PSYDL_TIME_UNIT
- $ENGINE_PAR_RINGMOD_EDGE
- $ENGINE_PAR_RINGMOD_FAST_MODE
- $ENGINE_PAR_RINGMOD_FM
- $ENGINE_PAR_RINGMOD_FREQUENCY
- $ENGINE_PAR_RINGMOD_LFO_AMOUNT
- $ENGINE_PAR_RINGMOD_LFO_RATE
- $ENGINE_PAR_RINGMOD_LFO_WAVE
- $ENGINE_PAR_RINGMOD_RING
- $ENGINE_PAR_STEPMOD_ONESHOT
- $ENGINE_PAR_STEPMOD_STEPS
- $ENGINE_PAR_STEPMOD_STEP_VALUE
- $ENGINE_PAR_STEPSEQ_NUM_STEPS
- $ENGINE_PAR_STEPSEQ_ONESHOT
- $ENGINE_PAR_STEPSEQ_STEP_VALUE
- $ENGINE_PAR_TM_LEGATO
- $EVENT_PAR_MOD_VALUE_ID_FULL
- $FILTER_TYPE_SV_BP6
- $FILTER_TYPE_SV_HP6
- $FILTER_TYPE_SV_LP6
- $FILTER_TYPE_SV_NOTCH6
- $GROUP_IDX
- $NI_NOT_FOUND
- $NI_RINGMOD_LFO_WAVE_SINE
- $NI_RINGMOD_LFO_WAVE_SQUARE
- $NI_ZONE_STATUS_EMPTY
- $NI_ZONE_STATUS_IGNORED
- $NI_ZONE_STATUS_LOADED
- $NI_ZONE_STATUS_PURGED
- $NUM_KNOBS
- $SLOT_IDX
- $ZONE_PAR_BPM
- $ZONE_PAR_SAMPLE_RATE
- %ID
- ?XY1

#### Command

- cbrt
- get_control_par_real_arr
- get_group_idx
- get_mod_idx
- get_num_zones
- get_target_idx
- get_zone_id
- get_zone_status
- int
- real
- set_control_par_real_arr
- sgn
- signbit

## Version 0.7.19

### KONTAKT 6.7.0 ready

Added Syntax highlighting.

#### Variable

- $EFFECT_TYPE_BASSINVADER
- $ENGINE_PAR_BASSINVADER_VOLUME
- $ENGINE_PAR_BASSINVADER_TREBLE
- $ENGINE_PAR_BASSINVADER_HI_MID
- $ENGINE_PAR_BASSINVADER_LO_MID
- $ENGINE_PAR_BASSINVADER_BASS
- $ENGINE_PAR_BASSINVADER_BOOST
- $ENGINE_PAR_BASSINVADER_MASTER
- $ENGINE_PAR_BASSINVADER_LO_CUT
- $ENGINE_PAR_BASSINVADER_MID_CONTOUR
- $ENGINE_PAR_BASSINVADER_HI_BOOST

- $EFFECT_TYPE_BASSPRO
- $ENGINE_PAR_BASSPRO_GAIN
- $ENGINE_PAR_BASSPRO_BASS
- $ENGINE_PAR_BASSPRO_MID
- $ENGINE_PAR_BASSPRO_MIDFREQ
- $ENGINE_PAR_BASSPRO_TREBLE
- $ENGINE_PAR_BASSPRO_DRIVE
- $ENGINE_PAR_BASSPRO_MASTER
- $ENGINE_PAR_BASSPRO_GEQ_40
- $ENGINE_PAR_BASSPRO_GEQ_90
- $ENGINE_PAR_BASSPRO_GEQ_180
- $ENGINE_PAR_BASSPRO_GEQ_300
- $ENGINE_PAR_BASSPRO_GEQ_500
- $ENGINE_PAR_BASSPRO_GEQ_1K
- $ENGINE_PAR_BASSPRO_GEQ_2K
- $ENGINE_PAR_BASSPRO_GEQ_4K
- $ENGINE_PAR_BASSPRO_GEQ_10K
- $ENGINE_PAR_BASSPRO_GEQ_VOLUME
- $ENGINE_PAR_BASSPRO_ULTRALO
- $ENGINE_PAR_BASSPRO_ULTRAHI
- $ENGINE_PAR_BASSPRO_BRIGHT
- $ENGINE_PAR_BASSPRO_GEQ
- $ENGINE_PAR_BASSPRO_MONO

## Version 0.7.17

### KONTAKT 6.6.0 ready

Added Syntax highlighting.

#### Variable

- $CONTROL_PAR_RANGE_MIN
- $CONTROL_PAR_RANGE_MAX
- $EVENT_PAR_MOD_VALUE_ID
- $OUTPUT_TYPE_AUX_OUT
- $OUTPUT_TYPE_BUS_OUT
- $OUTPUT_TYPE_DEFAULT
- $OUTPUT_TYPE_MASTER_OUT


#### Command

- redirect_output

## Version 0.7.16

### KONTAKT 6.5.0 ready

Added Syntax highlighting.

#### Variable

- $EFFECT_TYPE_CRYWAH
- $ENGINE_PAR_COMP_LINK
- $ENGINE_PAR_COMP_TYPE
- $ENGINE_PAR_ENVF_ADAPTION
- $ENGINE_PAR_ENVF_ATTACK
- $ENGINE_PAR_ENVF_GAIN_BOOST
- $ENGINE_PAR_ENVF_RELEASE
- $ENGINE_PAR_ENV_AHD
- $ENGINE_PAR_ENV_DBD_EASY
- $ENGINE_PAR_IRC_ER_LR_BOUNDARY
- $ENGINE_PAR_IRC_REVERSE
- $ENGINE_PAR_LFO_NORMALIZE
- $ENGINE_PAR_POST_FX_SLOT
- $ENGINE_PAR_SCOMP_LINK
- $ENGINE_PAR_STEREO_PSEUDO
- $NI_COMP_TYPE_CLASSIC
- $NI_COMP_TYPE_ENHANCED
- $NI_COMP_TYPE_PRO
- $NI_KONTAKT_IS_HEADLESS
#### Command

- get_event_mark

## Version 0.7.15

### Fixed internal compiler bug

[issue #11](https://github.com/r-koubou/KSPSyntaxParser/issues/11)

Fixed a bug that prevented the parser from working properly with the following commands

- int_to_real
- real_to_int
- detect_instrument_type
- detect_pitch
- detect_loudness
- detect_peak
- detect_rms
- detect_sample_type
- detect_drum_type


## Version 0.7.14

### Fixed internal compiler bug

- [issue #11](https://github.com/r-koubou/KSPSyntaxParser/issues/11)

### Bug fixes

- Syntax highlighting
- Hover

## Version 0.7.13

### KONTAKT 6.4.0 ready

Added Syntax highlighting.

#### Variable

- $CONTROL_PAR_MIDI_EXPORT_AREA_IDX
- $EFFECT_TYPE_SUPERGT
- $EFFECT_TYPE_TRANSLIM
- $ENGINE_PAR_LR_SWAP
- $ENGINE_PAR_PHASE_INVERT
- $ENGINE_PAR_SUPERGT_ATTACK
- $ENGINE_PAR_SUPERGT_CHANNEL_LINK_MODE
- $ENGINE_PAR_SUPERGT_CHARACTER
- $ENGINE_PAR_SUPERGT_CHAR_MODE
- $ENGINE_PAR_SUPERGT_COMPRESS
- $ENGINE_PAR_SUPERGT_HPF_MODE
- $ENGINE_PAR_SUPERGT_MIX
- $ENGINE_PAR_SUPERGT_RELEASE
- $ENGINE_PAR_SUPERGT_SATURATION
- $ENGINE_PAR_SUPERGT_SAT_MODE
- $ENGINE_PAR_SUPERGT_TRIM
- $ENGINE_PAR_TRANSLIM_CEILING
- $ENGINE_PAR_TRANSLIM_RELEASE
- $ENGINE_PAR_TRANSLIM_THRESHOLD
- $EVENT_PAR_4
- $EVENT_PAR_5
- $EVENT_PAR_6
- $EVENT_PAR_7
- $EVENT_PAR_8
- $EVENT_PAR_9
- $EVENT_PAR_10
- $EVENT_PAR_11
- $EVENT_PAR_12
- $EVENT_PAR_13
- $EVENT_PAR_14
- $EVENT_PAR_15
- $EVENT_PAR_CUSTOM
- $NI_SEND_BUS
- $NI_INSERT_BUS
- $NI_MAIN_BUS
- $NI_SUPERGT_CHANNEL_LINK_MODE_DUAL_MONO
- $NI_SUPERGT_CHANNEL_LINK_MODE_MS
- $NI_SUPERGT_CHANNEL_LINK_MODE_STEREO
- $NI_SUPERGT_CHAR_MODE_BRIGHT
- $NI_SUPERGT_CHAR_MODE_FAT
- $NI_SUPERGT_CHAR_MODE_WARM
- $NI_SUPERGT_HPF_MODE_100
- $NI_SUPERGT_HPF_MODE_300
- $NI_SUPERGT_HPF_MODE_OFF
- $NI_SUPERGT_SAT_MODE_HOT
- $NI_SUPERGT_SAT_MODE_MILD
- $NI_SUPERGT_SAT_MODE_MODERATE

#### Command

- mf_copy_export_area
- mf_set_num_export_areas


## Version 0.7.10

Added Syntax highlighting.

#### Variable

- $EFFECT_TYPE_REPLIKA
- $EFFECT_TYPE_REVERB2
- $ENGINE_PAR_PR_DECAY
- $ENGINE_PAR_PR_HIDAMP
- $ENGINE_PAR_PR_LOWSHELF
- $ENGINE_PAR_PR_PREDELAY
- $ENGINE_PAR_RDL_AMOUNT
- $ENGINE_PAR_RDL_BBDTYPE
- $ENGINE_PAR_RDL_DENSE
- $ENGINE_PAR_RDL_DEPTH
- $ENGINE_PAR_RDL_FEEDBACK
- $ENGINE_PAR_RDL_FLUTTER
- $ENGINE_PAR_RDL_HIGHCUT
- $ENGINE_PAR_RDL_LOWCUT
- $ENGINE_PAR_RDL_MODULATION
- $ENGINE_PAR_RDL_NOISE
- $ENGINE_PAR_RDL_PINGPONG
- $ENGINE_PAR_RDL_QUALITY
- $ENGINE_PAR_RDL_RATE
- $ENGINE_PAR_RDL_SATURATION
- $ENGINE_PAR_RDL_SIZE
- $ENGINE_PAR_RDL_STEREO
- $ENGINE_PAR_RDL_TAPEAGE
- $ENGINE_PAR_RDL_TIME
- $ENGINE_PAR_RDL_TIME_UNIT
- $ENGINE_PAR_RDL_TYPE
- $ENGINE_PAR_RV2_DAMPING
- $ENGINE_PAR_RV2_DIFF
- $ENGINE_PAR_RV2_HIGHCUT
- $ENGINE_PAR_RV2_LOWSHELF
- $ENGINE_PAR_RV2_MOD
- $ENGINE_PAR_RV2_PREDELAY
- $ENGINE_PAR_RV2_SIZE
- $ENGINE_PAR_RV2_STEREO
- $ENGINE_PAR_RV2_TIME
- $ENGINE_PAR_RV2_TYPE
- $ENGINE_PAR_WT_FORM
- $ENGINE_PAR_WT_FORM_MODE
- $ENGINE_PAR_WT_PHASE
- $ENGINE_PAR_WT_POSITION
- $ENGINE_PAR_WT_QUALITY
- $NI_FLAIR_MODE_STANDARD
- $NI_FLAIR_MODE_THRU_ZERO
- $NI_REPLIKA_TYPE_ANALOGUE
- $NI_REVERB2_TYPE_HALL
- $NI_WT_QUALITY_BEST
- $NI_WT_QUALITY_HIGH
- $NI_WT_QUALITY_LOFI
- $NI_WT_QUALITY_MEDIUM


## Version 0.7.9

### KONTAKT 6.3.1 ready

Added Syntax highlighting.

#### Variable

- $ENGINE_PAR_LFO_RANDOM
- $EVENT_PAR_REL_VELOCITY
- $NI_DETECT_INSTRUMENT_TYPE
- $NI_DETECT_INSTRUMENT_TYPE_BASS
- $NI_DETECT_INSTRUMENT_TYPE_BOWED_STRING
- $NI_DETECT_INSTRUMENT_TYPE_BRASS
- $NI_DETECT_INSTRUMENT_TYPE_FLUTE
- $NI_DETECT_INSTRUMENT_TYPE_GUITAR
- $NI_DETECT_INSTRUMENT_TYPE_INVALID
- $NI_DETECT_INSTRUMENT_TYPE_KEYBOARD
- $NI_DETECT_INSTRUMENT_TYPE_MALLET
- $NI_DETECT_INSTRUMENT_TYPE_ORGAN
- $NI_DETECT_INSTRUMENT_TYPE_PLUCKED_STRING
- $NI_DETECT_INSTRUMENT_TYPE_REED
- $NI_DETECT_INSTRUMENT_TYPE_SYNTH
- $NI_DETECT_INSTRUMENT_TYPE_VOCAL

### Command

- detect_instrument_type
- get_control_par_str_arr (Undocumented)

## Version 0.7.8

### Fixed internal compiler bug

#### ui_waveform variable initializer

Expected

~~~
declare ui_waveform $variable(6,6) {OK}
~~~

Bug

~~~
{$Waveform : Argument count not compatible for ui_waveform initializer (Argument count: 3, Required count: 2)}
declare ui_waveform $variable(6,6)
~~~

## Version 0.7.7

### KONTAKT 6.2.0 ready

Added Syntax highlighting.

#### Command

- wait_async
- get_loop_par
- get_loop_sample
- get_zone_par
- is_zone_empty
- set_loop_par
- set_num_user_zones
- set_sample
- set_zone_par
- detect_pitch
- detect_loudness
- detect_peak
- detect_rms
- detect_sample_type
- detect_drum_type

#### Variable

- $CONTROL_PAR_DND_ACCEPT_ARRAY
- $CONTROL_PAR_DND_ACCEPT_AUDIO
- $CONTROL_PAR_DND_MID$Y
- $CONTROL_PAR_FONT_TYPE_ON_HOVER
- $CONTROL_PAR_GRID_HEIGHT
- $CONTROL_PAR_RECEIVE_DRAG_EVENTS
- $EFFECT_TYPE_CHORAL
- $EFFECT_TYPE_FLAIR
- $EFFECT_TYPE_PHASIS
- $ENGINE_PAR_CHORAL_AMOUNT
- $ENGINE_PAR_CHORAL_DELAY
- $ENGINE_PAR_CHORAL_FEEDBACK
- $ENGINE_PAR_CHORAL_INVERT_PHASE
- $ENGINE_PAR_CHORAL_MIX
- $ENGINE_PAR_CHORAL_MODE
- $ENGINE_PAR_CHORAL_RATE
- $ENGINE_PAR_CHORAL_SCATTER
- $ENGINE_PAR_CHORAL_VOICES
- $ENGINE_PAR_CHORAL_WIDTH
- $ENGINE_PAR_FLAIR_AMOUNT
- $ENGINE_PAR_FLAIR_CHORD
- $ENGINE_PAR_FLAIR_DAMPING
- $ENGINE_PAR_FLAIR_DETUNE
- $ENGINE_PAR_FLAIR_FEEDBACK
- $ENGINE_PAR_FLAIR_INVERT_PHASE
- $ENGINE_PAR_FLAIR_MIX
- $ENGINE_PAR_FLAIR_MODE
- $ENGINE_PAR_FLAIR_OFFSET
- $ENGINE_PAR_FLAIR_PITCH
- $ENGINE_PAR_FLAIR_RATE
- $ENGINE_PAR_FLAIR_RATE_UNIT
- $ENGINE_PAR_FLAIR_SCANMODE
- $ENGINE_PAR_FLAIR_VOICES
- $ENGINE_PAR_FLAIR_WIDTH
- $ENGINE_PAR_PHASIS_AMOUNT
- $ENGINE_PAR_PHASIS_CENTER
- $ENGINE_PAR_PHASIS_FEEDBACK
- $ENGINE_PAR_PHASIS_INVERT_MOD_MIX
- $ENGINE_PAR_PHASIS_INVERT_PHASE
- $ENGINE_PAR_PHASIS_MIX
- $ENGINE_PAR_PHASIS_MOD_MIX
- $ENGINE_PAR_PHASIS_NOTCHES
- $ENGINE_PAR_PHASIS_RATE
- $ENGINE_PAR_PHASIS_RATE_UNIT
- $ENGINE_PAR_PHASIS_SPREAD
- $ENGINE_PAR_PHASIS_STEREO
- $ENGINE_PAR_PHASIS_ULTRA
- $ENGINE_PAR_WT_FORM_MODE
- $ENGINE_PAR_WT_INHARMONIC
- $LOOP_PAR_COUNT
- $LOOP_PAR_LENGTH
- $LOOP_PAR_MODE
- $LOOP_PAR_START
- $LOOP_PAR_TUNING
- $LOOP_PAR_XFADE
- $NI_BAR_START_POSITION
- $NI_CHORAL_MODE_SYNTH
- $NI_CHORAL_MODE_ENSEMBLE
- $NI_CHORAL_MODE_DIMENSION
- $NI_CHORAL_MODE_UNIVERSAL
- $NI_FILE_EXTENSION
- $NI_FILE_FULL_PATH
- $NI_FILE_FULL_PATH_OS
- $NI_FILE_NAME
- $NI_FLAIR_MODE_SCAN
- $NI_DETECT_DRUM_TYPE_CLAP
- $NI_DETECT_DRUM_TYPE_CLOSED_HH
- $NI_DETECT_DRUM_TYPE_CYMBAL
- $NI_DETECT_DRUM_TYPE_INVALID
- $NI_DETECT_DRUM_TYPE_KICK
- $NI_DETECT_DRUM_TYPE_OPEN_HH
- $NI_DETECT_DRUM_TYPE_PERC_DRUM
- $NI_DETECT_DRUM_TYPE_PERC_OTHER
- $NI_DETECT_DRUM_TYPE_SHAKER
- $NI_DETECT_DRUM_TYPE_SNARE
- $NI_DETECT_DRUM_TYPE_TOM
- $NI_DETECT_SAMPLE_TYPE_DRUM
- $NI_DETECT_SAMPLE_TYPE_INSTRUMENT
- $NI_DETECT_SAMPLE_TYPE_INVALID
- $NI_DND_ACCEPT_MULTIPLE
- $NI_DND_ACCEPT_NONE
- $NI_DND_ACCEPT_ONE
- $NI_FLAIR_SCANMODE_SAW_DOWN
- $NI_FLAIR_SCANMODE_SAW_UP
- $NI_FLAIR_SCANMODE_TRIANGLE
- $NI_MOUSE_EVENT_TYPE_DND_DRAG
- $NI_MOUSE_EVENT_TYPE_DND_DROP
- $NI_MOUSE_OVER_CONTROL
- $NI_MOUSE_EVENT_TYPE_DROP
- $NI_WT_FORM_ASYMM
- $NI_WT_FORM_ASYMMP
- $NI_WT_FORM_ASYMP
- $NI_WT_FORM_BENDM
- $NI_WT_FORM_BENDMP
- $NI_WT_FORM_BENDP
- $NI_WT_FORM_FLIP
- $NI_WT_FORM_LINEAR
- $NI_WT_FORM_MIRROR
- $NI_WT_FORM_PWM
- $NI_WT_FORM_QUANTIZE
- $NI_WT_FORM_SYNC1
- $NI_WT_FORM_SYNC2
- $NI_WT_FORM_SYNC3
- $ZONE_PAR_FADE_HIGH_KEY
- $ZONE_PAR_FADE_HIGH_VELO
- $ZONE_PAR_FADE_LOW_KEY
- $ZONE_PAR_FADE_LOW_VELO
- $ZONE_PAR_GROUP
- $ZONE_PAR_HIGH_KEY
- $ZONE_PAR_HIGH_VELO
- $ZONE_PAR_LOW_KEY
- $ZONE_PAR_LOW_VELO
- $ZONE_PAR_PAN
- $ZONE_PAR_ROOT_KEY
- $ZONE_PAR_SAMPLE_END
- $ZONE_PAR_SAMPLE_MOD_RANGE
- $ZONE_PAR_SAMPLE_START
- $ZONE_PAR_TUNE
- $ZONE_PAR_VOLUME
- %EVENT_PAR
- %NI_USER_ZONE_IDS
- ~NI_DETECT_LOUDNESS_INVALID
- ~NI_DETECT_PEAK_INVALID
- ~NI_DETECT_PITCH_INVALID
- ~NI_DETECT_RMS_INVALID
- !NI_DND_ITEMS_AUDIO
- !NI_DND_ITEMS_MID$Y
- !NI_DND_ITEMS_ARRAY

#### UI

- ui_mouse_area


## Version 0.7.6

### Improved Completion input

* Refactor data management
* Removed unnecessary snipetts

### Supporting to collapse region markers

~~~
{ #region }
your script
:
:
{ #endregion }
~~~

## Version 0.7.5

* KONTAKT 6.1.0 ready
    * Syntax hilighting
        * $CONTROL_PAR_PARENT_PANEL
        * $CONTROL_PAR_WF_VIS_MODE
        * $ENGINE_$PAR_INTMOD_RETRIGGER
        * $ENGINE_PAR_WT_INHARMONIC_MODE
        * $NI_WF_VIS_MODE_1
        * $NI_WF_VIS_MODE_2
        * $NI_WF_VIS_MODE_3
        * $CONTROL_PAR_PARENT_PANEL
        * ui_panel
        * load_performance_view()

## Version 0.7.4

* Editor
    * Improved, Bug fixes ([Detail](https://github.com/r-koubou/vscode-ksp/issues/8))

## Version 0.7.3

* Editor
    * Compiler
        * Bug fixes

## Version 0.7.2

* FIXES package.json
    * vscode module version update 1.1.21 to 1.1.22

## Version 0.7.1

* HOTFIX
    * FIXED package.json according to [Event-Stream Package Security Update](https://code.visualstudio.com/blogs/2018/11/26/event-stream)

## Version 0.7.0

* KONTAKT 6 ready (PREVIEW)
    * Syntax hilighting
    * Syntax check
        * If unknown command detected, report as warning.

### Unknown command informations that added from Kontakt6

* connect_view
* mf_get_byte_one
* mf_get_byte_two
* mf_get_channel
* mf_get_command
* mf_set_channel
* pgs_get_str_key_val


## Version 0.6.1

* Editor
    * Small bug fixes

## Version 0.6.0

* Editor
    * Syntax check and script copy to clipboard has been supported (See also Readme)

## Version 0.5.3

* Editor
    * Outline
        * Migrated from Custom view to VSCode Standard Outline view

## Version 0.5.2

* Editor
    * Outline
        * Replaced refresh icon
        * Imploved CPU load

## Version 0.5.1

* Editor
    * Changed a valid file extension for activate KSP language mode.
        * Before: \*.ksp, \*.txt
        * After: \*.ksp, **\*.ksp.txt**
    * Improved KSP Outline view
        * If language mode is KSP, outline view will be active. Otherwise will be inactive.

## Version 0.5.0

* Editor

    * **[New]** Outline view is avaiable
    * Code refactoring
    * Small bug fixes

## Version 0.4.7

* Editor

    * Bug fixes

## Version 0.4.6

* Code refactoring

## Version 0.4.5

* Editor

    * Code refactoring
    * Obfuscator

        * Supported: Call command from context menu.
        * Obfuscated code write to clipboard is available.

            Default is disabled. See vscode preference **"ksp.obfuscator.dest.clipboard"**. Value is true, it works. Otherwie write to given filepath.


* Syntax parser

    * Code refactoring
    * Allow maximum array size has been changed 1000000 from 32768 that according to KONTAKT 5.8.0's updated.


## Version 0.4.4

* Syntax parser

    * Small bug fixes

## Version 0.4.3

* Syntax parser

    * [Bug fixes](https://github.com/r-koubou/KSPSyntaxParser/issues/8#issuecomment-380670078)

## Version 0.4.2

* Editor

    * Problems View
        * Improved showing when validate a script.
        * Available multiple files showing.

## Version 0.4.1

* Syntax parser

    * [Improved variavle initializer](https://github.com/r-koubou/KSPSyntaxParser/issues/8)

## Version 0.4.0

Minor update.

This version contains important bug fixes.

### New Feature

- **Obfuscate(BETA)**

    **[How to Run]**

    1. Script file open
    2. Set language mode to 'ksp'
    3. Open command palette and type 'ksp'
    4. Select Obfuscator

    - inline function option

        Default is disabled. If you try, turn on vscode preference **"ksp.obfuscator.inline.function"**

### BUG FIXED / Improved

- Syntax parser
    - Bug
        - [Operator "&" problem](https://github.com/r-koubou/KSPSyntaxParser/issues/5)
        - [Variable initializer expression (using with brackets)](https://github.com/r-koubou/KSPSyntaxParser/issues/6)

    - Improved
        - Check array subscribe

- Editor
    - Small bug fixes
    - Snipet behavior improved

### 0.4.0 BETA Release note

- [BETA4](https://github.com/r-koubou/vscode-ksp/releases/tag/Beta-0.4.0_4)
- [BETA3](https://github.com/r-koubou/vscode-ksp/releases/tag/Beta-0.4.0_3)
- [BETA2](https://github.com/r-koubou/vscode-ksp/releases/tag/Beta-0.4.0_2)
- [BETA1](https://github.com/r-koubou/vscode-ksp/releases/tag/Beta-0.4.0_1)


## Version 0.3.15

* Syntax parser

    * Following bug fixes.

        - [BUG - Value assign to string variable](https://github.com/r-koubou/KSPSyntaxParser/issues/7)
        - [BUG - Callback "on __pgs_changed" is not reconized](https://github.com/r-koubou/KSPSyntaxParser/issues/4)
        - [BUG - Array variable declaration](https://github.com/r-koubou/KSPSyntaxParser/issues/1)
        - [BUG - Parser false detection in message() command](https://github.com/r-koubou/KSPSyntaxParser/issues/2)
        - [BUG - Unrecognized some NI built-in variables by parser](https://github.com/r-koubou/KSPSyntaxParser/issues/3)

* Editor

    * Small bug fixes
    * Undocumented NI variables / commands added to snipet etc.

## Version 0.3.14

* Syntax parser

    * Small bug fixes

## Version 0.3.13

* Editor, Syntax parser

    * Small bug fixes

## Version 0.3.12

* Editor

    * Small bug fixes

## Version 0.3.11

* Syntax parser

    * Bug fixes
    * Improving accuracy

## Version 0.3.10

* Syntax parser

    * Bug fixes [https://github.com/r-koubou/KSPSyntaxParser/issues/2](https://github.com/r-koubou/KSPSyntaxParser/issues/2)

## Version 0.3.9

* Syntax parser

    * Bug fixes [https://github.com/r-koubou/KSPSyntaxParser/issues/1](https://github.com/r-koubou/KSPSyntaxParser/issues/1)

    * ADDED: Script line counter.

        If reached to 5,000 lines per callback / function, KONTAKT KSP parser stack memory will be overflow error.

## Version 0.3.8

* Support 5.7 Built-in Variables

    In 5.7, many NI variable has added.

    See also : [http://twitdoc.com/upload/rz_devel/winmerge-file-compare-report.pdf](http://twitdoc.com/upload/rz_devel/winmerge-file-compare-report.pdf)

    \* Usually, my extraction from KSP Reference program. This time i created a diff from 5.6.8. It may be incomplete.

* Syntax Parser
    * Bug fixes
        * Assignment operator "**:=**" Incorrect detection

## Version 0.3.7

* Support 5.6.8 Built-in Variables
    * $NI_CONTROL_PAR_IDX
    * $HIDE_PART_CURSOR

## Version 0.3.6

* Added rename identifiers feature

## Version 0.3.5

* Go to Definition bug fixes

## Version 0.3.4

* Syntax Parser bug fixes

## Version 0.3.3

Syntax highlighting

* Numeric highlighting bug fixes

## Version 0.3.2

Syntax highlighting

* Numeric highlighting bug fixes

## Version 0.3.1

Syntax Parser

* Some bug fixes

## Version 0.3.0

* `Changed Extension ID`

    `Old extension Id no longer updated and will be removed from VScode marketplace on Aug 1st 2017 (JST)`.

    If you have already installed a previous version( `0.2.7 or older` ), please unintall it.

* `UPDATED` Syntax validation ( `"BETA" VERSION` ).
    * Analysis program has some bugs was FIXED
    * Analysis precision improved (`Semantic Analysis` is `READY`)

* Small bug fixes

## Version 0.2.7

Small bug fixes release.

## Version 0.2.6

### Syntax Parser - Bug fixed

Hexadecimal number cannot work

## Version 0.2.5

* Syntax Parser - Bug fixed

## Version 0.2.4

### Improved - Syntax Parser

A Symbol Table implemented

* variable
* callback
* function
* pre-processor ( RESET_CONDITION() )

## Version 0.2.3

### Improved - Syntax Parser

Support Preprocessor statement (Lexical Analysis only)

## Version 0.2.2

### BUG Fixed Syntax Parser program

* For Windows & Locale ja_JP users

    Error messages: Garbled characters

See also: [https://github.com/r-koubou/vscode-ksp/issues/1]()

## Version 0.2.1

### BUG Fixed Syntax Parser program

* Not work when newline is CRLF
* select statement

## Version 0.2.0

1. Modified some snippet.
2. **NEW!: Syntax validation ( ALPHA VERSION! ). Semantic analysis not yet.**

![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/syntaxparser.gif)

### NOTE

* Default is **"disabled"**.

    You can change setting at Preferences -> Settings (Part of "KSP NI KONTAKT Script" ).

* You need to install **Java 1.6 (or higher).**

    **Recomended** 1.8 or higher to work it.

## Version 0.1.0

* NEW Feature

    - Find symbols and find references in script (context menu / Shift+F12)

* Improved

    - Syntax highlighting

## Version 0.0.5

* NEW Feature

    - Go to definition

        Ctrl(Command)+Shift+O or F2 or Ctrl(Command)+Mouse Click
        ![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/goto1.png)

        - Variable

            - If variable type is ui_####, you can select following

                1. Callback definition
                2. Variable definition

                ![](https://github.com/r-koubou/vscode-ksp/raw/master/resources/readme/goto2.png)

        - User function

* Improved

    - Syntax highlighting

## Version 0.0.4

* Improved

    - Syntax highlighting, hover text, autocomplete

        - Some commands (incl non documented)


## Version 0.0.3

* KONTAKT 5.6.5 ready. Following command, variable added

    - New UI control
        - ui_xy
        - $CONTROL_PAR_CURSOR_PICTURE
        - $CONTROL_PAR_MOUSE_MODE
        - $CONTROL_PAR_ACTIVE_INDEX
        - $CONTROL_PAR_MOUSE_BEHAVIOUR_X
        - $CONTROL_PAR_MOUSE_BEHAVIOUR_Y

	- New UI Commands
        - set_control_par_arr()
        - set_control_par_str_arr()

* Improved

    - KSP commands signature behavior  (hover)
    - Syntax highlighting (KSP commands)

## Version 0.0.2

* Autocomplete supported
* Fixed file extension ( Added ".txt")
* Tweek some files.

## Version 0.0.1

### 1st release. ( Based on KONTAKT 5.6 )

Following features supported

* Syntax hilighting
* Snippets
	* Callbacks
	* Events
	* Commands
	* Built-in Variables

</details>
