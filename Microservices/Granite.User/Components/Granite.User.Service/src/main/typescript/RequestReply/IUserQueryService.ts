import {ICompletionStage} from "strata.foundation.core/Concurrent";
import {FindUsersByKeyRequest} from "./FindUsersByKeyRequest";
import {FindUsersByKeyReply} from "./FindUsersByKeyReply";
import {FindUsersByKeySetRequest} from "./FindUsersByKeySetRequest";
import {FindUsersByKeySetReply} from "./FindUsersByKeySetReply";
import {FindUsersByExternalIdentityRequest} from "./FindUsersByExternalIdentityRequest";
import {FindUsersByExternalIdentityReply} from "./FindUsersByExternalIdentityReply";

export
interface IUserQueryService
{
    findUsersByKey(request: FindUsersByKeyRequest):
        ICompletionStage<FindUsersByKeyReply>;

    findUsersByKeySet(request: FindUsersByKeySetRequest):
        ICompletionStage<FindUsersByKeySetReply>;

    findUsersByExternalIdentity(request: FindUsersByExternalIdentityRequest):
        ICompletionStage<FindUsersByExternalIdentityReply>;
}

