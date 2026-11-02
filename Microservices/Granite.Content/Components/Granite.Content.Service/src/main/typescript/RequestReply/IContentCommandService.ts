import type { ICompletionStage } from "strata.foundation.core/Concurrent";
import type { UpsertContentDocumentsRequest } from "./UpsertContentDocumentsRequest";
import type { UpsertContentDocumentsReply } from "./UpsertContentDocumentsReply";
import type { DestroyContentDocumentsRequest } from "./DestroyContentDocumentsRequest";
import type { DestroyContentDocumentsReply } from "./DestroyContentDocumentsReply";

export
interface IContentCommandService
{
    upsertContentDocuments(request: UpsertContentDocumentsRequest): ICompletionStage<UpsertContentDocumentsReply>;

    destroyContentDocuments(request: DestroyContentDocumentsRequest): ICompletionStage<DestroyContentDocumentsReply>;
}
