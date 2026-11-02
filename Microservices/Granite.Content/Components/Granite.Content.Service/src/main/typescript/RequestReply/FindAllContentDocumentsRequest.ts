import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";
import type { ContentKind } from "./ContentKind";

export
interface FindAllContentDocumentsRequest
    extends AbstractServiceRequest
{
    kinds?: Array<ContentKind>;
    authorities?: Array<string>;
}
