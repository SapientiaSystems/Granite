import {AbstractServiceRequest} from "strata.foundation.core/Transfer/";

export
interface DestroyUsersRequest
    extends AbstractServiceRequest
{
    userIds: Array<number>;
}

