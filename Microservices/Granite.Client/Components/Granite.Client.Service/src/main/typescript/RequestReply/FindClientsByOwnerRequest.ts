import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";

export
interface FindClientsByOwnerRequest
    extends AbstractServiceRequest
{
    userId?: number;
}
