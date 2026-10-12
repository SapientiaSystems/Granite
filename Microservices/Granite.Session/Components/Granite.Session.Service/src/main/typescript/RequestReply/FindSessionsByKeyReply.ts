import {AbstractServiceReply} from "strata.foundation.core/Transfer";
import {SessionData} from "./SessionData";

export
interface FindSessionsByKeyReply
    extends AbstractServiceReply
{
    sessions?: Array<SessionData>;
}
