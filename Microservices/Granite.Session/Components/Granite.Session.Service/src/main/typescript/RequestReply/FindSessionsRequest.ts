import {AbstractServiceRequest} from "strata.foundation.core/Transfer";

export
interface FindSessionsRequest
    extends AbstractServiceRequest
{
    includeClosed?: boolean;
}
