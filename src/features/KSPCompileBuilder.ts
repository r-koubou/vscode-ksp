/* =========================================================================

    KSPCompileBuilder.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import * as vscode from 'vscode';

import * as configkey from './KSPConfigurationConstants';
import { KSPConfigurationManager } from './KSPConfigurationManager';

/**
 * Build a internal compiler's commandline option.
 * Initial value is set by user configuration.
 */
export class KSPCompileBuilder
{
    // Compile options
    public inputFile: string;
    public parseOnly: boolean           = false;
    public strict: boolean              = false;
    public unused: boolean              = false;
    // Obfuscate options
    public obfuscate: boolean           = false;
    public obfuscateInline: boolean     = false;
    public obfuscatedOutputFile: string;
    // JVM args
    public jvmArgs: string[]            = [];
    public forceUseEn_US                = false;

    /** for resolving compiler's path */
    private extentionDir: string;

    /**
     * Commandline options initialized by configuration
     */
    constructor( extention: vscode.Extension<any>, inputFile:string, jvmArgs:string[] = [], obfuscate:boolean = false, obfuscateInline:boolean = false, obfuscatedOutputFile:string = undefined )
    {
        this.extentionDir       = extention.extensionPath;
        // Compile options
        this.inputFile          = inputFile;
        this.parseOnly          = KSPConfigurationManager.getConfig<boolean>( configkey.KEY_PARSE_SYNTAX_ONLY, configkey.DEFAULT_PARSE_SYNTAX_ONLY );
        this.strict             = KSPConfigurationManager.getConfig<boolean>( configkey.KEY_PARSE_STRICT, configkey.DEFAULT_PARSE_STRICT );
        this.unused             = KSPConfigurationManager.getConfig<boolean>( configkey.KEY_PARSE_UNUSED, configkey.DEFAULT_PARSE_UNUSED );
        // Obfuscate options
        this.obfuscate            = obfuscate;
        this.obfuscateInline      = obfuscateInline;
        this.obfuscatedOutputFile = obfuscatedOutputFile;
        // JVM args
        if( jvmArgs )
        {
            for( let a of jvmArgs )
            {
                this.jvmArgs.push( a );
            }
        }
    }

    /**
     * Build a commandline option string
     */
    public build(): string[]
    {
        let args: string[] = [].concat( this.jvmArgs );
        if( this.forceUseEn_US )
        {
            args.push( "-Duser.language=en" );
            args.push( "-Duser.country=US" );
        }
        args.push( "-Dkspparser.stdout.encoding=UTF-8" )
        args.push( "-Dkspparser.datadir=" + this.extentionDir + "/kspparser/data" )
        args.push( "-jar" );
        args.push( this.extentionDir + "/kspparser/KSPSyntaxParser.jar" );

        if( this.obfuscate )
        {
            args.push( "--strict" );
            args.push( "--obfuscate" );
            if( this.obfuscateInline )
            {
                args.push( "--inline-userfunction" )
            }
            args.push( "--output" );
            args.push( this.obfuscatedOutputFile );
        }
        else
        {
            if( this.parseOnly )
            {
                args.push( "--parseonly" );
            }
            if( this.strict )
            {
                args.push( "--strict" );
            }
            if( this.unused )
            {
                args.push( "--unused" );
            }
        }

        args.push( this.inputFile );
        return args;
    }
}
