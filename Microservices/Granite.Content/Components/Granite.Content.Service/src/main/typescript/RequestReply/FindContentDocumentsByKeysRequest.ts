import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";

export
interface FindContentDocumentsByKeysRequest
    extends AbstractServiceRequest
{
    keys?: Array<string>;
    authorities?: Array<string>;
}
