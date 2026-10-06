import {AbstractServiceReply} from "strata.foundation.core/Transfer";
import {UserData} from "./UserData";

export
interface DestroyUsersReply
    extends AbstractServiceReply
{
    requestedUserIds: Array<number>;
    failedUserIds: Array<number>;
    destroyedUsers: Array<UserData>;
}

