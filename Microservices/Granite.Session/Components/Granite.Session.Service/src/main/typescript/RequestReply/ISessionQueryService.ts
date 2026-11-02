import {ICompletionStage} from "strata.foundation.core/Concurrent";
import {FindSessionsByKeyRequest} from "./FindSessionsByKeyRequest";
import {FindSessionsByKeyReply} from "./FindSessionsByKeyReply";
import {FindSessionsByKeySetRequest} from "./FindSessionsByKeySetRequest";
import {FindSessionsByKeySetReply} from "./FindSessionsByKeySetReply";
import {FindSessionByUserIdAndDeviceIdRequest} from "./FindSessionByUserIdAndDeviceIdRequest";
import {FindSessionByUserIdAndDeviceIdReply} from "./FindSessionByUserIdAndDeviceIdReply";
import {FindSessionsRequest} from "./FindSessionsRequest";
import {FindSessionsReply} from "./FindSessionsReply";

export
interface ISessionQueryService
{
    findSessionsByKey(request: FindSessionsByKeyRequest):
        ICompletionStage<FindSessionsByKeyReply>;

    findSessionsByKeySet(request: FindSessionsByKeySetRequest):
        ICompletionStage<FindSessionsByKeySetReply>;

    findSessionByUserIdAndDeviceId(request: FindSessionByUserIdAndDeviceIdRequest):
        ICompletionStage<FindSessionByUserIdAndDeviceIdReply>;

    findSessions(request: FindSessionsRequest):
        ICompletionStage<FindSessionsReply>;
}
