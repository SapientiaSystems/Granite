//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { FrontEndOutput }            from "strata.frontendcontroller.service/RequestReply";
import type { PreferenceDefinitionData }  from "./PreferenceDefinitionData";

export
interface PreferencesOutput
    extends FrontEndOutput
{
    values:          Record<string,unknown>;
    definitions:     Array<PreferenceDefinitionData>;
    failureMessages: Array<string>;
}

//////////////////////////////////////////////////////////////////////////////
