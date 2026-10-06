import {AbstractServiceReply} from "strata.foundation.core/Transfer";
import {UpdateUserData} from "./UpdateUserData";
import {UserData} from "./UserData";

export
interface UpdateUsersReply
    extends AbstractServiceReply
{
    requestedUsers: Array<UpdateUserData>;
    failedUsers: Array<UpdateUserData>;
    updatedUsers: Array<UserData>;
}

