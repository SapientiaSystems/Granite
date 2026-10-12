import {AbstractServiceReply} from "strata.foundation.core/Transfer";
import {SessionData} from "./SessionData";

export
interface RefreshReply
    extends AbstractServiceReply
{
    sessionData?: SessionData;
    accessToken?: string;
}
