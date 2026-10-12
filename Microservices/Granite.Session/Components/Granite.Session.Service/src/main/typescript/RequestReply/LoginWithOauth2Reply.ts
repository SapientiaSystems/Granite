import {AbstractServiceReply} from "strata.foundation.core/Transfer";
import {SessionData} from "./SessionData";

export
interface LoginWithOauth2Reply
    extends AbstractServiceReply
{
    sessionData?: SessionData;
    accessToken?: string;
}
