import {AbstractServiceReply} from "strata.foundation.core/Transfer";
import {SessionData} from "./SessionData";

export
interface FindSessionByUserIdAndDeviceIdReply
    extends AbstractServiceReply
{
    session?: SessionData;
}
