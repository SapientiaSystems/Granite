import type { AbstractServiceReply } from "strata.foundation.core/Transfer";
import type { ContentDocumentData } from "./ContentDocumentData";

export
interface FindAllContentDocumentsReply
    extends AbstractServiceReply
{
    documents?: Array<ContentDocumentData>;
}
