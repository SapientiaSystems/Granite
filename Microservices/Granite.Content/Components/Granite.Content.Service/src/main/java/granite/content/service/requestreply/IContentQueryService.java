//////////////////////////////////////////////////////////////////////////////
// IContentQueryService.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.service.requestreply;

import java.util.concurrent.CompletionStage;

public
interface IContentQueryService
{
    CompletionStage<SearchContentReply>
    searchContent(SearchContentRequest request);

    CompletionStage<FindContentDocumentsByKeysReply>
    findContentDocumentsByKeys(FindContentDocumentsByKeysRequest request);

    CompletionStage<FindAllContentDocumentsReply>
    findAllContentDocuments(FindAllContentDocumentsRequest request);
}

//////////////////////////////////////////////////////////////////////////////
