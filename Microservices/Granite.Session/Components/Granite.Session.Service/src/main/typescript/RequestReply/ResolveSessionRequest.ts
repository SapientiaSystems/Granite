import {AbstractServiceRequest} from "strata.foundation.core/Transfer";

export
interface ResolveSessionRequest
    extends AbstractServiceRequest
{
    handle?: string;
}
