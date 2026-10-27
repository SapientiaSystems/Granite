//////////////////////////////////////////////////////////////////////////////
// UserCommandService.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.foundation.core.security.ApplicationRoles;
import granite.user.server.domain.IUserEvent;
import granite.user.server.domain.IUserEventObserver;
import granite.user.server.domain.IUserRepository;
import granite.user.server.domain.User;
import granite.user.service.event.IUserEventSender;
import granite.user.service.event.UserEvent;
import granite.user.service.requestreply.*;
import granite.user.service.requestreply.UserData;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import strata.foundation.core.concurrent.CurrentThreadExecutor;
import strata.foundation.core.event.EventIdentifiersData;
import strata.foundation.core.event.StandardEventType;
import strata.foundation.core.transfer.ExceptionData;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public
class UserCommandService
    implements ITransactionalUserCommandService, IUserEventObserver
{
    private static final Map<PasswordEncoder,String> DUMMY_HASHES = new ConcurrentHashMap<>();

    private final IUserRepository    repository;
    private final IUserEventSender   sender;
    private final ModelMapper        mapper;
    private final PasswordEncoder    encoder;
    private final LoginLockoutPolicy lockoutPolicy;
    private final ApplicationRoles   roles;
    private final Logger             logger;

    @Inject
    public UserCommandService(
        IUserRepository    r,
        IUserEventSender   s,
        ModelMapper        m,
        PasswordEncoder    e,
        LoginLockoutPolicy p,
        ApplicationRoles   a)
    {
        repository    = r;
        sender        = s;
        mapper        = m;
        encoder       = e;
        lockoutPolicy = p;
        roles         = a;
        logger        = LogManager.getLogger(UserCommandService.class);
    }

    @Override
    public CompletionStage<CreateUserReply>
    createUser(CreateUserRequest request)
    {
        logger.debug("createUser({})",request.getRequestId());
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(createRequest -> mapToContext(createRequest))
                .thenApply(context -> createUser(context))
                .thenApply(context -> saveUser(context))
                .thenApply(context -> buildReply(context))
                .exceptionally(exception -> buildReply(exception,request));
    }

    @Override
    public CompletionStage<CreateUsersReply>
    createUsers(CreateUsersRequest request)
    {
        logger.debug("createUsers({})",request.getRequestId());
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(createRequest -> mapToContext(createRequest))
                .thenApply(context -> createUsers(context))
                .thenApply(context -> saveUsers(context))
                .thenApply(context -> buildReply(context))
                .exceptionally(exception -> buildReply(exception,request));
    }

    @Override
    public CompletionStage<UpdateUserReply>
    updateUser(UpdateUserRequest request)
    {
        logger.debug("updateUser({})",request.getRequestId());
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(updateRequest -> mapToContext(updateRequest))
                .thenApply(context -> findUser(context))
                .thenApply(context -> updateUser(context))
                .thenApply(context -> saveUser(context))
                .thenApply(context -> buildReply(context))
                .exceptionally(exception -> buildReply(exception,request));
    }

    @Override
    public CompletionStage<MergeUserValuesReply>
    mergeUserValues(MergeUserValuesRequest request)
    {
        logger.debug("mergeUserValues({})",request.getRequestId());
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(mergeRequest -> mapToContext(mergeRequest))
                .thenApply(context -> findUser(context))
                .thenApply(context -> mergeValues(context))
                .thenApply(context -> saveUser(context))
                .thenApply(context -> buildReply(context))
                .exceptionally(exception -> buildReply(exception,request));
    }

    @Override
    public CompletionStage<UpdateUsersReply>
    updateUsers(UpdateUsersRequest request)
    {
        logger.debug("updateUsers({})",request.getRequestId());
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(updateRequest -> mapToContext(updateRequest))
                .thenApply(context -> findUsers(context))
                .thenApply(context -> updateUsers(context))
                .thenApply(context -> saveUsers(context))
                .thenApply(context -> buildReply(context))
                .exceptionally(exception -> buildReply(exception,request));
    }


    @Override
    public CompletionStage<DestroyUserReply>
    destroyUser(DestroyUserRequest request)
    {
        logger.debug("destroyUser({})",request.getRequestId());
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(destroyRequest -> mapToContext(destroyRequest))
                .thenApply(context -> findUser(context))
                .thenApply(context -> deleteUser(context))
                .thenApply(context -> buildReply(context))
                .exceptionally(exception -> buildReply(exception,request));
    }

    @Override
    public CompletionStage<DestroyUsersReply>
    destroyUsers(DestroyUsersRequest request)
    {
        logger.debug("destroyUsers({})",request.getRequestId());
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(destroyRequest -> mapToContext(destroyRequest))
                .thenApply(context -> findUsers(context))
                .thenApply(context -> deleteUsers(context))
                .thenApply(context -> buildReply(context))
                .exceptionally(exception -> buildReply(exception,request));
    }

    @Override
    public CompletionStage<AuthenticateUserReply>
    authenticateUser(AuthenticateUserRequest request)
    {
        logger.debug("authenticateUser({})",request.getRequestId());
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(authRequest -> mapToContext(authRequest))
                .thenApply(context -> lookupUser(context))
                .thenApply(context -> checkLockout(context))
                .thenApply(context -> checkCredential(context))
                .thenApply(context -> recordOutcome(context))
                .thenApply(context -> buildReply(context))
                .exceptionally(exception -> buildReply(exception,request));
    }

    @Override
    public void
    onEvent(IUserEvent event)
    {
        switch (event.getName())
        {
            case "UserCreated":
                logger.debug("sending UserCreated event");
                sender.send(toServiceEvent(event,StandardEventType.CREATED));
                break;

            case "UserUpdated":
                logger.debug("sending UserUpdated event");
                sender.send(toServiceEvent(event,StandardEventType.UPDATED));
                break;

            case "UserDestroyed":
                logger.debug("sending UserDestroyed event");
                sender.send(toServiceEvent(event,StandardEventType.DESTROYED));
                break;

            default:
                logger.error("Encountered unknown event type: {}",event.getName());
                break;
        }
    }

    @Override
    public void
    onException(Exception e)
    {
        logger.error(
            "Observing UserEvents and caught exception: {}",
            e.getMessage());
    }

    /*
    @Override
    public CompletionStage<FindUserReply>
    findUser(FindUserRequest request)
    {
        logger.debug("findUser(" + request.getRequestId() + ")");
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(findRequest -> mapToContext(findRequest))
                .thenApply(context -> findUser(context))
                .thenApply(context -> buildReply(context))
                .exceptionally(exception -> buildReply(exception,request));
    }
     */

    protected CreateUserRequest
    validate(CreateUserRequest request)
    {
        logger.debug("<create> validate({})",request.getRequestId());

        Objects.requireNonNull(
            request.getUserToCreate(),
            "validation failed: userToCreate was not provided");
        return request;
    }

    protected CreateUsersRequest
    validate(CreateUsersRequest request)
    {
        logger.debug("<create set> validate({})",request.getRequestId());

        Objects.requireNonNull(
            request.getUsersToCreate(),
            "validation failed: usersToCreate was not provided");

        if (request.getUsersToCreate().isEmpty())
            throw new IllegalArgumentException("validation failed: usersToCreate was empty");

        return request;
    }

    protected UpdateUserRequest
    validate(UpdateUserRequest request)
    {
        logger.debug("<update> validate({})",request.getRequestId());

        Objects.requireNonNull(
            request.getUserToUpdate(),
            "validation failed: id was not provided");
        return request;
    }

    protected MergeUserValuesRequest
    validate(MergeUserValuesRequest request)
    {
        logger.debug("<merge values> validate({})",request.getRequestId());

        Objects.requireNonNull(
            request.getUserId(),
            "validation failed: userId was not provided");
        Objects.requireNonNull(
            request.getValues(),
            "validation failed: values were not provided");
        return request;
    }

    protected MergeUserValuesContext
    mapToContext(MergeUserValuesRequest request)
    {
        logger.debug("<merge values> mapToContext({})",request.getRequestId());
        return new MergeUserValuesContext(request);
    }

    protected MergeUserValuesContext
    findUser(MergeUserValuesContext context)
    {
        logger.debug("<merge values> find User");
        return
            context
                .setMergedUser(
                    repository
                        .findById(context.getUserId())
                        .map(entity -> entity.attach(this)));
    }

    protected MergeUserValuesContext
    mergeValues(MergeUserValuesContext context)
    {
        logger.debug("<merge values> merge values");

        User user =
            context
                .getMergedUser()
                .orElseThrow(
                    () ->
                        new NoSuchElementException(
                            "User not found: " + context.getUserId()));

        for (Map.Entry<String,Object> entry : context.getValues().entrySet())
            if (entry.getValue() != null)
                user.setValue(entry.getKey(),entry.getValue());
            else
                user.getValues().remove(entry.getKey());

        return context;
    }

    protected MergeUserValuesContext
    saveUser(MergeUserValuesContext context)
    {
        logger.debug("<merge values> save User");
        return
            context
                .setMergedUser(
                    repository
                        .save(
                            context
                                .getMergedUser()
                                .orElseThrow(
                                    () ->
                                        new IllegalStateException(
                                            "Error during user values merge.")))
                        .notifyUpdated());
    }

    private MergeUserValuesReply
    buildReply(MergeUserValuesContext context)
    {
        User merged =
            context
                .getMergedUser()
                .orElseThrow();

        logger.info("building successful reply");

        return
            new MergeUserValuesReply(context.getRequest())
                .setSuccess(true)
                .setSuccessMessage("User: " + merged.getPrimaryId() + " values merged")
                .setMergedUser(mapper.map(merged,UserData.class));
    }

    private String
    mergeFailureMessage(Throwable e)
    {
        Throwable cause = e instanceof CompletionException && e.getCause() != null ? e.getCause() : e;

        return
            cause instanceof NoSuchElementException
                ? cause.getMessage()
                : failureMessage(e,"Unable to merge user values");
    }

    private MergeUserValuesReply
    buildReply(Throwable e,MergeUserValuesRequest request)
    {
        logger.info("building exception reply: {}",e.getMessage());
        return
            new MergeUserValuesReply(request)
                .setSuccess(false)
                .setFailureMessage(mergeFailureMessage(e))
                .setException(ExceptionData.of(e));
    }

    protected UpdateUsersRequest
    validate(UpdateUsersRequest request)
    {
        logger.debug("<update set> validate({})",request.getRequestId());

        Objects.requireNonNull(
            request.getUsersToUpdate(),
            "validation failed: usersToUpdate was not provided");

        if (request.getUsersToUpdate().isEmpty())
            throw new IllegalArgumentException("validation failed: usersToUpdate was empty");

        return request;
    }

    protected DestroyUserRequest
    validate(DestroyUserRequest request)
    {
        logger.debug("<destroy> validate({})",request.getRequestId());

        Objects.requireNonNull(
            request.getUserId(),
            "validation failed: id was not provided");
        return request;
    }

    protected DestroyUsersRequest
    validate(DestroyUsersRequest request)
    {
        logger.debug("<destroy set> validate({})",request.getRequestId());

        Objects.requireNonNull(
            request.getUserIds(),
            "validation failed: userIds was not provided");

        if (request.getUserIds().isEmpty())
            throw new IllegalArgumentException("validation failed: userIds was empty");

        return request;
    }

    protected FindUserRequest
    validate(FindUserRequest request)
    {
        logger.debug("<find> validate({})",request.getRequestId());

        Objects.requireNonNull(
            request.getUserId(),
            "validation failed: id was not provided");
        return request;
    }

    protected AuthenticateUserRequest
    validate(AuthenticateUserRequest request)
    {
        logger.debug("<authenticate> validate({})",request.getRequestId());

        Objects.requireNonNull(request,"request");
        Objects.requireNonNull(
            request.getUserName(),
            "validation failed: userName was not provided");
        Objects.requireNonNull(
            request.getCredential(),
            "validation failed: credential was not provided");

        if (request.getCredential().isBlank())
            throw new IllegalArgumentException("validation failed: credential was blank");

        Objects.requireNonNull(
            request.getCredentialKind(),
            "validation failed: credentialKind was not provided");
        return request;
    }

    protected CreateUserContext
    mapToContext(CreateUserRequest request)
    {
        logger.debug("<create> mapToContext({})",request.getRequestId());
        return new CreateUserContext(request);
    }

    protected CreateUsersContext
    mapToContext(CreateUsersRequest request)
    {
        logger.debug("<create set> mapToContext({})",request.getRequestId());
        return new CreateUsersContext(request);
    }

    protected UpdateUserContext
    mapToContext(UpdateUserRequest request)
    {
        logger.debug("<update> mapToContext({})",request.getRequestId());
        return new UpdateUserContext(request);
    }

    protected UpdateUsersContext
    mapToContext(UpdateUsersRequest request)
    {
        logger.debug("<update set> mapToContext({})",request.getRequestId());
        return new UpdateUsersContext(request);
    }

    protected DestroyUserContext
    mapToContext(DestroyUserRequest request)
    {
        logger.debug("<destroy> mapToContext({})",request.getRequestId());
        return new DestroyUserContext(request);
    }

    protected DestroyUsersContext
    mapToContext(DestroyUsersRequest request)
    {
        logger.debug("<destroy set> mapToContext({})",request.getRequestId());
        return new DestroyUsersContext(request);
    }

    protected FindUserContext
    mapToContext(FindUserRequest request)
    {
        logger.debug("<find> mapToContext({})",request.getRequestId());
        return new FindUserContext(request);
    }

    protected AuthenticateUserContext
    mapToContext(AuthenticateUserRequest request)
    {
        logger.debug("<authenticate> mapToContext({})",request.getRequestId());
        return new AuthenticateUserContext(request);
    }

    protected AuthenticateUserContext
    lookupUser(AuthenticateUserContext context)
    {
        logger.debug("<authenticate> lookup User");
        return context.setUser(repository.findByUserName(context.getUserName()));
    }

    protected AuthenticateUserContext
    checkLockout(AuthenticateUserContext context)
    {
        logger.debug("<authenticate> check lockout");

        boolean locked =
            context
                .getUser()
                .map(user -> user.isLocked(lockoutPolicy.now()))
                .orElse(false);

        return locked ? context.setLocked(true).setFailureMessage("account locked") : context;
    }

    protected AuthenticateUserContext
    checkCredential(AuthenticateUserContext context)
    {
        logger.debug("<authenticate> check credential");

        CredentialKind kind = context.getCredentialKind();

        switch (kind)
        {
            case PASSWORD:
                User    user    = context.getUser().orElse(null);
                boolean hasHash = user != null && user.getPassword() != null;
                boolean matches =
                    encoder.matches(
                        context.getCredential(),
                        hasHash ? user.getPassword() : dummyHash());

                return context.setValid(hasHash && matches && !context.isLocked());

            case ACCESS_TOKEN:
            case SECURITY_CODE:
            default:
                return
                    context
                        .setValid(false)
                        .setFailureMessage(
                            "CredentialKind "
                                + kind
                                + " is not supported by Granite.User.authenticateUser");
        }
    }

    protected AuthenticateUserContext
    recordOutcome(AuthenticateUserContext context)
    {
        logger.debug("<authenticate> record outcome");

        User user = context.getUser().orElse(null);

        if (user == null || context.isLocked() || context.getCredentialKind() != CredentialKind.PASSWORD)
            return context;

        if (context.getValid())
        {
            if (user.getFailedLoginAttempts() > 0 || user.getLockedUntil() != null)
                repository.save(user.resetFailedLogins());

            return context;
        }

        user.recordFailedLogin(
            lockoutPolicy.now(),
            lockoutPolicy.getMaxAttempts(),
            lockoutPolicy.getLockDuration());
        repository.save(user);
        return context;
    }

    protected CreateUserContext
    createUser(CreateUserContext context)
    {
        logger.debug("create User");
        return
            context
                .setCreatedUser(mapToUser(context.getUserToCreate()));
    }

    protected CreateUsersContext
    createUsers(CreateUsersContext context)
    {
        logger.debug("create Users");

        List<User> created = new ArrayList<>();

        for (CreateUserData entityData : context.getUsersToCreate())
            created.add(mapToUser(entityData));

        return context.setCreatedUsers(created);
    }

    protected UpdateUserContext
    findUser(UpdateUserContext context)
    {
        logger.debug("<update> find User");
        return
            context
                .setUpdatedUser(
                    repository
                        .findById(
                            context
                                .getUserToUpdate()
                                .getUserId())
                        .map(entity -> entity.attach(this)));
    }

    protected DestroyUserContext
    findUser(DestroyUserContext context)
    {
        logger.debug("<destroy> find User");
        return
            context
                .setDestroyedUser(
                    repository
                        .findById(context.getUserId())
                        .map(entity -> entity.attach(this)));
    }

    protected FindUserContext
    findUser(FindUserContext context)
    {
        logger.debug("<find> find User");
        return
            context
                .setFoundUser(
                    repository
                        .findById(context.getUserId())
                        .map(entity -> entity.attach(this)));
    }

    protected UpdateUserContext
    updateUser(UpdateUserContext context)
    {
        UpdateUserData data = context.getUserToUpdate();

        logger.debug("update User");

        User user = context.getUpdatedUser().orElseThrow();

        requireAddedRoles(user,data);
        applyUpdate(user,data);

        return context;
    }

    protected DestroyUserContext
    deleteUser(DestroyUserContext context)
    {
        logger.debug("destroy User");

        repository
            .delete(
                context
                    .getDestroyedUser()
                    .orElseThrow(
                        () ->
                            new NoSuchElementException(
                                "Error during user destroy")));

        context
            .getDestroyedUser()
            .ifPresent(User::notifyDestroyed);

        return context;
    }

    protected DestroyUsersContext
    findUsers(DestroyUsersContext context)
    {
        logger.debug("<destroy set> find Users");

        Map<Long,Optional<User>> found =
            repository.findAllByIdIn(context.getUserIds());

        List<User> entities = new ArrayList<>();

        for (Optional<User> entry : found.values())
            entry
                .map(entity -> entity.attach(this))
                .ifPresent(entities::add);

        return context.setDestroyedUsers(entities);
    }

    protected UpdateUsersContext
    findUsers(UpdateUsersContext context)
    {
        logger.debug("<update set> find Users");

        Set<Long> ids =
            context
                .getUsersToUpdate()
                .stream()
                .map(UpdateUserData::getUserId)
                .collect(Collectors.toSet());

        Map<Long,Optional<User>> found =
            repository.findAllByIdIn(ids);

        List<User> entities = new ArrayList<>();

        for (Optional<User> entry : found.values())
            entry
                .map(entity -> entity.attach(this))
                .ifPresent(entities::add);

        return context.setUpdatedUsers(entities);
    }

    protected UpdateUsersContext
    updateUsers(UpdateUsersContext context)
    {
        logger.debug("<update set> update Users");

        Map<Long,UpdateUserData> dataById =
            context
                .getUsersToUpdate()
                .stream()
                .collect(
                    Collectors.toMap(
                        UpdateUserData::getUserId,
                        d -> d));

        for (User entity : context.getUpdatedUsers())
        {
            UpdateUserData data = dataById.get(entity.getPrimaryId());

            if (data != null)
                requireAddedRoles(entity,data);
        }

        for (User entity : context.getUpdatedUsers())
        {
            UpdateUserData data = dataById.get(entity.getPrimaryId());

            if (data == null)
                continue;

            applyUpdate(entity,data);
        }

        return context;
    }

    protected DestroyUsersContext
    deleteUsers(DestroyUsersContext context)
    {
        logger.debug("<destroy set> destroy Users");

        List<User> deleted = new ArrayList<>();

        for (User entity : context.getDestroyedUsers())
        {
            repository.delete(entity);
            entity.notifyDestroyed();
            deleted.add(entity);
        }

        return context.setDestroyedUsers(deleted);
    }

    protected CreateUserContext
    saveUser(CreateUserContext context)
    {
        logger.debug("<create> save User");
        return
            context
                .setCreatedUser(
                    repository
                        .save(
                            context
                                .getCreatedUser()
                                .orElseThrow(
                                    () ->
                                        new IllegalStateException(
                                            "Error during user creation.")))
                        .attach(this)
                        .notifyCreated());
    }

    protected CreateUsersContext
    saveUsers(CreateUsersContext context)
    {
        logger.debug("<create> save Users");

        List<User> persisted = new ArrayList<>();

        for (User entity : context.getCreatedUsers())
            persisted.add(repository.save(entity).attach(this).notifyCreated());

        return context.setCreatedUsers(persisted);
    }

    protected UpdateUserContext
    saveUser(UpdateUserContext context)
    {
        logger.debug("<update> save User");
        return
            context
                .setUpdatedUser(
                    repository
                        .save(
                            context
                                .getUpdatedUser()
                                .orElseThrow(
                                    () ->
                                        new IllegalStateException(
                                            "Error during user update.")))
                        .notifyUpdated());
    }

    protected UpdateUsersContext
    saveUsers(UpdateUsersContext context)
    {
        logger.debug("<update set> save Users");

        List<User> persisted = new ArrayList<>();

        for (User entity : context.getUpdatedUsers())
            persisted.add(repository.save(entity).notifyUpdated());

        return context.setUpdatedUsers(persisted);
    }

    private String
    failureMessage(Throwable e,String fallback)
    {
        Throwable cause = e instanceof CompletionException && e.getCause() != null ? e.getCause() : e;

        return cause instanceof UnknownRoleException ? cause.getMessage() : fallback;
    }

    private void
    requireRoles(Set<String> authorities)
    {
        if (authorities == null)
            return;

        for (String authority : authorities)
            if (!roles.isRole(authority))
                throw new UnknownRoleException(authority);
    }

    private void
    requireAddedRoles(User user,UpdateUserData data)
    {
        Set<String> requested = data.getAuthorities().orElse(null);

        if (requested == null)
            return;

        Set<String> added = new LinkedHashSet<>(requested);

        if (user.getAuthorities() != null)
            added.removeAll(user.getAuthorities());

        requireRoles(added);
    }

    private User
    mapToUser(CreateUserData data)
    {
        requireRoles(data.getAuthorities());

        User user = mapper.map(data,User.class);

        user.setAuthorities(roles.expand(user.getAuthorities()));

        user.setPassword(hash(data.getPassword()));
        replaceValues(user,data.getValues());
        return user;
    }

    private void
    applyUpdate(User user,UpdateUserData data)
    {
        data
            .getUserName()
            .filter(userName -> !userName.isBlank())
            .ifPresent(user::setUserName);
        data
            .getExternalIdentity()
            .filter(identity -> !identity.isBlank())
            .ifPresent(user::setExternalIdentity);
        data
            .getPassword()
            .filter(password -> !password.isBlank())
            .ifPresent(password -> user.setPassword(hash(password)));
        data
            .getAuthorities()
            .ifPresent(authorities -> user.setAuthorities(roles.expand(authorities)));
        data
            .getValues()
            .ifPresent(values -> replaceUnreservedValues(user,values));
    }

    private void
    replaceUnreservedValues(User user,Map<String,Object> values)
    {
        Map<String,Object> reserved = new LinkedHashMap<>();

        for (Map.Entry<String,Object> entry : user.getValues().entrySet())
            if (ReservedValueKeys.isReserved(entry.getKey()))
                reserved.put(entry.getKey(),entry.getValue());

        Map<String,Object> unreserved = new LinkedHashMap<>();

        if (values != null)
            for (Map.Entry<String,Object> entry : values.entrySet())
                if (!ReservedValueKeys.isReserved(entry.getKey()))
                    unreserved.put(entry.getKey(),entry.getValue());

        replaceValues(user,unreserved);

        for (Map.Entry<String,Object> entry : reserved.entrySet())
            user.setValue(entry.getKey(),entry.getValue());
    }

    private void
    replaceValues(User user,Map<String,Object> values)
    {
        user.getValues().clear();

        if (values == null)
            return;

        for (Map.Entry<String,Object> entry : values.entrySet())
            if (entry.getValue() != null)
                user.setValue(entry.getKey(),entry.getValue());
    }

    private String
    dummyHash()
    {
        return DUMMY_HASHES.computeIfAbsent(encoder,e -> e.encode(UUID.randomUUID().toString()));
    }

    private String
    hash(String password)
    {
        if (password == null || password.isBlank())
            return null;

        return encoder.encode(password);
    }

    private CreateUserReply
    buildReply(CreateUserContext context)
    {
        User created =
            context
                .getCreatedUser()
                .orElseThrow();

        logger.info("building successful reply");

        return
            new CreateUserReply(context.getRequest())
                .setSuccess(true)
                .setSuccessMessage("User: " + created.getPrimaryId() + " created")
                .setCreatedUser(mapper.map(created,UserData.class));
    }

    private CreateUserReply
    buildReply(Throwable e,CreateUserRequest request)
    {
        logger.info("building exception reply: {}",e.getMessage());
        return
            new CreateUserReply(request)
                .setSuccess(false)
                .setFailureMessage(failureMessage(e,"Unable to create user"))
                .setException(ExceptionData.of(e));
    }

    private CreateUsersReply
    buildReply(CreateUsersContext context)
    {
        logger.info("building successful set reply");

        List<CreateUserData> requested = context.getUsersToCreate();
        Set<String> createdUserNames = new HashSet<>();
        List<UserData> created =
            context
                .getCreatedUsers()
                .stream()
                .map(entity -> mapper.map(entity,UserData.class))
                .map(entity -> {createdUserNames.add(entity.getUserName()); return entity;})
                .toList();
        List<CreateUserData> failed =
            requested
                .stream()
                .filter(r -> !createdUserNames.contains(r.getUserName()))
                .toList();

        return
            new CreateUsersReply(context.getRequest())
                .setSuccess(failed.isEmpty())
                .setSuccessMessage("Users created: " + created.size())
                .setRequestedUsers(requested)
                .setFailedUsers(failed)
                .setCreatedUsers(created);
    }

    private CreateUsersReply
    buildReply(Throwable e,CreateUsersRequest request)
    {
        logger.info("building exception set reply: {}",e.getMessage());
        return
            new CreateUsersReply(request)
                .setSuccess(false)
                .setFailureMessage(failureMessage(e,"Unable to create users"))
                .setException(ExceptionData.of(e));
    }

    private UpdateUserReply
    buildReply(UpdateUserContext context)
    {
        User created =
            context
                .getUpdatedUser()
                .orElseThrow();

        logger.info("building successful reply");

        return
            new UpdateUserReply(context.getRequest())
                .setSuccess(true)
                .setSuccessMessage("User: " + created.getPrimaryId() + " created")
                .setUpdatedUser(mapper.map(created,UserData.class));
    }

    private UpdateUserReply
    buildReply(Throwable e,UpdateUserRequest request)
    {
        logger.info("building exception reply: {}",e.getMessage());
        return
            new UpdateUserReply(request)
                .setSuccess(false)
                .setFailureMessage(failureMessage(e,"Unable to update user"))
                .setException(ExceptionData.of(e));
    }

    private UpdateUsersReply
    buildReply(UpdateUsersContext context)
    {
        logger.info("building successful set reply");

        List<UpdateUserData> requested = context.getUsersToUpdate();
        List<UserData> updated =
            context
                .getUpdatedUsers()
                .stream()
                .map(entity -> mapper.map(entity,UserData.class))
                .toList();
        Set<Long> updatedIds =
            updated
                .stream()
                .map(UserData::getUserId)
                .collect(Collectors.toSet());
        List<UpdateUserData> failed =
            requested
                .stream()
                .filter(r -> !updatedIds.contains(r.getUserId()))
                .toList();

        return
            new UpdateUsersReply(context.getRequest())
                .setSuccess(failed.isEmpty())
                .setSuccessMessage("Users updated: " + updated.size())
                .setRequestedUsers(requested)
                .setFailedUsers(failed)
                .setUpdatedUsers(updated);
    }

    private UpdateUsersReply
    buildReply(Throwable e,UpdateUsersRequest request)
    {
        logger.info("building exception set reply: {}",e.getMessage());
        return
            new UpdateUsersReply(request)
                .setSuccess(false)
                .setFailureMessage(failureMessage(e,"Unable to update users"))
                .setException(ExceptionData.of(e));
    }

    private DestroyUserReply
    buildReply(DestroyUserContext context)
    {
        User destroyed =
            context
                .getDestroyedUser()
                .orElseThrow();

        logger.info("building successful reply");

        return
            new DestroyUserReply(context.getRequest())
                .setSuccess(true)
                .setSuccessMessage("User: " + destroyed.getPrimaryId() + " destroyed")
                .setDestroyedUser(mapper.map(destroyed,UserData.class));
    }

    private DestroyUserReply
    buildReply(Throwable e,DestroyUserRequest request)
    {
        logger.info("building exception reply: {}",e.getMessage());
        return
            new DestroyUserReply(request)
                .setSuccess(false)
                .setFailureMessage("Unable to destroy user")
                .setException(ExceptionData.of(e));
    }

    private DestroyUsersReply
    buildReply(DestroyUsersContext context)
    {
        logger.info("building successful set reply");

        Set<Long> requested = context.getUserIds();
        List<UserData> destroyed =
            context
                .getDestroyedUsers()
                .stream()
                .map(entity -> mapper.map(entity,UserData.class))
                .toList();
        Set<Long> destroyedIds =
            destroyed
                .stream()
                .map(UserData::getUserId)
                .collect(Collectors.toSet());
        Set<Long> failed =
            requested
                .stream()
                .filter(id -> !destroyedIds.contains(id))
                .collect(Collectors.toSet());

        return
            new DestroyUsersReply(context.getRequest())
                .setSuccess(failed.isEmpty())
                .setSuccessMessage("Users destroyed: " + destroyed.size())
                .setRequestedUserIds(requested)
                .setFailedUserIds(failed)
                .setDestroyedUsers(destroyed);
    }

    private DestroyUsersReply
    buildReply(Throwable e,DestroyUsersRequest request)
    {
        logger.info("building exception set reply: {}",e.getMessage());
        return
            new DestroyUsersReply(request)
                .setSuccess(false)
                .setFailureMessage("Unable to destroy users")
                .setException(ExceptionData.of(e));
    }

    private FindUserReply
    buildReply(FindUserContext context)
    {
        User found =
            context
                .getFoundUser()
                .orElseThrow();

        logger.info("building successful reply");

        return
            new FindUserReply(context.getRequest())
                .setSuccess(true)
                .setSuccessMessage("User: " + found.getPrimaryId() + " found")
                .setFoundUser(mapper.map(found,UserData.class));
    }

    private FindUserReply
    buildReply(Throwable e,FindUserRequest request)
    {
        logger.info("building exception reply: {}",e.getMessage());
        return
            new FindUserReply(request)
                .setSuccess(false)
                .setFailureMessage("Unable to find user " + request.getUserId())
                .setException(ExceptionData.of(e));
    }

    private AuthenticateUserReply
    buildReply(AuthenticateUserContext context)
    {
        logger.info("building authenticate reply (valid={})",context.getValid());

        AuthenticateUserReply reply =
            new AuthenticateUserReply(context.getRequest())
                .setSuccess(true)
                .setValid(context.getValid());

        if (context.getValid())
        {
            UserData userData =
                context
                    .getUser()
                    .map(u -> mapper.map(u,UserData.class))
                    .orElse(null);

            reply
                .setUser(userData)
                .setSuccessMessage(
                    "User: "
                        + (userData != null ? userData.getUserId() : null)
                        + " authenticated");
        }
        else if (context.getFailureMessage() != null)
        {
            reply.setFailureMessage(context.getFailureMessage());
        }

        return reply;
    }

    private AuthenticateUserReply
    buildReply(Throwable e,AuthenticateUserRequest request)
    {
        logger.info("building authenticate exception reply: {}",e.getMessage());
        return
            new AuthenticateUserReply(request)
                .setSuccess(false)
                .setValid(false)
                .setFailureMessage("Unable to authenticate user")
                .setException(ExceptionData.of(e));
    }

    private UserEvent
    toServiceEvent(IUserEvent domainEvent,StandardEventType eventType)
    {
        return
            UserEvent
                .newBuilder()
                .setEventType(eventType)
                .setIdentifiers(
                    EventIdentifiersData
                        .newBuilder()
                        .setEventId(UUID.randomUUID().toString())
                        .setCorrelationId(domainEvent.getCorrelationId())
                        .setTimestamp(domainEvent.getTimestamp())
                        .build())
                .setSourceType(
                    domainEvent
                        .getSource()
                        .getClass()
                        .getCanonicalName())
                .setSourceId(
                    domainEvent
                        .getSource()
                        .getPrimaryId())
                .build();
    }
}

//////////////////////////////////////////////////////////////////////////////
