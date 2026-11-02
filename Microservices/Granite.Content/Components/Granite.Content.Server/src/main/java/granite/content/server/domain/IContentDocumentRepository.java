//////////////////////////////////////////////////////////////////////////////
// IContentDocumentRepository.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.domain;

import granite.content.service.requestreply.ContentKind;
import strata.server.core.repository.IRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public
interface IContentDocumentRepository
    extends IRepository<Long,ContentDocument>
{
    Map<String,Optional<ContentDocument>>
    findAllByKeyIn(Set<String> keys);

    List<ContentDocument>
    findAllDocuments();

    List<ContentDocument>
    findAllByKindIn(Set<ContentKind> kinds);

    List<ContentDocument>
    findAllBySource(String source);
}

//////////////////////////////////////////////////////////////////////////////
