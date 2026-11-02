import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";

export
interface DestroyContentDocumentsRequest
    extends AbstractServiceRequest
{
    keys?: Array<string>;
}
