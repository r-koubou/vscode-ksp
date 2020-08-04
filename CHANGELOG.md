# Changelog

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
