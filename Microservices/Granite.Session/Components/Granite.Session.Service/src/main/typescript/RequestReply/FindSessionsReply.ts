import {AbstractServiceReply} from "strata.foundation.core/Transfer";
import {SessionData} from "./SessionData";

export
interface FindSessionsReply
    extends AbstractServiceReply
{
    sessions?: Array<SessionData>;
}
