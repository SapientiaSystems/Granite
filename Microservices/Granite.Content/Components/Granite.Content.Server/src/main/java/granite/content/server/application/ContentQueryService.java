//////////////////////////////////////////////////////////////////////////////
// ContentQueryService.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.application;

import granite.content.server.domain.ContentDocument;
import granite.content.server.domain.IContentDocumentRepository;
import granite.content.service.requestreply.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import strata.foundation.core.concurrent.CurrentThreadExecutor;
import strata.foundation.core.transfer.ExceptionData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public
class ContentQueryService
    implements ITransactionalContentQueryService
{
    public static final int DEFAULT_LIMIT    = 10;
    public static final int MAX_LIMIT        = 50;
    public static final int MAX_QUERY_LENGTH = 500;

    private final IContentDocumentRepository repository;
    private final IContentSearchEngine       searchEngine;
    private final ModelMapper                mapper;
    private final Logger                     logger;

    public
    ContentQueryService(IContentDocumentRepository r,IContentSearchEngine e,ModelMapper m)
    {
        repository   = r;
        searchEngine = e;
        mapper       = m;
        logger       = LogManager.getLogger(ContentQueryService.class);
    }

    @Override
    public CompletionStage<SearchContentReply>
    searchContent(SearchContentRequest request)
    {
        logger.debug("searchContent({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(
                    valid ->
                        searchEngine.search(
                            visible(candidates(valid.getKinds()),valid.getAuthorities()),
                            valid.getQuery(),
                            limit(valid.getLimit())))
                .thenApply(
                    hits ->
                        new SearchContentReply(request)
                            .setSuccess(true)
                            .setSuccessMessage("Found " + hits.size() + " hit(s)")
                            .setHits(hits))
                .exceptionally(
                    e ->
                        new SearchContentReply(request)
                            .setSuccess(false)
                            .setFailureMessage("Unable to search content: " + e.getMessage())
                            .setException(ExceptionData.of(e)));
    }

    @Override
    public CompletionStage<FindContentDocumentsByKeysReply>
    findContentDocumentsByKeys(FindContentDocumentsByKeysRequest request)
    {
        logger.debug("findContentDocumentsByKeys({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(
                    valid ->
                        visible(
                            repository
                                .findAllByKeyIn(valid.getKeys())
                                .values()
                                .stream()
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList()),
                            valid.getAuthorities()))
                .thenApply(
                    found ->
                        new FindContentDocumentsByKeysReply(request)
                            .setSuccess(true)
                            .setSuccessMessage("Found " + found.size() + " content document(s)")
                            .setDocuments(toData(found)))
                .exceptionally(
                    e ->
                        new FindContentDocumentsByKeysReply(request)
                            .setSuccess(false)
                            .setFailureMessage("Unable to find content documents: " + e.getMessage())
                            .setException(ExceptionData.of(e)));
    }

    @Override
    public CompletionStage<FindAllContentDocumentsReply>
    findAllContentDocuments(FindAllContentDocumentsRequest request)
    {
        logger.debug("findAllContentDocuments({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> Objects.requireNonNull(request,"request"),new CurrentThreadExecutor())
                .thenApply(valid -> visible(candidates(valid.getKinds()),valid.getAuthorities()))
                .thenApply(
                    found ->
                        new FindAllContentDocumentsReply(request)
                            .setSuccess(true)
                            .setSuccessMessage("Found " + found.size() + " content document(s)")
                            .setDocuments(toData(found)))
                .exceptionally(
                    e ->
                        new FindAllContentDocumentsReply(request)
                            .setSuccess(false)
                            .setFailureMessage("Unable to find content documents: " + e.getMessage())
                            .setException(ExceptionData.of(e)));
    }

    protected SearchContentRequest
    validate(SearchContentRequest request)
    {
        Objects.requireNonNull(request,"request");

        if (request.getQuery() == null || request.getQuery().isBlank())
            throw new IllegalArgumentException("validation failed: query was blank");

        if (request.getQuery().length() > MAX_QUERY_LENGTH)
            throw new IllegalArgumentException("validation failed: query is longer than " + MAX_QUERY_LENGTH + " characters");

        return request;
    }

    protected FindContentDocumentsByKeysRequest
    validate(FindContentDocumentsByKeysRequest request)
    {
        Objects.requireNonNull(request,"request");
        Objects.requireNonNull(request.getKeys(),"validation failed: keys were not provided");

        if (request.getKeys().isEmpty())
            throw new IllegalArgumentException("validation failed: keys were empty");

        return request;
    }

    private List<ContentDocument>
    candidates(Set<ContentKind> kinds)
    {
        return
            kinds == null || kinds.isEmpty()
                ? repository.findAllDocuments()
                : repository.findAllByKindIn(kinds);
    }

    private List<ContentDocument>
    visible(List<ContentDocument> documents,Set<String> authorities)
    {
        List<ContentDocument> result = new ArrayList<>();

        for (ContentDocument document : documents)
            if (document.isVisibleTo(authorities))
                result.add(document);

        return result;
    }

    private int
    limit(Integer requested)
    {
        if (requested == null || requested <= 0)
            return DEFAULT_LIMIT;

        return Math.min(requested,MAX_LIMIT);
    }

    private List<ContentDocumentData>
    toData(List<ContentDocument> found)
    {
        return found.stream().map(d -> mapper.map(d,ContentDocumentData.class)).collect(Collectors.toList());
    }
}

//////////////////////////////////////////////////////////////////////////////
