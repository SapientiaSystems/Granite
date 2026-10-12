import {ICompletionStage} from "strata.foundation.core/Concurrent";
import {FindSessionsByKeyRequest} from "./FindSessionsByKeyRequest";
import {FindSessionsByKeyReply} from "./FindSessionsByKeyReply";
import {FindSessionsByKeySetRequest} from "./FindSessionsByKeySetRequest";
import {FindSessionsByKeySetReply} from "./FindSessionsByKeySetReply";
import {FindSessionByUserIdAndDeviceIdRequest} from "./FindSessionByUserIdAndDeviceIdRequest";
import {FindSessionByUserIdAndDeviceIdReply} from "./FindSessionByUserIdAndDeviceIdReply";

export
interface ISessionQueryService
{
    findSessionsByKey(request: FindSessionsByKeyRequest):
        ICompletionStage<FindSessionsByKeyReply>;

    findSessionsByKeySet(request: FindSessionsByKeySetRequest):
        ICompletionStage<FindSessionsByKeySetReply>;

    findSessionByUserIdAndDeviceId(request: FindSessionByUserIdAndDeviceIdRequest):
        ICompletionStage<FindSessionByUserIdAndDeviceIdReply>;
}
