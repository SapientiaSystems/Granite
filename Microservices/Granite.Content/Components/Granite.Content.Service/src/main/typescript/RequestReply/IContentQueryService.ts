import type { ICompletionStage } from "strata.foundation.core/Concurrent";
import type { SearchContentRequest } from "./SearchContentRequest";
import type { SearchContentReply } from "./SearchContentReply";
import type { FindContentDocumentsByKeysRequest } from "./FindContentDocumentsByKeysRequest";
import type { FindContentDocumentsByKeysReply } from "./FindContentDocumentsByKeysReply";
import type { FindAllContentDocumentsRequest } from "./FindAllContentDocumentsRequest";
import type { FindAllContentDocumentsReply } from "./FindAllContentDocumentsReply";

export
interface IContentQueryService
{
    searchContent(request: SearchContentRequest): ICompletionStage<SearchContentReply>;

    findContentDocumentsByKeys(request: FindContentDocumentsByKeysRequest): ICompletionStage<FindContentDocumentsByKeysReply>;

    findAllContentDocuments(request: FindAllContentDocumentsRequest): ICompletionStage<FindAllContentDocumentsReply>;
}
