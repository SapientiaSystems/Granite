//////////////////////////////////////////////////////////////////////////////
// ContentDocumentRepository.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.domain;

import granite.content.service.requestreply.ContentKind;
import strata.server.core.repository.AbstractRepository;
import strata.server.core.unitofwork.IUnitOfWork;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public
class ContentDocumentRepository
    extends AbstractRepository<Long,ContentDocument>
    implements IContentDocumentRepository
{
    public
    ContentDocumentRepository(IUnitOfWork uow)
    {
        super(ContentDocument.class,"primaryId",uow);
        getUnitOfWork()
            .registerQuery(
                "findAllContentDocumentsByKeyIn",
                "select e from ContentDocument e where e.documentKey IN :keys")
            .registerQuery(
                "findAllContentDocuments",
                "select e from ContentDocument e order by e.title")
            .registerQuery(
                "findAllContentDocumentsByKindIn",
                "select e from ContentDocument e where e.kind IN :kinds order by e.title")
            .registerQuery(
                "findAllContentDocumentsBySource",
                "select e from ContentDocument e where e.source = :source");
    }

    @Override
    public Map<String,Optional<ContentDocument>>
    findAllByKeyIn(Set<String> keys)
    {
        Map<String,Optional<ContentDocument>> result = new LinkedHashMap<>();

        for (String key : keys)
            result.put(key,Optional.empty());

        if (keys.isEmpty())
            return result;

        for (ContentDocument document : getUnitOfWork().findManyByQuery(ContentDocument.class,"findAllContentDocumentsByKeyIn",Map.of("keys",keys)))
            result.put(document.getDocumentKey(),Optional.of(document));

        return result;
    }

    @Override
    public List<ContentDocument>
    findAllDocuments()
    {
        return new ArrayList<>(getUnitOfWork().findManyByQuery(ContentDocument.class,"findAllContentDocuments",Map.of()));
    }

    @Override
    public List<ContentDocument>
    findAllByKindIn(Set<ContentKind> kinds)
    {
        return new ArrayList<>(getUnitOfWork().findManyByQuery(ContentDocument.class,"findAllContentDocumentsByKindIn",Map.of("kinds",kinds)));
    }

    @Override
    public List<ContentDocument>
    findAllBySource(String source)
    {
        return new ArrayList<>(getUnitOfWork().findManyByQuery(ContentDocument.class,"findAllContentDocumentsBySource",Map.of("source",source)));
    }
}

//////////////////////////////////////////////////////////////////////////////
