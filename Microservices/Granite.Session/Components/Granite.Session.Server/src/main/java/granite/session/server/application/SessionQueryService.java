//////////////////////////////////////////////////////////////////////////////
// SessionQueryService.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.ISessionRepository;
import granite.session.server.domain.Session;
import granite.session.service.requestreply.*;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import strata.foundation.core.concurrent.CurrentThreadExecutor;
import strata.foundation.core.transfer.ExceptionData;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class SessionQueryService
    implements ITransactionalSessionQueryService
{
    private final ISessionRepository repository;
    private final ModelMapper        mapper;
    private final Logger             logger;

    @Inject
    public
    SessionQueryService(ISessionRepository r,ModelMapper m)
    {
        repository = r;
        mapper     = m;
        logger     = LogManager.getLogger(SessionQueryService.class);
    }

    @Override
    public CompletionStage<FindSessionsByKeyReply>
    findSessionsByKey(FindSessionsByKeyRequest request)
    {
        logger.debug("findSessionsByKey({})",request.getRequestId());
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(findRequest -> mapToContext(findRequest))
                .thenApply(context -> findSessions(context))
                .thenApply(context -> buildReply(context))
                .exceptionally(exception -> buildReply(exception,request));
    }

    @Override
    public CompletionStage<FindSessionsByKeySetReply>
    findSessionsByKeySet(FindSessionsByKeySetRequest request)
    {
        logger.debug("findSessionsByKeySet({})",request.getRequestId());
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(findRequest -> mapToContext(findRequest))
                .thenApply(context -> findSessions(context))
                .thenApply(context -> buildReply(context))
                .exceptionally(exception -> buildReply(exception,request));
    }

    @Override
    public CompletionStage<FindSessionByUserIdAndDeviceIdReply>
    findSessionByUserIdAndDeviceId(FindSessionByUserIdAndDeviceIdRequest request)
    {
        logger.debug("findSessionByUserIdAndDeviceId({})",request.getRequestId());
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(findRequest -> mapToContext(findRequest))
                .thenApply(context -> findSession(context))
                .thenApply(context -> buildReply(context))
                .exceptionally(exception -> buildReply(exception,request));
    }

    protected FindSessionsByKeyRequest
    validate(FindSessionsByKeyRequest request)
    {
        logger.debug("validate((FindSessionsByKey){})",request.getRequestId());

        Objects.requireNonNull(
            request.getKey(),
            "validation failed: key was not provided");
        return request;
    }

    protected FindSessionsByKeySetRequest
    validate(FindSessionsByKeySetRequest request)
    {
        logger.debug("validate((FindSessionsByKeySet){})",request.getRequestId());

        Objects.requireNonNull(
            request.getKeySet(),
            "validation failed: key set was not provided");

        if (request.getKeySet().isEmpty())
            throw new IllegalArgumentException(
                "validation failed: key set was empty");

        return request;
    }

    protected FindSessionByUserIdAndDeviceIdRequest
    validate(FindSessionByUserIdAndDeviceIdRequest request)
    {
        logger.debug(
            "validate((FindSessionByUserIdAndDeviceId){})",
            request.getRequestId());

        Objects.requireNonNull(
            request.getUserId(),
            "validation failed: userId was not provided");
        Objects.requireNonNull(
            request.getDeviceId(),
            "validation failed: deviceId was not provided");

        if (request.getDeviceId().isBlank())
            throw new IllegalArgumentException(
                "validation failed: deviceId was blank");

        return request;
    }

    protected FindSessionsByKeyContext
    mapToContext(FindSessionsByKeyRequest request)
    {
        logger.debug(
            "mapToContext((FindSessionsByKeyRequest){})",
            request.getRequestId());

        return new FindSessionsByKeyContext(request);
    }

    protected FindSessionsByKeySetContext
    mapToContext(FindSessionsByKeySetRequest request)
    {
        logger.debug(
            "mapToContext((FindSessionsByKeySetRequest){})",
            request.getRequestId());

        return new FindSessionsByKeySetContext(request);
    }

    protected FindSessionByUserIdAndDeviceIdContext
    mapToContext(FindSessionByUserIdAndDeviceIdRequest request)
    {
        logger.debug(
            "mapToContext((FindSessionByUserIdAndDeviceIdRequest){})",
            request.getRequestId());

        return new FindSessionByUserIdAndDeviceIdContext(request);
    }

    protected FindSessionsByKeyContext
    findSessions(FindSessionsByKeyContext context)
    {
        SessionKey key = context.getKey();
        logger.debug("find Sessions((FindSessionsByKeyContext)context)");

        switch (key.getName())
        {
            case SessionKey.SESSION_ID:
                return
                    context.setFound(
                        repository
                            .findById(key.getValueAsLong())
                            .map(entity -> List.of(entity))
                            .orElse(List.of()));

            case SessionKey.USER_ID:
                return
                    context.setFound(
                        repository.findAllByUserId(key.getValueAsLong()));

            default:
                throw new IllegalArgumentException("Invalid key: " + key.getName());
        }
    }

    protected FindSessionsByKeySetContext
    findSessions(FindSessionsByKeySetContext context)
    {
        SessionKeySet keySet = context.getKeySet();
        logger.debug("find Sessions((FindSessionsByKeySetContext)context)");

        switch (keySet.getName())
        {
            case SessionKeySet.SESSION_IDS:
                return
                    context.setFound(
                        repository
                            .findAllByIdIn(keySet.getValuesAsLongs())
                            .values()
                            .stream()
                            .filter(entity -> entity.isPresent())
                            .map(entity -> entity.get())
                            .toList());

            case SessionKeySet.USER_IDS:
                return
                    context.setFound(
                        repository
                            .findAllByUserIdIn(keySet.getValuesAsLongs())
                            .values()
                            .stream()
                            .filter(entities -> !entities.isEmpty())
                            .flatMap(entities -> entities.stream())
                            .toList());

            default:
                throw new IllegalArgumentException("Invalid key: " + keySet.getName());
        }
    }

    protected FindSessionByUserIdAndDeviceIdContext
    findSession(FindSessionByUserIdAndDeviceIdContext context)
    {
        logger.debug("find Session((FindSessionByUserIdAndDeviceIdContext)context)");
        return
            context.setFound(
                repository
                    .findByUserIdAndDeviceId(
                        context.getUserId(),
                        context.getDeviceId())
                    .orElse(null));
    }

    private FindSessionsByKeyReply
    buildReply(FindSessionsByKeyContext context)
    {
        List<Session> found = context.getFound();

        logger.info("building successful reply");

        return
            new FindSessionsByKeyReply(context.getRequest())
                .setSuccess(true)
                .setSuccessMessage("Found " + found.size() + " session(s)")
                .setSessions(
                    mapper.map(
                        found,
                        new TypeToken<List<SessionData>>() {}.getType()));
    }

    private FindSessionsByKeyReply
    buildReply(Throwable e,FindSessionsByKeyRequest request)
    {
        logger.info("building exception reply: {}",e.getMessage());
        return
            new FindSessionsByKeyReply(request)
                .setSuccess(false)
                .setFailureMessage(
                    "Unable to find session(s) for key " +
                    request.getKey() + ": " + e.getMessage())
                .setException(ExceptionData.of(e));
    }

    private FindSessionsByKeySetReply
    buildReply(FindSessionsByKeySetContext context)
    {
        List<Session> found = context.getFound();

        logger.info("building successful reply");

        return
            new FindSessionsByKeySetReply(context.getRequest())
                .setSuccess(true)
                .setSuccessMessage(
                    "Found " + found.size() +
                    " session(s) for key set: " + context.getKeySet())
                .setSessions(
                    mapper.map(
                        found,
                        new TypeToken<List<SessionData>>() {}.getType()));
    }

    private FindSessionsByKeySetReply
    buildReply(Throwable e,FindSessionsByKeySetRequest request)
    {
        logger.info("building exception reply: {}",e.getMessage());
        return
            new FindSessionsByKeySetReply(request)
                .setSuccess(false)
                .setFailureMessage(
                    "Unable to find sessions for key set " +
                    request.getKeySet() + ": " + e.getMessage())
                .setException(ExceptionData.of(e));
    }

    private FindSessionByUserIdAndDeviceIdReply
    buildReply(FindSessionByUserIdAndDeviceIdContext context)
    {
        Session found = context.getFound();

        logger.info("building successful reply");

        return
            new FindSessionByUserIdAndDeviceIdReply(context.getRequest())
                .setSuccess(true)
                .setSuccessMessage(
                    found == null ? "No session found" : "Found session")
                .setSession(
                    found == null ? null : mapper.map(found,SessionData.class));
    }

    private FindSessionByUserIdAndDeviceIdReply
    buildReply(Throwable e,FindSessionByUserIdAndDeviceIdRequest request)
    {
        logger.info("building exception reply: {}",e.getMessage());
        return
            new FindSessionByUserIdAndDeviceIdReply(request)
                .setSuccess(false)
                .setFailureMessage(
                    "Unable to find session for user " + request.getUserId() +
                    " and device " + request.getDeviceId() + ": " + e.getMessage())
                .setException(ExceptionData.of(e));
    }
}

//////////////////////////////////////////////////////////////////////////////
