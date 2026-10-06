import {AbstractServiceRequest} from "strata.foundation.core/Transfer/";
import {UserKey} from "./UserKey";

export
interface FindUsersByKeyRequest
    extends AbstractServiceRequest
{
    key: UserKey;
    returnIfFound: boolean;
}

