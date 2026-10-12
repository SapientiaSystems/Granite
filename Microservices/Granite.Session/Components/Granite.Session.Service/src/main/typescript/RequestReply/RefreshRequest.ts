import {AbstractServiceRequest} from "strata.foundation.core/Transfer";

export
interface RefreshRequest
    extends AbstractServiceRequest
{
    sessionId?: number;
}
