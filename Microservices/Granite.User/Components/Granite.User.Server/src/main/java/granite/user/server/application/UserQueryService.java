//////////////////////////////////////////////////////////////////////////////
// UserCommandService.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.domain.IUserRepository;
import granite.user.server.domain.User;
import granite.user.service.requestreply.*;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import strata.foundation.core.collection.IMultiMap;
import strata.foundation.core.concurrent.CurrentThreadExecutor;
import strata.foundation.core.transfer.ExceptionData;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public
class UserQueryService
    implements ITransactionalUserQueryService
{
    private final IUserRepository  repository;
    private final ModelMapper            mapper;
    private final Logger                 logger;

    @Inject
    public UserQueryService(IUserRepository r,ModelMapper m)
    {
        repository = r;
        mapper     = m;
        logger     = LogManager.getLogger(UserQueryService.class);
    }

    @Override
    public CompletionStage<FindUsersByKeyReply>
    findUsersByKey(FindUsersByKeyRequest request)
    {
        logger.debug("findUsersByKey({})",request.getRequestId());
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(findRequest -> mapToContext(findRequest))
                .thenApply(context -> findUsers(context))
                .thenApply(context -> buildReply(context))
                .exceptionally(exception -> buildReply(exception,request));
    }

    @Override
    public CompletionStage<FindUsersByKeySetReply>
    findUsersByKeySet(FindUsersByKeySetRequest request)
    {
        logger.debug("findUsersByKeySet({})",request.getRequestId());
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(findRequest -> mapToContext(findRequest))
                .thenApply(context -> findUsers(context))
                .thenApply(context -> buildReply(context))
                .exceptionally(exception -> buildReply(exception,request));
    }

    protected FindUsersByKeyRequest
    validate(FindUsersByKeyRequest request)
    {
        logger.debug("validate((FindUsersByKey){})",request.getRequestId());

        Objects.requireNonNull(
            request.getKey(),
            "validation failed: key was not provided");
        return request;
    }

    protected FindUsersByKeySetRequest
    validate(FindUsersByKeySetRequest request)
    {
        logger.debug("validate((FindUsersByKeySet){})",request.getRequestId());

        Objects.requireNonNull(
            request.getKeys(),
            "validation failed: key set was not provided");

        if (request.getKeys().isEmpty())
            throw new IllegalArgumentException(
                "validation failed: key set was empty");

        return request;
    }

    protected FindUsersByKeyContext
    mapToContext(FindUsersByKeyRequest request)
    {
        logger.debug(
            "mapToContext((FindUsersByKeyRequest){})",
            request.getRequestId());

        return new FindUsersByKeyContext(request);
    }

    protected FindUsersByKeySetContext
    mapToContext(FindUsersByKeySetRequest request)
    {
        logger.debug(
            "mapToContext((FindUsersByKeySetRequest){})",
            request.getRequestId());

        return new FindUsersByKeySetContext(request);
    }

    protected FindUsersByKeyContext
    findUsers(FindUsersByKeyContext context)
    {
        UserKey key = context.getKey();
        logger.debug("find Users((FindUsersByKeyContext)context)");

        switch (key.getName())
        {
            case UserKey.USER_ID:
                Long id = key.getValueAsLong();
                return context.setFoundUsers(findUsersById(id));

            case UserKey.USER_FOO:
                String foo = key.getValue();
                return context.setFoundUsers(findUsersByFoo(foo));

            default:
                throw new IllegalArgumentException("Invalid key: " + key.getName());
        }
    }

    protected FindUsersByKeySetContext
    findUsers(FindUsersByKeySetContext context)
    {
        UserKeySet keys = context.getRequestedKeys();
        logger.debug("find Users((FindUsersByKeySetContext)context)");

        switch (keys.getName())
        {
            case UserKeySet.USER_IDS:
                Map<Long,Optional<User>> idsResult =
                    findUsersByIds(keys.getValuesAsLongs());

                return
                    context
                        .setFoundKeys(
                            UserKeySet.ofIds(
                                idsResult
                                    .entrySet()
                                    .stream()
                                    .filter(entry -> entry.getValue().isPresent())
                                    .map(entry -> entry.getKey())
                                    .collect(Collectors.toSet())))
                        .setMissingKeys(
                            UserKeySet.ofIds(
                                idsResult
                                    .entrySet()
                                    .stream()
                                    .filter(entry -> entry.getValue().isEmpty())
                                    .map(entry -> entry.getKey())
                                    .collect(Collectors.toSet())))
                        .setFoundUsers(
                            idsResult
                                .values()
                                .stream()
                                .filter(entity -> entity.isPresent())
                                .map(entity -> entity.get())
                                .toList());

            case UserKeySet.USER_FOOS:
                IMultiMap<String,User> foosResult =
                    findUsersByFoos(keys.getValues());

                return
                    context
                        .setFoundKeys(
                            UserKeySet.ofFoos(
                                foosResult
                                    .entrySet()
                                    .stream()
                                    .filter(entry -> !entry.getValue().isEmpty())
                                    .map(entry -> entry.getKey())
                                    .collect(Collectors.toSet())))
                        .setMissingKeys(
                            UserKeySet.ofFoos(
                                foosResult
                                    .entrySet()
                                    .stream()
                                    .filter(entry -> entry.getValue().isEmpty())
                                    .map(entry -> entry.getKey())
                                    .collect(Collectors.toSet())))
                        .setFoundUsers(
                            foosResult
                                .values()
                                .stream()
                                .filter(entity -> !entity.isEmpty())
                                .flatMap(entity -> entity.stream())
                                .toList());

            default:
                throw new IllegalArgumentException("Invalid key: " + keys.getName());
        }
    }

    protected List<User>
    findUsersById(Long id)
    {
        logger.debug("find UsersById({})",id);
        return
            repository
                .findById(id)
                .map(entity -> List.of(entity))
                .orElse(List.of());
    }

    protected List<User>
    findUsersByFoo(String foo)
    {
        logger.debug("find UsersByFoo({})",foo);
        return repository.findAllByFoo(foo);
    }

    protected Map<Long,Optional<User>>
    findUsersByIds(Set<Long> ids)
    {
        logger.debug("find UsersByIds({})",ids);
        return repository.findAllByIdIn(ids);
    }

    protected IMultiMap<String,User>
    findUsersByFoos(Set<String> foos)
    {
        logger.debug("find UsersByFoos({})",foos);
        return repository.findAllByFooIn(foos);
    }

    private FindUsersByKeyReply
    buildReply(FindUsersByKeyContext context)
    {
        List<User> found = context.getFoundUsers();

        logger.info("building successful reply");

        return
            new FindUsersByKeyReply(context.getRequest())
                .setSuccess(true)
                .setSuccessMessage("Found " + found.size() + " user(s)")
                .setFoundUsers(
                    mapper.map(
                        found,
                        new TypeToken<List<UserData>>() {}.getType()));
    }

    private FindUsersByKeyReply
    buildReply(Throwable e,FindUsersByKeyRequest request)
    {
        logger.info("building exception reply: {}",e.getMessage());
        return
            new FindUsersByKeyReply(request)
                .setSuccess(false)
                .setFailureMessage(
                    "Unable to find user(s) for key " +
                    request.getKey() + ": " + e.getMessage())
                .setException(ExceptionData.of(e));
    }

    private FindUsersByKeySetReply
    buildReply(FindUsersByKeySetContext context)
    {
        List<User> found = context.getFoundUsers();

        logger.info("building successful reply");

        return
            new FindUsersByKeySetReply(context.getRequest())
                .setSuccess(true)
                .setSuccessMessage(
                    "Found " + found.size() +
                    " entities for key set: " + context.getRequestedKeys())
                .setFoundKeys(context.getFoundKeys())
                .setMissingKeys(context.getMissingKeys())
                .setFoundUsers(
                    mapper.map(
                        found,
                        new TypeToken<List<UserData>>() {}.getType()));
    }

    private FindUsersByKeySetReply
    buildReply(Throwable e,FindUsersByKeySetRequest request)
    {
        logger.info("building exception reply: {}",e.getMessage());
        return
            new FindUsersByKeySetReply(request)
                .setSuccess(false)
                .setFailureMessage(
                    "Unable to find users for key set " +
                        request.getKeys() + ": " + e.getMessage())
                .setException(ExceptionData.of(e));
    }

}

//////////////////////////////////////////////////////////////////////////////
