import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";
import type { UpsertContentDocumentData } from "./UpsertContentDocumentData";

export
interface UpsertContentDocumentsRequest
    extends AbstractServiceRequest
{
    documents?: Array<UpsertContentDocumentData>;
}
