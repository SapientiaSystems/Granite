import {ICompletionStage} from "strata.foundation.core/Concurrent";
import {CreateUserRequest} from "./CreateUserRequest";
import {CreateUserReply} from "./CreateUserReply";
import {CreateUsersRequest} from "./CreateUsersRequest";
import {CreateUsersReply} from "./CreateUsersReply";
import {UpdateUserRequest} from "./UpdateUserRequest";
import {UpdateUserReply} from "./UpdateUserReply";
import {MergeUserValuesRequest} from "./MergeUserValuesRequest";
import {MergeUserValuesReply} from "./MergeUserValuesReply";
import {DestroyUserRequest} from "./DestroyUserRequest";
import {DestroyUserReply} from "./DestroyUserReply";
import {DestroyUsersRequest} from "./DestroyUsersRequest";
import {DestroyUsersReply} from "./DestroyUsersReply";
import {AuthenticateUserRequest} from "./AuthenticateUserRequest";
import {AuthenticateUserReply} from "./AuthenticateUserReply";

export
interface IUserCommandService
{
    createUser(request: CreateUserRequest):
        ICompletionStage<CreateUserReply>;

    createUsers(request: CreateUsersRequest):
        ICompletionStage<CreateUsersReply>;

    updateUser(request: UpdateUserRequest):
        ICompletionStage<UpdateUserReply>;

    mergeUserValues(request: MergeUserValuesRequest):
        ICompletionStage<MergeUserValuesReply>;

    destroyUser(request: DestroyUserRequest):
        ICompletionStage<DestroyUserReply>;

    destroyUsers(request: DestroyUsersRequest):
        ICompletionStage<DestroyUsersReply>;

    authenticateUser(request: AuthenticateUserRequest):
        ICompletionStage<AuthenticateUserReply>;
}

