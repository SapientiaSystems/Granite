import {AbstractServiceReply} from "strata.foundation.core/Transfer";
import {UserKeySet} from "./UserKeySet";
import {UserData} from "./UserData";

export
interface FindUsersByKeySetReply
    extends AbstractServiceReply
{
    requestedKeys: UserKeySet;
    missingKeys: UserKeySet;
    foundKeys: UserKeySet;
    foundUsers: Array<UserData>;
    exists: boolean;
    returnIfFound: boolean;
}

