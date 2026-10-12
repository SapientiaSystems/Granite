import {AbstractServiceReply} from "strata.foundation.core/Transfer";
import {SessionData} from "./SessionData";

export
interface LoginReply
    extends AbstractServiceReply
{
    sessionData?: SessionData;
    accessToken?: string;
}
