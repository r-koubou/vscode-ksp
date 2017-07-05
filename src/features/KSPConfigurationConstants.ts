/* =========================================================================

    KSPConfigConstants.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

export const CONFIG_SECTION_NAME            = 'ksp';

// Validate
export const KEY_ENABLE_VALIDATE            = 'validate.enable';
export const KEY_ENABLE_REALTIME_VALIDATE   = 'validate.realtime.enable';
export const KEY_REALTIME_VALIDATE_DELAY    = 'validate.realtime.delay';
export const KEY_PARSE_SYNTAX_ONLY          = 'validate.syntax.only';
export const KEY_PARSE_STRICT               = 'validate.strict';
export const KEY_PARSE_UNUSED               = 'validate.unused';

export const KEY_JAVA_LOCATION              = 'java.location';

export const DEFAULT_JAVA_LOCATION          = 'java';
export const DEFAULT_ENABLE_VALIDATE        = false;
export const DEFAULT_REALTIME_VALIDATE      = false;
export const DEFAULT_VALIDATE_DELAY         = 500;
export const DEFAULT_PARSE_SYNTAX_ONLY      = false;
export const DEFAULT_PARSE_STRICT           = false;
export const DEFAULT_PARSE_UNUSED           = false;
