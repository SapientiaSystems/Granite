import {AbstractServiceReply} from "strata.foundation.core/Transfer";
import {SessionData} from "./SessionData";

export
interface CompleteOktaLoginReply
    extends AbstractServiceReply
{
    sessionData?: SessionData;
    sessionHandle?: string;
    userName?: string;
    returnTo?: string;
}
