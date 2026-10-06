import {AbstractServiceRequest} from "strata.foundation.core/Transfer/";
import {UserKeySet} from "./UserKeySet";

export
interface FindUsersByKeySetRequest
    extends AbstractServiceRequest
{
    keys: UserKeySet;
    returnIfFound: boolean;
}

