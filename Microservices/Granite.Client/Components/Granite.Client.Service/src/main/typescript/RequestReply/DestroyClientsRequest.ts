import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";

export
interface DestroyClientsRequest
    extends AbstractServiceRequest
{
    clientIds?: Array<string>;
}
