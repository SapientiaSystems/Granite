//////////////////////////////////////////////////////////////////////////////
// IContentCommandService.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.service.requestreply;

import java.util.concurrent.CompletionStage;

public
interface IContentCommandService
{
    CompletionStage<UpsertContentDocumentsReply>
    upsertContentDocuments(UpsertContentDocumentsRequest request);

    CompletionStage<DestroyContentDocumentsReply>
    destroyContentDocuments(DestroyContentDocumentsRequest request);
}

//////////////////////////////////////////////////////////////////////////////
