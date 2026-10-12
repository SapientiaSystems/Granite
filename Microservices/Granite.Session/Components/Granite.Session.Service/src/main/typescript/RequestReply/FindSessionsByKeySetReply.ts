import {AbstractServiceReply} from "strata.foundation.core/Transfer";
import {SessionData} from "./SessionData";

export
interface FindSessionsByKeySetReply
    extends AbstractServiceReply
{
    sessions?: Array<SessionData>;
}
