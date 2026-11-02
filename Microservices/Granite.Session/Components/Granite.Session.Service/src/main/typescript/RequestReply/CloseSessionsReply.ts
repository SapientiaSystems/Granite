import {AbstractServiceReply} from "strata.foundation.core/Transfer";
import {SessionData} from "./SessionData";

export
interface CloseSessionsReply
    extends AbstractServiceReply
{
    requestedSessionIds?: Array<number>;
    failedSessionIds?: Array<number>;
    closedSessions?: Array<SessionData>;
}
