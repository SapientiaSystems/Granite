import type { AbstractServiceReply } from "strata.foundation.core/Transfer";
import type { ContentDocumentData } from "./ContentDocumentData";

export
interface FindContentDocumentsByKeysReply
    extends AbstractServiceReply
{
    documents?: Array<ContentDocumentData>;
}
