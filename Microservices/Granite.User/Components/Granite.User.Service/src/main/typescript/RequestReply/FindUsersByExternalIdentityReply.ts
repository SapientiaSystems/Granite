import {AbstractServiceReply} from "strata.foundation.core/Transfer";
import {UserData} from "./UserData";

export
interface FindUsersByExternalIdentityReply
    extends AbstractServiceReply
{
    users: Array<UserData>;
}
