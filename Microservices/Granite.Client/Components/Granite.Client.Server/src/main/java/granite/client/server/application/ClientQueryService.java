//////////////////////////////////////////////////////////////////////////////
// ClientQueryService.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.application;

import granite.client.server.domain.Client;
import granite.client.server.domain.IClientRepository;
import granite.client.service.requestreply.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import strata.foundation.core.concurrent.CurrentThreadExecutor;
import strata.foundation.core.transfer.ExceptionData;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public
class ClientQueryService
    implements ITransactionalClientQueryService
{
    private final IClientRepository repository;
    private final ModelMapper       mapper;
    private final Logger            logger;

    public
    ClientQueryService(IClientRepository r,ModelMapper m)
    {
        repository = r;
        mapper     = m;
        logger     = LogManager.getLogger(ClientQueryService.class);
    }

    @Override
    public CompletionStage<FindClientsReply>
    findAllClients(FindAllClientsRequest request)
    {
        logger.debug("findAllClients({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> Objects.requireNonNull(request,"request"),new CurrentThreadExecutor())
                .thenApply(valid -> repository.findAllClients())
                .thenApply(found -> success(new FindClientsReply(request),found))
                .exceptionally(e -> failure(new FindClientsReply(request),e));
    }

    @Override
    public CompletionStage<FindClientsReply>
    findClientsByIds(FindClientsByIdsRequest request)
    {
        logger.debug("findClientsByIds({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(valid -> repository.findAllByClientIdIn(valid.getClientIds()))
                .thenApply(found -> success(new FindClientsReply(request),found))
                .exceptionally(e -> failure(new FindClientsReply(request),e));
    }

    @Override
    public CompletionStage<FindClientsReply>
    findClientsByOwner(FindClientsByOwnerRequest request)
    {
        logger.debug("findClientsByOwner({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(valid -> repository.findAllByOwner(valid.getUserId()))
                .thenApply(found -> success(new FindClientsReply(request),found))
                .exceptionally(e -> failure(new FindClientsReply(request),e));
    }

    protected FindClientsByIdsRequest
    validate(FindClientsByIdsRequest request)
    {
        Objects.requireNonNull(request,"request");
        Objects.requireNonNull(request.getClientIds(),"validation failed: clientIds were not provided");

        if (request.getClientIds().isEmpty())
            throw new IllegalArgumentException("validation failed: clientIds were empty");

        return request;
    }

    protected FindClientsByOwnerRequest
    validate(FindClientsByOwnerRequest request)
    {
        Objects.requireNonNull(request,"request");
        Objects.requireNonNull(request.getUserId(),"validation failed: userId was not provided");
        return request;
    }

    private FindClientsReply
    success(FindClientsReply reply,List<Client> found)
    {
        return
            reply
                .setSuccess(true)
                .setSuccessMessage("Found " + found.size() + " client(s)")
                .setClients(
                    found
                        .stream()
                        .map(c -> mapper.map(c,ClientData.class))
                        .collect(Collectors.toList()));
    }

    private FindClientsReply
    failure(FindClientsReply reply,Throwable e)
    {
        return
            reply
                .setSuccess(false)
                .setFailureMessage("Unable to find clients: " + e.getMessage())
                .setException(ExceptionData.of(e));
    }
}

//////////////////////////////////////////////////////////////////////////////
