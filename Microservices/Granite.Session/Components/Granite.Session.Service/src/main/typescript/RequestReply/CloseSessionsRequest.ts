import {AbstractServiceRequest} from "strata.foundation.core/Transfer";

export
interface CloseSessionsRequest
    extends AbstractServiceRequest
{
    sessionIds?: Array<number>;
}
