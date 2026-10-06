import {AbstractServiceReply} from "strata.foundation.core/Transfer";
import {UserKey} from "./UserKey";
import {UserData} from "./UserData";

export
interface FindUsersByKeyReply
    extends AbstractServiceReply
{
    key: UserKey;
    foundUsers: Array<UserData>;
    exists: boolean;
    returnIfFound: boolean;
}

