import {ICompletionStage} from "strata.foundation.core/Concurrent";
import {FindUsersByKeyRequest} from "./FindUsersByKeyRequest";
import {FindUsersByKeyReply} from "./FindUsersByKeyReply";
import {FindUsersByKeySetRequest} from "./FindUsersByKeySetRequest";
import {FindUsersByKeySetReply} from "./FindUsersByKeySetReply";

export
interface IUserQueryService
{
    findUsersByKey(request: FindUsersByKeyRequest):
        ICompletionStage<FindUsersByKeyReply>;

    findUsersByKeySet(request: FindUsersByKeySetRequest):
        ICompletionStage<FindUsersByKeySetReply>;
}

