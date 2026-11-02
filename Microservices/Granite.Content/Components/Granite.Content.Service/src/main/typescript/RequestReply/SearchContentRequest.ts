import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";
import type { ContentKind } from "./ContentKind";

export
interface SearchContentRequest
    extends AbstractServiceRequest
{
    query?: string;
    kinds?: Array<ContentKind>;
    authorities?: Array<string>;
    limit?: number;
}
