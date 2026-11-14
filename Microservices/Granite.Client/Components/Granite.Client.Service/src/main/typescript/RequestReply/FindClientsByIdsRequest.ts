import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";

export
interface FindClientsByIdsRequest
    extends AbstractServiceRequest
{
    clientIds?: Array<string>;
}
