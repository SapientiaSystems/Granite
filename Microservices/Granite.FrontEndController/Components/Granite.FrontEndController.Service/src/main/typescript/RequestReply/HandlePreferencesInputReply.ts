//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { HandleInputReply } from "strata.frontendcontroller.service/RequestReply";
import type { PreferencesInput } from "./PreferencesInput";
import type { PreferencesOutput } from "./PreferencesOutput";

export
interface HandlePreferencesInputReply
    extends HandleInputReply<PreferencesInput,PreferencesOutput>
{
}

//////////////////////////////////////////////////////////////////////////////
