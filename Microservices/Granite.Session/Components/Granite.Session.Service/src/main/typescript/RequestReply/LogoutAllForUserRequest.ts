import {AbstractServiceRequest} from "strata.foundation.core/Transfer";

export
interface LogoutAllForUserRequest
    extends AbstractServiceRequest
{
    userId?: number;
}
