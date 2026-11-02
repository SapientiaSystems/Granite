import type { AbstractServiceReply } from "strata.foundation.core/Transfer";
import type { ContentDocumentData } from "./ContentDocumentData";

export
interface UpsertContentDocumentsReply
    extends AbstractServiceReply
{
    requestedKeys?: Array<string>;
    failedKeys?: Array<string>;
    upsertedDocuments?: Array<ContentDocumentData>;
}
