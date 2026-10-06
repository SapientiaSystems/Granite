import {AbstractServiceReply} from "strata.foundation.core/Transfer";
import {CreateUserData} from "./CreateUserData";
import {UserData} from "./UserData";

export
interface CreateUsersReply
    extends AbstractServiceReply
{
    requestedUsers: Array<CreateUserData>;
    failedUsers: Array<CreateUserData>;
    createdUsers: Array<UserData>;
}

