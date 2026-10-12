import {AbstractServiceRequest} from "strata.foundation.core/Transfer";

export
interface LogoutRequest
    extends AbstractServiceRequest
{
    sessionId?: number;
}
