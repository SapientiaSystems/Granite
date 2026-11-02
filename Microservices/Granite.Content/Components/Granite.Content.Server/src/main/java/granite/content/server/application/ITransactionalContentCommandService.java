//////////////////////////////////////////////////////////////////////////////
// ITransactionalContentCommandService.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.application;

import granite.content.service.requestreply.IContentCommandService;
import granite.content.service.requestreply.UpsertContentDocumentData;
import granite.content.service.requestreply.UpsertContentDocumentsReply;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletionStage;

@Transactional
public
interface ITransactionalContentCommandService
    extends IContentCommandService
{
    CompletionStage<UpsertContentDocumentsReply>
    synchronizeSource(String source,List<UpsertContentDocumentData> documents);
}

//////////////////////////////////////////////////////////////////////////////
