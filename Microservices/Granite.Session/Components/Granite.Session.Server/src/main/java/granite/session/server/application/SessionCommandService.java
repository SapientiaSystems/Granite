//////////////////////////////////////////////////////////////////////////////
// SessionCommandService.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.ISessionEvent;
import granite.session.server.domain.ISessionEventObserver;
import granite.session.server.domain.ISessionRepository;
import granite.session.server.domain.Session;
import granite.session.service.event.ISessionEventSender;
import granite.session.service.event.SessionEvent;
import granite.session.service.requestreply.LoginReply;
import granite.session.service.requestreply.LoginRequest;
import granite.session.service.requestreply.LoginWithOauth2Reply;
import granite.session.service.requestreply.LoginWithOauth2Request;
import granite.session.service.requestreply.LogoutAllForUserReply;
import granite.session.service.requestreply.LogoutAllForUserRequest;
import granite.session.service.requestreply.LogoutReply;
import granite.session.service.requestreply.LogoutRequest;
import granite.session.service.requestreply.RefreshReply;
import granite.session.service.requestreply.RefreshRequest;
import granite.session.service.requestreply.SessionData;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.AuthenticateUserReply;
import granite.user.service.requestreply.AuthenticateUserRequest;
import granite.user.service.requestreply.CreateUserData;
import granite.user.service.requestreply.CreateUserReply;
import granite.user.service.requestreply.CreateUserRequest;
import granite.user.service.requestreply.FindUsersByExternalIdentityReply;
import granite.user.service.requestreply.FindUsersByExternalIdentityRequest;
import granite.user.service.requestreply.UserData;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import strata.foundation.core.concurrent.CurrentThreadExecutor;
import strata.foundation.core.event.EventIdentifiersData;
import strata.foundation.core.event.StandardEventType;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class SessionCommandService
    implements ITransactionalSessionCommandService,ISessionEventObserver
{
    private final ISessionRepository        repository;
    private final IJwtTokenService          jwtTokenService;
    private final IRefreshTokenService      refreshTokenService;
    private final IUserCommandServiceClient userCommandServiceClient;
    private final IUserQueryServiceClient   userQueryServiceClient;
    private final IOauth2IdTokenValidator   oauth2Validator;
    private final ILoginGate                loginGate;
    private final ISessionEventSender       sender;
    private final ModelMapper               mapper;
    private final Duration                  sessionTtl;
    private final Duration                  maxLifetime;
    private final Clock                     clock;
    private final Logger                    logger;

    @Inject
    public
    SessionCommandService(
        ISessionRepository        repository,
        IJwtTokenService          jwtTokenService,
        IRefreshTokenService      refreshTokenService,
        IUserCommandServiceClient userCommandServiceClient,
        IUserQueryServiceClient   userQueryServiceClient,
        IOauth2IdTokenValidator   oauth2Validator,
        ILoginGate                loginGate,
        ISessionEventSender       sender,
        ModelMapper               mapper,
        Duration                  sessionTtl,
        Duration                  maxLifetime,
        Clock                     clock)
    {
        this.repository               = repository;
        this.jwtTokenService          = jwtTokenService;
        this.refreshTokenService      = refreshTokenService;
        this.userCommandServiceClient = userCommandServiceClient;
        this.userQueryServiceClient   = userQueryServiceClient;
        this.oauth2Validator          = oauth2Validator;
        this.loginGate                = loginGate;
        this.sender                   = sender;
        this.mapper                   = mapper;
        this.sessionTtl               = sessionTtl;
        this.maxLifetime              = maxLifetime;
        this.clock                    = clock;
        this.logger                   = LogManager.getLogger(SessionCommandService.class);
    }

    @Override
    public CompletionStage<LoginReply>
    login(LoginRequest request)
    {
        logger.debug(
            "login({})",
            request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(this::mapToContext)
                .thenApply(this::dispatch)
                .thenApply(this::applyLoginGate)
                .thenApply(this::createSession)
                .thenApply(this::mintJwt)
                .thenApply(this::save)
                .thenApply(this::buildReply)
                .exceptionally(e -> buildReply(e,request));
    }

    @Override
    public CompletionStage<LoginWithOauth2Reply>
    loginWithOauth2(LoginWithOauth2Request request)
    {
        logger.debug(
            "loginWithOauth2({})",
            request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validateOauth2(request),new CurrentThreadExecutor())
                .thenApply(this::mapToOauth2Context)
                .thenApply(this::validateIdToken)
                .thenApply(this::resolveUser)
                .thenApply(this::applyOauth2LoginGate)
                .thenApply(this::createOauth2Session)
                .thenApply(this::mintOauth2Jwt)
                .thenApply(this::saveOauth2)
                .thenApply(this::buildOauth2Reply)
                .exceptionally(e -> buildOauth2Reply(e,request));
    }

    @Override
    public CompletionStage<LogoutReply>
    logout(LogoutRequest request)
    {
        logger.debug(
            "logout({})",
            request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validateLogout(request),new CurrentThreadExecutor())
                .thenApply(this::mapToLogoutContext)
                .thenApply(this::loadForLogout)
                .thenApply(this::destroy)
                .thenApply(this::buildLogoutReply)
                .exceptionally(e -> buildLogoutReply(e,request));
    }

    @Override
    public CompletionStage<LogoutAllForUserReply>
    logoutAllForUser(LogoutAllForUserRequest request)
    {
        logger.debug(
            "logoutAllForUser({})",
            request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validateLogoutAllForUser(request),new CurrentThreadExecutor())
                .thenApply(this::mapToLogoutAllForUserContext)
                .thenApply(this::loadAllForLogout)
                .thenApply(this::destroyAll)
                .thenApply(this::buildLogoutAllForUserReply)
                .exceptionally(e -> buildLogoutAllForUserReply(e,request));
    }

    @Override
    public CompletionStage<RefreshReply>
    refresh(RefreshRequest request)
    {
        logger.debug(
            "refresh({})",
            request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validateRefresh(request),new CurrentThreadExecutor())
                .thenApply(this::mapToRefreshContext)
                .thenApply(this::loadForRefresh)
                .thenApply(this::revokeOnReuse)
                .thenApply(this::verifyLive)
                .thenApply(this::updateExpires)
                .thenApply(this::rotateRefreshToken)
                .thenApply(this::remintJwt)
                .thenApply(this::saveRefreshed)
                .thenApply(this::buildRefreshReply)
                .exceptionally(e -> buildRefreshReply(e,request));
    }

    @Override
    public void
    onEvent(ISessionEvent event)
    {
        switch (event.getName())
        {
            case "SessionCreated":
                logger.debug("sending SessionCreated event");
                sender.send(toServiceEvent(event,StandardEventType.CREATED));
                break;

            case "SessionUpdated":
                logger.debug("sending SessionUpdated event");
                sender.send(toServiceEvent(event,StandardEventType.UPDATED));
                break;

            case "SessionDestroyed":
                logger.debug("sending SessionDestroyed event");
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
            "Observing SessionEvents and caught exception: {}",
            e.getMessage());
    }

    protected LoginRequest
    validate(LoginRequest request)
    {
        logger.debug("<login> validate");

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
        Objects.requireNonNull(
            request.getDeviceId(),
            "validation failed: deviceId was not provided");
        return request;
    }

    protected LoginContext
    mapToContext(LoginRequest request)
    {
        logger.debug("<login> mapToContext({})",request.getRequestId());
        return new LoginContext(request);
    }

    protected LoginContext
    dispatch(LoginContext context)
    {
        logger.debug("<login> dispatch(kind={})",context.getCredentialKind());

        switch (context.getCredentialKind())
        {
            case PASSWORD:
                return dispatchPassword(context);

            case ACCESS_TOKEN:
                return dispatchAccessToken(context);

            case SECURITY_CODE:
            default:
                return context.setUnsupportedKind(true);
        }
    }

    protected LoginContext
    applyLoginGate(LoginContext context)
    {
        logger.debug("<login> applyLoginGate");

        if (context.getValidatedUserId() == null || context.isUnsupportedKind())
            return context;

        if (loginGate.permits(context.getValidatedAuthorities()))
            return context;

        logger.info("<login> refused by login gate");
        return context.setValidatedUserId(null);
    }

    protected LoginContext
    createSession(LoginContext context)
    {
        logger.debug("<login> createSession");

        if (context.getValidatedUserId() == null || context.isUnsupportedKind())
            return context;

        Instant now          = Instant.now(clock);
        String  refreshToken = refreshTokenService.generate();
        Session session      =
            new Session()
                .setUserId(context.getValidatedUserId())
                .setDeviceId(context.getDeviceId())
                .setExpires(capExpires(now,now))
                .setAccessToken("")
                .setRefreshTokenHash(refreshTokenService.hash(refreshToken))
                .setAuthorities(context.getValidatedAuthorities());

        session.setCreated(now);
        return
            context
                .setCreated(session)
                .setRefreshToken(refreshToken);
    }

    protected LoginContext
    mintJwt(LoginContext context)
    {
        logger.debug("<login> mintJwt");

        if (context.getCreated() == null)
            return context;

        Session session = repository.save(context.getCreated());
        String  token   = jwtTokenService.mint(session);

        session.setAccessToken(token);
        return
            context
                .setCreated(session)
                .setJwt(token);
    }

    protected LoginContext
    save(LoginContext context)
    {
        logger.debug("<login> save");

        if (context.getCreated() == null)
            return context;

        Session session = repository.save(context.getCreated()).attach(this);

        session.notifyCreated();
        return context.setCreated(session);
    }

    protected LoginWithOauth2Request
    validateOauth2(LoginWithOauth2Request request)
    {
        logger.debug("<loginWithOauth2> validate");

        Objects.requireNonNull(request,"request");
        Objects.requireNonNull(
            request.getProvider(),
            "validation failed: provider was not provided");

        if (request.getProvider().isBlank())
            throw new IllegalArgumentException("validation failed: provider was blank");

        Objects.requireNonNull(
            request.getIdToken(),
            "validation failed: idToken was not provided");

        if (request.getIdToken().isBlank())
            throw new IllegalArgumentException("validation failed: idToken was blank");

        Objects.requireNonNull(
            request.getDeviceId(),
            "validation failed: deviceId was not provided");
        return request;
    }

    protected LoginWithOauth2Context
    mapToOauth2Context(LoginWithOauth2Request request)
    {
        logger.debug("<loginWithOauth2> mapToContext({})",request.getRequestId());
        return new LoginWithOauth2Context(request);
    }

    protected LoginWithOauth2Context
    validateIdToken(LoginWithOauth2Context context)
    {
        logger.debug("<loginWithOauth2> validateIdToken(provider={})",context.getProvider());

        try
        {
            Oauth2ValidatedIdentity identity =
                oauth2Validator.validate(context.getProvider(),context.getIdToken());

            return context.setValidatedIdentity(identity);
        }
        catch (Oauth2ValidationException e)
        {
            logger.info("<loginWithOauth2> id token rejected: {}",e.getMessage());
            return context.setFailureReason(e.getMessage());
        }
    }

    protected LoginWithOauth2Context
    resolveUser(LoginWithOauth2Context context)
    {
        logger.debug("<loginWithOauth2> resolveUser");

        if (context.getValidatedIdentity() == null)
            return context;

        String   provider = context.getValidatedIdentity().getProvider();
        String   sub      = context.getValidatedIdentity().getSub();
        UserData existing = findUserByExternalIdentity(provider,sub);

        if (existing != null)
            return
                context
                    .setUserId(existing.getUserId())
                    .setAuthorities(existing.getAuthorities());

        String composite = provider + ":" + sub;

        try
        {
            CreateUserReply reply =
                userCommandServiceClient
                    .createUser(
                        new CreateUserRequest()
                            .setUserToCreate(
                                new CreateUserData()
                                    .setExternalIdentity(composite)
                                    .setUserName(sub)))
                    .toCompletableFuture()
                    .join();

            if (reply != null && reply.isSuccess() && reply.getCreatedUser() != null)
                return
                    context
                        .setUserId(reply.getCreatedUser().getUserId())
                        .setAuthorities(reply.getCreatedUser().getAuthorities());

            UserData recovered = findUserByExternalIdentity(provider,sub);

            if (recovered != null)
                return
                    context
                        .setUserId(recovered.getUserId())
                        .setAuthorities(recovered.getAuthorities());

            String failureMessage =
                reply != null && reply.getFailureMessage() != null
                    ? reply.getFailureMessage()
                    : "unable to provision user";
            return context.setFailureReason(failureMessage);
        }
        catch (RuntimeException createUserException)
        {
            logger.info(
                "<loginWithOauth2> createUser failed, re-querying: {}",
                createUserException.getMessage());

            try
            {
                UserData recovered = findUserByExternalIdentity(provider,sub);

                if (recovered != null)
                    return
                        context
                            .setUserId(recovered.getUserId())
                            .setAuthorities(recovered.getAuthorities());
            }
            catch (RuntimeException recoveryException)
            {
                createUserException.addSuppressed(recoveryException);
            }

            throw createUserException;
        }
    }

    protected LoginWithOauth2Context
    applyOauth2LoginGate(LoginWithOauth2Context context)
    {
        logger.debug("<loginWithOauth2> applyLoginGate");

        if (context.getUserId() == null || context.getFailureReason() != null)
            return context;

        if (loginGate.permits(context.getAuthorities()))
            return context;

        logger.info("<loginWithOauth2> refused by login gate");
        return
            context
                .setUserId(null)
                .setFailureReason("invalid credentials");
    }

    protected LoginWithOauth2Context
    createOauth2Session(LoginWithOauth2Context context)
    {
        logger.debug("<loginWithOauth2> createSession");

        if (context.getUserId() == null)
            return context;

        Instant now          = Instant.now(clock);
        String  refreshToken = refreshTokenService.generate();
        Session session      =
            new Session()
                .setUserId(context.getUserId())
                .setDeviceId(context.getDeviceId())
                .setExpires(capExpires(now,now))
                .setAccessToken("")
                .setRefreshTokenHash(refreshTokenService.hash(refreshToken))
                .setAuthorities(context.getAuthorities());

        session.setCreated(now);
        return
            context
                .setCreated(session)
                .setRefreshToken(refreshToken);
    }

    protected LoginWithOauth2Context
    mintOauth2Jwt(LoginWithOauth2Context context)
    {
        logger.debug("<loginWithOauth2> mintJwt");

        if (context.getCreated() == null)
            return context;

        Session session = repository.save(context.getCreated());
        String  token   = jwtTokenService.mint(session);

        session.setAccessToken(token);
        return
            context
                .setCreated(session)
                .setJwt(token);
    }

    protected LoginWithOauth2Context
    saveOauth2(LoginWithOauth2Context context)
    {
        logger.debug("<loginWithOauth2> save");

        if (context.getCreated() == null)
            return context;

        Session session = repository.save(context.getCreated()).attach(this);

        session.notifyCreated();
        return context.setCreated(session);
    }

    private LoginContext
    dispatchPassword(LoginContext context)
    {
        AuthenticateUserRequest authRequest =
            new AuthenticateUserRequest()
                .setUserName(context.getUserName())
                .setCredential(context.getCredential())
                .setCredentialKind(context.getCredentialKind());
        AuthenticateUserReply   reply       =
            userCommandServiceClient
                .authenticateUser(authRequest)
                .toCompletableFuture()
                .join();

        if (reply != null
            && reply.getValid()
            && reply.getUser() != null
            && reply.getUser().getUserId() != null)
            return
                context
                    .setValidatedUserId(reply.getUser().getUserId())
                    .setValidatedAuthorities(reply.getUser().getAuthorities());

        return context;
    }

    private LoginContext
    dispatchAccessToken(LoginContext context)
    {
        Optional<Session> found =
            repository.findByAccessToken(context.getCredential());

        if (found.isEmpty())
            return context;

        Session existing = found.get();
        Instant now      = Instant.now(clock);

        if (existing.getExpires() == null || !existing.getExpires().isAfter(now))
            return context;

        return
            context
                .setValidatedUserId(existing.getUserId())
                .setValidatedAuthorities(existing.getAuthorities());
    }

    private LoginReply
    buildReply(LoginContext context)
    {
        if (context.isUnsupportedKind())
        {
            logger.info("<login> unsupported credential kind");
            return
                new LoginReply(context.getRequest())
                    .setSuccess(false)
                    .setFailureMessage(
                        "CredentialKind "
                            + context.getCredentialKind()
                            + " is not supported");
        }

        if (context.getValidatedUserId() == null || context.getCreated() == null)
        {
            logger.info("<login> invalid credentials");
            return
                new LoginReply(context.getRequest())
                    .setSuccess(false)
                    .setFailureMessage("invalid credentials");
        }

        Session   session = context.getCreated();
        SessionData data  = mapper.map(session,SessionData.class);

        logger.info("<login> session: {} created",session.getSessionId());
        return
            new LoginReply(context.getRequest())
                .setSuccess(true)
                .setSuccessMessage("Session: " + session.getSessionId() + " created")
                .setSessionData(data)
                .setAccessToken(session.getAccessToken())
                .setRefreshToken(context.getRefreshToken());
    }

    private LoginReply
    buildReply(Throwable e,LoginRequest request)
    {
        logger.info("<login> exception reply: {}",e.getMessage());
        return
            new LoginReply(request)
                .setSuccess(false)
                .setFailureMessage("Unable to log in")
                .setException(ExceptionData.of(e));
    }

    protected LogoutRequest
    validateLogout(LogoutRequest request)
    {
        logger.debug("<logout> validate");

        Objects.requireNonNull(request,"request");
        Objects.requireNonNull(
            request.getSessionId(),
            "validation failed: sessionId was not provided");
        return request;
    }

    protected LogoutContext
    mapToLogoutContext(LogoutRequest request)
    {
        logger.debug("<logout> mapToContext({})",request.getRequestId());
        return new LogoutContext(request);
    }

    protected LogoutContext
    loadForLogout(LogoutContext context)
    {
        logger.debug("<logout> load({})",context.getSessionId());

        Optional<Session> found = repository.findById(context.getSessionId());

        if (found.isEmpty())
            return context.setNotFound(true);

        return context.setLoaded(found.get());
    }

    protected LogoutContext
    destroy(LogoutContext context)
    {
        logger.debug("<logout> destroy");

        if (context.isNotFound() || context.getLoaded() == null)
            return context;

        Session session = context.getLoaded();

        session.attach(this);
        session.destroy();
        repository.delete(session);
        return context;
    }

    protected LogoutAllForUserRequest
    validateLogoutAllForUser(LogoutAllForUserRequest request)
    {
        logger.debug("<logoutAllForUser> validate");

        Objects.requireNonNull(request,"request");
        Objects.requireNonNull(
            request.getUserId(),
            "validation failed: userId was not provided");
        return request;
    }

    protected LogoutAllForUserContext
    mapToLogoutAllForUserContext(LogoutAllForUserRequest request)
    {
        logger.debug("<logoutAllForUser> mapToContext({})",request.getRequestId());
        return new LogoutAllForUserContext(request);
    }

    protected LogoutAllForUserContext
    loadAllForLogout(LogoutAllForUserContext context)
    {
        logger.debug("<logoutAllForUser> loadAll({})",context.getUserId());

        List<Session> sessions = repository.findAllByUserId(context.getUserId());

        return context.setLoaded(sessions);
    }

    protected LogoutAllForUserContext
    destroyAll(LogoutAllForUserContext context)
    {
        logger.debug("<logoutAllForUser> destroyAll(count={})",context.getLoaded().size());

        Set<Long> destroyed = new TreeSet<>();

        for (Session session : context.getLoaded())
        {
            session.attach(this);
            session.destroy();
            repository.delete(session);
            destroyed.add(session.getSessionId());
        }

        return context.setDestroyed(destroyed);
    }

    protected RefreshRequest
    validateRefresh(RefreshRequest request)
    {
        logger.debug("<refresh> validate");

        Objects.requireNonNull(request,"request");
        Objects.requireNonNull(
            request.getRefreshToken(),
            "validation failed: refreshToken was not provided");

        if (request.getRefreshToken().isBlank())
            throw new IllegalArgumentException("validation failed: refreshToken was blank");

        return request;
    }

    protected RefreshContext
    mapToRefreshContext(RefreshRequest request)
    {
        logger.debug("<refresh> mapToContext({})",request.getRequestId());
        return new RefreshContext(request);
    }

    protected RefreshContext
    loadForRefresh(RefreshContext context)
    {
        logger.debug("<refresh> load");

        String            hash  = refreshTokenService.hash(context.getPresentedRefreshToken());
        Optional<Session> found = repository.findByRefreshTokenHash(hash);

        if (found.isPresent())
            return context.setLoaded(found.get());

        Optional<Session> replayed = repository.findByPreviousRefreshTokenHash(hash);

        if (replayed.isPresent())
            return
                context
                    .setReused(true)
                    .setLoaded(replayed.get());

        return context.setNotFound(true);
    }

    protected RefreshContext
    revokeOnReuse(RefreshContext context)
    {
        logger.debug("<refresh> revokeOnReuse");

        if (!context.isReused() || context.getLoaded() == null)
            return context;

        Session session = context.getLoaded();

        logger.warn(
            "<refresh> refresh token reuse detected; destroying session: {}",
            session.getSessionId());
        session.attach(this);
        session.destroy();
        repository.delete(session);
        return context;
    }

    protected RefreshContext
    verifyLive(RefreshContext context)
    {
        logger.debug("<refresh> verifyLive");

        if (context.isRejected())
            return context;

        Session session = context.getLoaded();
        Instant now     = Instant.now(clock);

        if (session.getExpires() == null || session.getExpires().isBefore(now))
            return context.setExpired(true);

        if (session.getCreated() == null || !session.getCreated().plus(maxLifetime).isAfter(now))
            return context.setExpired(true);

        return context;
    }

    protected RefreshContext
    updateExpires(RefreshContext context)
    {
        logger.debug("<refresh> updateExpires");

        if (context.isRejected())
            return context;

        Session session = context.getLoaded();
        Instant now     = Instant.now(clock);

        session.attach(this);
        session.setExpires(capExpires(session.getCreated(),now));
        return context;
    }

    protected RefreshContext
    rotateRefreshToken(RefreshContext context)
    {
        logger.debug("<refresh> rotateRefreshToken");

        if (context.isRejected())
            return context;

        String refreshToken = refreshTokenService.generate();

        context
            .getLoaded()
            .rotateRefreshTokenHash(refreshTokenService.hash(refreshToken));
        return context.setRefreshToken(refreshToken);
    }

    protected RefreshContext
    remintJwt(RefreshContext context)
    {
        logger.debug("<refresh> remintJwt");

        if (context.isRejected())
            return context;

        Session session = context.getLoaded();
        String  token   = jwtTokenService.mint(session);

        session.setAccessToken(token);
        return context.setJwt(token);
    }

    protected RefreshContext
    saveRefreshed(RefreshContext context)
    {
        logger.debug("<refresh> save");

        if (context.isRejected())
            return context;

        Session session = repository.save(context.getLoaded());

        return context.setLoaded(session);
    }

    private LogoutReply
    buildLogoutReply(LogoutContext context)
    {
        if (context.isNotFound())
        {
            logger.info("<logout> session not found: {}",context.getSessionId());
            return
                new LogoutReply(context.getRequest())
                    .setSuccess(false)
                    .setFailureMessage("session not found");
        }

        logger.info("<logout> session: {} destroyed",context.getSessionId());
        return
            new LogoutReply(context.getRequest())
                .setSuccess(true)
                .setSuccessMessage("Session: " + context.getSessionId() + " destroyed");
    }

    private LogoutReply
    buildLogoutReply(Throwable e,LogoutRequest request)
    {
        logger.info("<logout> exception reply: {}",e.getMessage());
        return
            new LogoutReply(request)
                .setSuccess(false)
                .setFailureMessage("Unable to log out")
                .setException(ExceptionData.of(e));
    }

    private LogoutAllForUserReply
    buildLogoutAllForUserReply(LogoutAllForUserContext context)
    {
        logger.info(
            "<logoutAllForUser> user: {} destroyed {} session(s)",
            context.getUserId(),
            context.getDestroyed().size());
        return
            new LogoutAllForUserReply(context.getRequest())
                .setSuccess(true)
                .setSuccessMessage(
                    "User: "
                        + context.getUserId()
                        + " destroyed "
                        + context.getDestroyed().size()
                        + " session(s)")
                .setDestroyed(context.getDestroyed());
    }

    private LogoutAllForUserReply
    buildLogoutAllForUserReply(Throwable e,LogoutAllForUserRequest request)
    {
        logger.info("<logoutAllForUser> exception reply: {}",e.getMessage());
        return
            new LogoutAllForUserReply(request)
                .setSuccess(false)
                .setFailureMessage("Unable to log out all for user")
                .setException(ExceptionData.of(e));
    }

    private RefreshReply
    buildRefreshReply(RefreshContext context)
    {
        if (context.isNotFound())
        {
            logger.info("<refresh> invalid refresh token");
            return
                new RefreshReply(context.getRequest())
                    .setSuccess(false)
                    .setFailureMessage("invalid refresh token");
        }

        if (context.isReused())
        {
            logger.info("<refresh> session: {} revoked",context.getLoaded().getSessionId());
            return
                new RefreshReply(context.getRequest())
                    .setSuccess(false)
                    .setFailureMessage("refresh token reused; session revoked");
        }

        if (context.isExpired())
        {
            logger.info("<refresh> session expired: {}",context.getLoaded().getSessionId());
            return
                new RefreshReply(context.getRequest())
                    .setSuccess(false)
                    .setFailureMessage("session expired");
        }

        Session     session = context.getLoaded();
        SessionData data    = mapper.map(session,SessionData.class);

        logger.info("<refresh> session: {} refreshed",session.getSessionId());
        return
            new RefreshReply(context.getRequest())
                .setSuccess(true)
                .setSuccessMessage("Session: " + session.getSessionId() + " refreshed")
                .setSessionData(data)
                .setAccessToken(session.getAccessToken())
                .setRefreshToken(context.getRefreshToken());
    }

    private RefreshReply
    buildRefreshReply(Throwable e,RefreshRequest request)
    {
        logger.info("<refresh> exception reply: {}",e.getMessage());
        return
            new RefreshReply(request)
                .setSuccess(false)
                .setFailureMessage("Unable to refresh")
                .setException(ExceptionData.of(e));
    }

    private Instant
    capExpires(Instant created,Instant now)
    {
        Instant sliding  = now.plus(sessionTtl);
        Instant absolute = created.plus(maxLifetime);

        return sliding.isBefore(absolute) ? sliding : absolute;
    }

    private Long
    findUserIdByExternalIdentity(String provider,String sub)
    {
        UserData found = findUserByExternalIdentity(provider,sub);

        return found != null ? found.getUserId() : null;
    }

    private UserData
    findUserByExternalIdentity(String provider,String sub)
    {
        FindUsersByExternalIdentityReply reply =
            userQueryServiceClient
                .findUsersByExternalIdentity(
                    new FindUsersByExternalIdentityRequest()
                        .setProvider(provider)
                        .setExternalId(sub))
                .toCompletableFuture()
                .join();

        if (reply == null || reply.getUsers() == null)
            return null;

        List<UserData> users = reply.getUsers();

        if (users.isEmpty())
            return null;

        return users.get(0);
    }

    private LoginWithOauth2Reply
    buildOauth2Reply(LoginWithOauth2Context context)
    {
        if (context.getFailureReason() != null)
        {
            logger.info("<loginWithOauth2> failure: {}",context.getFailureReason());
            return
                new LoginWithOauth2Reply(context.getRequest())
                    .setSuccess(false)
                    .setFailureMessage(context.getFailureReason());
        }

        if (context.getUserId() == null || context.getCreated() == null)
        {
            logger.info("<loginWithOauth2> invalid identity");
            return
                new LoginWithOauth2Reply(context.getRequest())
                    .setSuccess(false)
                    .setFailureMessage("invalid identity");
        }

        Session     session = context.getCreated();
        SessionData data    = mapper.map(session,SessionData.class);

        logger.info("<loginWithOauth2> session: {} created",session.getSessionId());
        return
            new LoginWithOauth2Reply(context.getRequest())
                .setSuccess(true)
                .setSuccessMessage("Session: " + session.getSessionId() + " created")
                .setSessionData(data)
                .setAccessToken(session.getAccessToken())
                .setRefreshToken(context.getRefreshToken());
    }

    private LoginWithOauth2Reply
    buildOauth2Reply(Throwable e,LoginWithOauth2Request request)
    {
        logger.info("<loginWithOauth2> exception reply: {}",e.getMessage());
        return
            new LoginWithOauth2Reply(request)
                .setSuccess(false)
                .setFailureMessage("Unable to log in")
                .setException(ExceptionData.of(e));
    }

    private SessionEvent
    toServiceEvent(ISessionEvent domainEvent,StandardEventType eventType)
    {
        return
            SessionEvent
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
