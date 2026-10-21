import {AbstractServiceReply} from "strata.foundation.core/Transfer";
import {UserData} from "./UserData";

export
interface FindAllUsersReply
    extends AbstractServiceReply
{
    users: Array<UserData>;
}
