import {AbstractServiceRequest} from "strata.foundation.core/Transfer/";

export
interface DestroyUserRequest
    extends AbstractServiceRequest
{
    userId: number;
}