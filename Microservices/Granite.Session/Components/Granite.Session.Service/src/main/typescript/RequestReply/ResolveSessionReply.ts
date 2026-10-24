import {AbstractServiceReply} from "strata.foundation.core/Transfer";
import {SessionData} from "./SessionData";

export
interface ResolveSessionReply
    extends AbstractServiceReply
{
    session?: SessionData;
    accessToken?: string;
}
