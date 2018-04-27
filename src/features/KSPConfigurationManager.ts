/* =========================================================================

    KSPConfigurationManager.ts
    Copyright(c) R-Koubou

    [License]
    MIT

   ======================================================================== */

import * as vscode          from 'vscode';
import * as config          from './KSPConfigurationConstants';

export class KSPConfigurationManager
{
    /**
     * ctor
     */
    private constructor(){}

    /**
     * Get a user configuration value
     */
    static getConfig<T>( key:string, defaultValue:T ): T
    {
        let ret:T = defaultValue;
        KSPConfigurationManager.getConfigComplex( key, defaultValue, (v,u)=>{
            ret = v;
        });
        return ret;
    }

    /**
     * Get a user configuration value
     */
    static getConfigComplex<T>( key:string, defaultValue:T, callback: ( value:T, userDefined:boolean)=>void ): void
    {
        let section: vscode.WorkspaceConfiguration = vscode.workspace.getConfiguration( config.CONFIG_SECTION_NAME );
        let value:T = defaultValue;
        let userDefined: boolean = false;
        let inspect = section.inspect<T>( key );

        if( ! section )
        {
            callback( defaultValue, userDefined );
            return;
        }

        if( inspect )
        {
            if( inspect.workspaceValue )
            {
                value       = inspect.workspaceValue;
                userDefined = true;
            }
            else if( inspect.globalValue )
            {
                value = inspect.globalValue;
            }
        }
        callback( value, userDefined );
    }
}