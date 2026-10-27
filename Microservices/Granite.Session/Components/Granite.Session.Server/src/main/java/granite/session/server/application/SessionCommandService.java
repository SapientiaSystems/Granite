//////////////////////////////////////////////////////////////////////////////
// SessionCommandService.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.foundation.core.security.ApplicationRoles;
import granite.session.server.domain.ISessionEvent;
import granite.session.server.domain.ISessionEventObserver;
import granite.session.server.domain.IOktaLoginTransactionRepository;
import granite.session.server.domain.ISessionRepository;
import granite.session.server.domain.OktaLoginTransaction;
import granite.session.server.domain.Session;
import granite.session.server.domain.SignInMethod;
import granite.session.service.requestreply.SessionClosedReason;
import granite.session.service.event.ISessionEventSender;
import granite.session.service.event.SessionEvent;
import granite.session.service.requestreply.CompleteOktaLoginReply;
import granite.session.service.requestreply.CompleteOktaLoginRequest;
import granite.session.service.requestreply.LoginReply;
import granite.session.service.requestreply.LoginRequest;
import granite.session.service.requestreply.LogoutAllForUserReply;
import granite.session.service.requestreply.LogoutAllForUserRequest;
import granite.session.service.requestreply.LogoutReply;
import granite.session.service.requestreply.LogoutRequest;
import granite.session.service.requestreply.RefreshReply;
import granite.session.service.requestreply.RefreshRequest;
import granite.session.service.requestreply.ResolveSessionReply;
import granite.session.service.requestreply.ResolveSessionRequest;
import granite.session.service.requestreply.SessionData;
import granite.session.service.requestreply.StartOktaLoginReply;
import granite.session.service.requestreply.StartOktaLoginRequest;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.AuthenticateUserReply;
import granite.user.service.requestreply.AuthenticateUserRequest;
import granite.user.service.requestreply.CreateUserData;
import granite.user.service.requestreply.CreateUserReply;
import granite.user.service.requestreply.CreateUserRequest;
import granite.user.service.requestreply.CredentialKind;
import granite.user.service.requestreply.FindUsersByExternalIdentityReply;
import granite.user.service.requestreply.FindUsersByExternalIdentityRequest;
import granite.user.service.requestreply.FindUsersByKeyReply;
import granite.user.service.requestreply.FindUsersByKeyRequest;
import granite.user.service.requestreply.UpdateUserData;
import granite.user.service.requestreply.UpdateUserReply;
import granite.user.service.requestreply.UpdateUserRequest;
import granite.user.service.requestreply.UserData;
import granite.user.service.requestreply.UserKey;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import strata.foundation.core.concurrent.CurrentThreadExecutor;
import strata.foundation.core.event.EventIdentifiersData;
import strata.foundation.core.event.StandardEventType;
import strata.foundation.core.transfer.ExceptionData;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public
class SessionCommandService
    implements ITransactionalSessionCommandService,ISessionEventObserver
{
    private final ISessionRepository              repository;
    private final IOktaLoginTransactionRepository transactionRepository;
    private final IJwtTokenService                jwtTokenService;
    private final IRefreshTokenService            refreshTokenService;
    private final IUserCommandServiceClient       userCommandServiceClient;
    private final IUserQueryServiceClient         userQueryServiceClient;
    private final IOktaClient                     oktaClient;
    private final OktaSettings                    oktaSettings;
    private final OktaRoles                       oktaRoles;
    private final ILoginGate                      loginGate;
    private final ISessionEventSender             sender;
    private final ModelMapper                     mapper;
    private final Duration                        maxLifetime;
    private final Duration                        renewalMargin;
    private final Clock                           clock;
    private final ConflictDetector                conflictDetector;
    private final Map<String,OktaIdentity>        exchangedIdentities;
    private final Logger                          logger;

    private static final Duration ACTIVITY_INTERVAL   = Duration.ofMinutes(1);
    private static final String   INVALID_SESSION     = "invalid session";
    private static final String   INVALID_CREDENTIALS = "invalid credentials";
    private static final String   OKTA_PROVIDER       = "okta";

    @Inject
    public
    SessionCommandService(
        ISessionRepository              repository,
        IOktaLoginTransactionRepository transactionRepository,
        IJwtTokenService                jwtTokenService,
        IRefreshTokenService            refreshTokenService,
        IUserCommandServiceClient       userCommandServiceClient,
        IUserQueryServiceClient         userQueryServiceClient,
        IOktaClient                     oktaClient,
        OktaSettings                    oktaSettings,
        ILoginGate                      loginGate,
        ISessionEventSender             sender,
        ModelMapper                     mapper,
        Duration                        maxLifetime,
        Duration                        renewalMargin,
        Clock                           clock,
        ApplicationRoles                roles)
    {
        this.repository               = repository;
        this.transactionRepository    = transactionRepository;
        this.jwtTokenService          = jwtTokenService;
        this.refreshTokenService      = refreshTokenService;
        this.userCommandServiceClient = userCommandServiceClient;
        this.userQueryServiceClient   = userQueryServiceClient;
        this.oktaClient               = oktaClient;
        this.oktaSettings             = oktaSettings != null ? oktaSettings : new OktaSettings();
        this.oktaRoles                = new OktaRoles(this.oktaSettings,roles);
        this.loginGate                = loginGate;
        this.sender                   = sender;
        this.mapper                   = mapper;
        this.maxLifetime              = maxLifetime;
        this.renewalMargin            = renewalMargin;
        this.clock                    = clock;
        this.conflictDetector         = new ConflictDetector();
        this.exchangedIdentities      = new ConcurrentHashMap<>();
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
                .thenApply(this::applyLifecycle)
                .thenApply(this::createSession)
                .thenApply(this::mintJwt)
                .thenApply(this::linkReplaced)
                .thenApply(this::save)
                .thenApply(this::buildReply)
                .exceptionally(e -> buildReply(e,request));
    }

    @Override
    public CompletionStage<StartOktaLoginReply>
    startOktaLogin(StartOktaLoginRequest request)
    {
        logger.debug(
            "startOktaLogin({})",
            request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validateStartOkta(request),new CurrentThreadExecutor())
                .thenApply(this::mapToStartOktaContext)
                .thenApply(this::prepareOktaTransaction)
                .thenApply(this::buildAuthorizeUrl)
                .thenApply(this::saveOktaTransaction)
                .thenApply(this::buildStartOktaReply)
                .exceptionally(e -> buildStartOktaReply(e,request));
    }

    @Override
    public CompletionStage<CompleteOktaLoginReply>
    completeOktaLogin(CompleteOktaLoginRequest request)
    {
        logger.debug(
            "completeOktaLogin({})",
            request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validateCompleteOkta(request),new CurrentThreadExecutor())
                .thenApply(this::mapToCompleteOktaContext)
                .thenApply(this::consumeOktaTransaction)
                .thenApply(this::exchangeOktaCode)
                .thenApply(this::resolveOktaUser)
                .thenApply(this::applyOktaLoginGate)
                .thenApply(this::applyOktaLifecycle)
                .thenApply(this::createOktaSession)
                .thenApply(this::mintOktaJwt)
                .thenApply(this::linkOktaReplaced)
                .thenApply(this::saveOktaSession)
                .thenApply(this::buildCompleteOktaReply)
                .exceptionally(e -> buildCompleteOktaReply(e,request));
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
                .thenApply(this::close)
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
                .thenApply(this::closeAll)
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
                .thenApply(this::markUpdated)
                .thenApply(this::rotateRefreshToken)
                .thenApply(this::remintJwt)
                .thenApply(this::saveRefreshed)
                .thenApply(this::buildRefreshReply)
                .exceptionally(e -> buildRefreshReply(e,request));
    }

    @Override
    public CompletionStage<ResolveSessionReply>
    resolveSession(ResolveSessionRequest request)
    {
        logger.debug(
            "resolveSession({})",
            request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validateResolve(request),new CurrentThreadExecutor())
                .thenApply(this::mapToResolveContext)
                .thenApply(this::loadForResolve)
                .thenApply(this::checkLifetime)
                .thenApply(this::renew)
                .thenApply(this::recordActivity)
                .thenApply(this::saveResolved)
                .thenApply(this::buildResolveReply)
                .exceptionally(e -> buildResolveReply(e,request));
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
    applyLifecycle(LoginContext context)
    {
        logger.debug("<login> applyLifecycle");

        if (context.getValidatedUserId() == null || context.isUnsupportedKind())
            return context;

        return
            context.setLifecycle(
                prepareLifecycle(context.getValidatedUserId(),context.getDeviceId()));
    }

    protected LoginContext
    createSession(LoginContext context)
    {
        logger.debug("<login> createSession");

        if (context.getValidatedUserId() == null || context.isUnsupportedKind())
            return context;

        String refreshToken  = refreshTokenService.generate();
        String sessionHandle = refreshTokenService.generate();

        return
            context
                .setCreated(
                    newSession(
                        context.getLifecycle(),
                        context.getValidatedUserId(),
                        context.getDeviceId(),
                        context.getValidatedAuthorities(),
                        refreshToken,
                        sessionHandle,
                        context.getCredentialKind() == CredentialKind.ACCESS_TOKEN
                            ? SignInMethod.ACCESS_TOKEN
                            : SignInMethod.PASSWORD))
                .setRefreshToken(refreshToken)
                .setSessionHandle(sessionHandle);
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
    linkReplaced(LoginContext context)
    {
        logger.debug("<login> linkReplaced");

        if (context.getCreated() != null)
            linkReplacedSource(context.getLifecycle(),context.getCreated());

        return context;
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

    protected StartOktaLoginRequest
    validateStartOkta(StartOktaLoginRequest request)
    {
        logger.debug("<startOktaLogin> validate");

        Objects.requireNonNull(request,"request");
        Objects.requireNonNull(
            request.getDeviceId(),
            "validation failed: deviceId was not provided");

        if (request.getDeviceId().isBlank())
            throw new IllegalArgumentException("validation failed: deviceId was blank");

        return request;
    }

    protected StartOktaLoginContext
    mapToStartOktaContext(StartOktaLoginRequest request)
    {
        logger.debug("<startOktaLogin> mapToContext({})",request.getRequestId());
        return new StartOktaLoginContext(request);
    }

    protected StartOktaLoginContext
    prepareOktaTransaction(StartOktaLoginContext context)
    {
        logger.debug("<startOktaLogin> prepareTransaction");

        if (!oktaSettings.isEnabled())
            return context.setRejected(true);

        Instant now           = Instant.now(clock);
        String  transactionId = refreshTokenService.generate();
        String  verifier      = refreshTokenService.generate();

        return
            context
                .setTransactionId(transactionId)
                .setCodeChallenge(codeChallenge(verifier))
                .setPromptLogin(!liveSessions(repository.findOpenByDeviceId(context.getDeviceId()),now).isEmpty())
                .setTransaction(
                    new OktaLoginTransaction()
                        .setHashedTransactionId(refreshTokenService.hash(transactionId))
                        .setState(refreshTokenService.generate())
                        .setNonce(refreshTokenService.generate())
                        .setCodeVerifier(verifier)
                        .setReturnTo(sanitizeReturnTo(context.getReturnTo()))
                        .setDeviceId(context.getDeviceId())
                        .setExpires(now.plus(oktaSettings.getTransactionTtl())));
    }

    protected StartOktaLoginContext
    buildAuthorizeUrl(StartOktaLoginContext context)
    {
        logger.debug("<startOktaLogin> buildAuthorizeUrl(prompt={})",context.isPromptLogin());

        if (context.isRejected())
            return context;

        OktaLoginTransaction transaction = context.getTransaction();

        return
            context.setAuthorizeUrl(
                oktaClient.buildAuthorizeUrl(
                    transaction.getState(),
                    transaction.getNonce(),
                    context.getCodeChallenge(),
                    context.isPromptLogin()));
    }

    protected StartOktaLoginContext
    saveOktaTransaction(StartOktaLoginContext context)
    {
        logger.debug("<startOktaLogin> save");

        if (context.isRejected())
            return context;

        return context.setTransaction(transactionRepository.save(context.getTransaction()));
    }

    protected CompleteOktaLoginRequest
    validateCompleteOkta(CompleteOktaLoginRequest request)
    {
        logger.debug("<completeOktaLogin> validate");

        Objects.requireNonNull(request,"request");
        return request;
    }

    protected CompleteOktaLoginContext
    mapToCompleteOktaContext(CompleteOktaLoginRequest request)
    {
        logger.debug("<completeOktaLogin> mapToContext({})",request.getRequestId());
        return new CompleteOktaLoginContext(request);
    }

    protected CompleteOktaLoginContext
    consumeOktaTransaction(CompleteOktaLoginContext context)
    {
        logger.debug("<completeOktaLogin> consumeTransaction");

        if (!oktaSettings.isEnabled())
            return context.reject("okta login disabled");

        if (isBlank(context.getTransactionId()))
            return context.reject("missing transaction");

        String                         hash  = refreshTokenService.hash(context.getTransactionId());
        Optional<OktaLoginTransaction> found = transactionRepository.findByHashedTransactionId(hash);

        if (found.isEmpty())
            return context.reject("unknown transaction");

        OktaLoginTransaction transaction = found.get();

        transactionRepository.delete(transaction);
        context
            .setHashedTransactionId(hash)
            .setTransaction(transaction);

        if (transaction.isExpired(Instant.now(clock)))
            return context.reject("expired transaction");

        if (!matches(transaction.getDeviceId(),context.getDeviceId()))
            return context.reject("device mismatch");

        if (!matches(transaction.getState(),context.getState()))
            return context.reject("state mismatch");

        if (isBlank(context.getCode()))
            return context.reject("missing code");

        return context;
    }

    protected CompleteOktaLoginContext
    exchangeOktaCode(CompleteOktaLoginContext context)
    {
        logger.debug("<completeOktaLogin> exchangeCode");

        if (context.isRejected())
            return context;

        OktaIdentity remembered = exchangedIdentities.get(context.getHashedTransactionId());

        if (remembered != null)
            return context.setIdentity(remembered);

        OktaLoginTransaction transaction = context.getTransaction();

        try
        {
            OktaIdentity identity =
                oktaClient.exchange(
                    context.getCode(),
                    transaction.getCodeVerifier(),
                    transaction.getNonce());

            exchangedIdentities.put(context.getHashedTransactionId(),identity);
            return context.setIdentity(identity);
        }
        catch (OktaLoginException e)
        {
            logger.info("<completeOktaLogin> Okta login rejected: {}",e.getMessage());
            return context.reject("okta rejected the login");
        }
    }

    protected CompleteOktaLoginContext
    resolveOktaUser(CompleteOktaLoginContext context)
    {
        logger.debug("<completeOktaLogin> resolveUser");

        if (context.isRejected())
            return context;

        OktaIdentity identity = context.getIdentity();
        UserData     user     = findUserByExternalIdentity(OKTA_PROVIDER,identity.getSubject());

        if (user == null)
        {
            Optional<Set<String>> assigned = oktaRoles.rolesFor(identity.getGroups());

            if (assigned.isEmpty())
                return context.reject("user unavailable");

            user = createOktaUser(identity,assigned.get());
        }
        else if (oktaSettings.getRoleAssignment() == OktaRoleAssignment.EVERY_LOGIN)
        {
            Optional<Set<String>> assigned = oktaRoles.rolesFor(identity.getGroups());

            if (assigned.isEmpty())
                return context.reject("user unavailable");

            user = writeBackRoles(user,assigned.get());
        }

        if (user == null || user.getUserId() == null)
            return context.reject("user unavailable");

        return
            context
                .setUserId(user.getUserId())
                .setUserName(user.getUserName())
                .setAuthorities(storedAuthorities(user));
    }

    protected CompleteOktaLoginContext
    applyOktaLoginGate(CompleteOktaLoginContext context)
    {
        logger.debug("<completeOktaLogin> applyLoginGate");

        if (context.isRejected() || loginGate.permits(context.getAuthorities()))
            return context;

        logger.info("<completeOktaLogin> refused by login gate");
        return context.reject("refused by login gate");
    }

    protected CompleteOktaLoginContext
    applyOktaLifecycle(CompleteOktaLoginContext context)
    {
        logger.debug("<completeOktaLogin> applyLifecycle");

        if (context.isRejected())
            return context;

        return
            context.setLifecycle(
                prepareLifecycle(context.getUserId(),context.getDeviceId()));
    }

    protected CompleteOktaLoginContext
    createOktaSession(CompleteOktaLoginContext context)
    {
        logger.debug("<completeOktaLogin> createSession");

        if (context.isRejected())
            return context;

        String refreshToken  = refreshTokenService.generate();
        String sessionHandle = refreshTokenService.generate();

        return
            context
                .setCreated(
                    newSession(
                        context.getLifecycle(),
                        context.getUserId(),
                        context.getDeviceId(),
                        context.getAuthorities(),
                        refreshToken,
                        sessionHandle,
                        SignInMethod.OKTA)
                        .setOktaIdToken(context.getIdentity().getIdToken())
                        .setOktaGroups(context.getIdentity().getGroups()))
                .setSessionHandle(sessionHandle);
    }

    protected CompleteOktaLoginContext
    mintOktaJwt(CompleteOktaLoginContext context)
    {
        logger.debug("<completeOktaLogin> mintJwt");

        if (context.getCreated() == null)
            return context;

        Session session = repository.save(context.getCreated());

        session.setAccessToken(jwtTokenService.mint(session));
        return context.setCreated(session);
    }

    protected CompleteOktaLoginContext
    linkOktaReplaced(CompleteOktaLoginContext context)
    {
        logger.debug("<completeOktaLogin> linkReplaced");

        if (context.getCreated() != null)
            linkReplacedSource(context.getLifecycle(),context.getCreated());

        return context;
    }

    protected CompleteOktaLoginContext
    saveOktaSession(CompleteOktaLoginContext context)
    {
        logger.debug("<completeOktaLogin> save");

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

        if (existing.isClosed() || existing.getExpires() == null || !existing.getExpires().isAfter(now))
            return context;

        UserData user = findUserById(existing.getUserId());

        if (user == null)
            return context;

        return
            context
                .setValidatedUserId(existing.getUserId())
                .setValidatedAuthorities(
                    user.getAuthorities() != null
                        ? new LinkedHashSet<>(user.getAuthorities())
                        : new LinkedHashSet<>());
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
                .setRefreshToken(context.getRefreshToken())
                .setSessionHandle(context.getSessionHandle());
    }

    private LoginReply
    buildReply(Throwable e,LoginRequest request)
    {
        if (conflictDetector.isConflict(e))
            throw new CompletionException(e);

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
    close(LogoutContext context)
    {
        logger.debug("<logout> close");

        if (context.isNotFound() || context.getLoaded() == null || context.getLoaded().isClosed())
            return context;

        context.setEndSessionUrl(endSessionUrlFor(context.getLoaded()));
        closeSession(context.getLoaded(),SessionClosedReason.LOGOUT,Instant.now(clock));
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

        List<Session> sessions = repository.findOpenByUserId(context.getUserId());

        return context.setLoaded(sessions);
    }

    protected LogoutAllForUserContext
    closeAll(LogoutAllForUserContext context)
    {
        logger.debug("<logoutAllForUser> closeAll(count={})",context.getLoaded().size());

        Set<Long> closed = new TreeSet<>();
        Instant   now    = Instant.now(clock);

        for (Session session : context.getLoaded())
        {
            closeSession(session,SessionClosedReason.ADMIN,now);
            closed.add(session.getSessionId());
        }

        return context.setDestroyed(closed);
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
            "<refresh> refresh token reuse detected; revoking session: {}",
            session.getSessionId());

        if (!session.isClosed())
            closeSession(session,SessionClosedReason.REVOKED_SECURITY,Instant.now(clock));

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

        if (session.isClosed())
            return context.setClosed(true);

        if (!isLive(session,now))
            return context.setExpired(true);

        return context;
    }

    protected RefreshContext
    markUpdated(RefreshContext context)
    {
        logger.debug("<refresh> markUpdated");

        if (context.isRejected())
            return context;

        context
            .getLoaded()
            .attach(this)
            .notifyUpdated();
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

    protected ResolveSessionRequest
    validateResolve(ResolveSessionRequest request)
    {
        logger.debug("<resolveSession> validate");

        Objects.requireNonNull(request,"request");
        Objects.requireNonNull(
            request.getHandle(),
            "validation failed: handle was not provided");

        if (request.getHandle().isBlank())
            throw new IllegalArgumentException("validation failed: handle was blank");

        return request;
    }

    protected ResolveSessionContext
    mapToResolveContext(ResolveSessionRequest request)
    {
        logger.debug("<resolveSession> mapToContext({})",request.getRequestId());
        return new ResolveSessionContext(request);
    }

    protected ResolveSessionContext
    loadForResolve(ResolveSessionContext context)
    {
        logger.debug("<resolveSession> load");

        Optional<Session> found =
            repository.findByHashedHandle(refreshTokenService.hash(context.getHandle()));

        if (found.isEmpty())
            return context.setRejected(true);

        return context.setLoaded(found.get());
    }

    protected ResolveSessionContext
    checkLifetime(ResolveSessionContext context)
    {
        logger.debug("<resolveSession> checkLifetime");

        if (context.isRejected())
            return context;

        Session session = context.getLoaded();
        Instant now     = Instant.now(clock);

        if (session.isClosed())
            return context.setRejected(true);

        if (isLive(session,now))
            return context;

        session.attach(this);
        session.close(SessionClosedReason.EXPIRED,now);
        return
            context
                .setClosed(true)
                .setRejected(true);
    }

    protected ResolveSessionContext
    renew(ResolveSessionContext context)
    {
        logger.debug("<resolveSession> renew");

        if (context.isRejected())
            return context;

        Session session = context.getLoaded();
        Instant now     = Instant.now(clock);
        Instant exp     = jwtTokenService.getExpiration(session.getAccessToken());

        if (exp != null && exp.minus(renewalMargin).isAfter(now))
            return context;

        UserData user = findUserById(session.getUserId());

        if (user == null)
            return context.setRejected(true);

        session.attach(this);
        session
            .setAuthorities(storedAuthorities(user))
            .setAccessToken(jwtTokenService.mint(session))
            .notifyUpdated();
        return context.setRenewed(true);
    }

    protected ResolveSessionContext
    recordActivity(ResolveSessionContext context)
    {
        logger.debug("<resolveSession> recordActivity");

        if (context.isRejected())
            return context;

        Session session = context.getLoaded();
        Instant now     = Instant.now(clock);
        Instant last    = session.getLastActivity();

        if (last != null && last.plus(ACTIVITY_INTERVAL).isAfter(now))
            return context;

        session.setLastActivity(now);
        return context.setActivityRecorded(true);
    }

    protected ResolveSessionContext
    saveResolved(ResolveSessionContext context)
    {
        logger.debug("<resolveSession> save");

        if (context.getLoaded() == null || !context.isChanged())
            return context;

        return context.setLoaded(repository.save(context.getLoaded()));
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

        logger.info("<logout> session: {} closed",context.getSessionId());
        return
            new LogoutReply(context.getRequest())
                .setSuccess(true)
                .setSuccessMessage("Session: " + context.getSessionId() + " closed")
                .setEndSessionUrl(context.getEndSessionUrl());
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
            "<logoutAllForUser> user: {} closed {} session(s)",
            context.getUserId(),
            context.getDestroyed().size());
        return
            new LogoutAllForUserReply(context.getRequest())
                .setSuccess(true)
                .setSuccessMessage(
                    "User: "
                        + context.getUserId()
                        + " closed "
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

        if (context.isClosed())
        {
            logger.info("<refresh> session closed: {}",context.getLoaded().getSessionId());
            return
                new RefreshReply(context.getRequest())
                    .setSuccess(false)
                    .setFailureMessage("session closed");
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

    private ResolveSessionReply
    buildResolveReply(ResolveSessionContext context)
    {
        if (context.isRejected())
        {
            if (context.isClosed())
                logger.info(
                    "<resolveSession> session: {} closed as expired",
                    context.getLoaded().getSessionId());
            else
                logger.info("<resolveSession> invalid session");

            return
                new ResolveSessionReply(context.getRequest())
                    .setSuccess(false)
                    .setFailureMessage(INVALID_SESSION);
        }

        Session     session = context.getLoaded();
        SessionData data    = mapper.map(session,SessionData.class);

        logger.debug(
            "<resolveSession> session: {} resolved (renewed={})",
            session.getSessionId(),
            context.isRenewed());
        return
            new ResolveSessionReply(context.getRequest())
                .setSuccess(true)
                .setSuccessMessage("Session: " + session.getSessionId() + " resolved")
                .setSession(data)
                .setAccessToken(session.getAccessToken());
    }

    private ResolveSessionReply
    buildResolveReply(Throwable e,ResolveSessionRequest request)
    {
        logger.info("<resolveSession> exception reply: {}",e.getMessage());
        return
            new ResolveSessionReply(request)
                .setSuccess(false)
                .setFailureMessage(INVALID_SESSION);
    }

    private boolean
    isLive(Session session,Instant now)
    {
        if (session.getExpires() == null || !session.getExpires().isAfter(now))
            return false;

        return session.getCreated() != null && session.getCreated().plus(maxLifetime).isAfter(now);
    }

    private UserData
    findUserById(Long userId)
    {
        FindUsersByKeyReply reply =
            userQueryServiceClient
                .findUsersByKey(
                    new FindUsersByKeyRequest()
                        .setKey(UserKey.ofUserId(userId)))
                .toCompletableFuture()
                .join();

        if (reply == null || !reply.isSuccess() || reply.getFoundUsers() == null)
            return null;

        for (UserData user : reply.getFoundUsers())
            if (userId.equals(user.getUserId()))
                return user;

        return null;
    }

    private LoginLifecycle
    prepareLifecycle(Long userId,String deviceId)
    {
        Instant        now       = Instant.now(clock);
        LoginLifecycle lifecycle = new LoginLifecycle();
        List<Session>  mine      = new ArrayList<>();

        for (Session open : repository.findOpenByDeviceId(deviceId))
        {
            if (open.isClosed())
                continue;

            if (!isLive(open,now))
                closeSession(open,SessionClosedReason.EXPIRED,now);
            else if (!userId.equals(open.getUserId()))
                closeSession(open,SessionClosedReason.DISPLACED,now);
            else
                mine.add(open);
        }

        Session replaced = latest(mine);

        for (Session session : mine)
            closeSession(session,SessionClosedReason.REPLACED,now);

        if (replaced != null)
            lifecycle
                .setSource(replaced)
                .setReplacing(true);
        else
            lifecycle.setSource(latest(liveSessions(repository.findOpenByUserId(userId),now)));

        for (Session remaining : repository.findOpenByDeviceId(deviceId))
            if (!remaining.isClosed() && userId.equals(remaining.getUserId()))
                throw new ConcurrentLoginException("concurrent login for user: " + userId);

        logger.debug(
            "<login> lifecycle(user={},replacing={},source={})",
            userId,
            lifecycle.isReplacing(),
            lifecycle.getSource() != null ? lifecycle.getSource().getSessionId() : null);
        return lifecycle;
    }

    private Session
    newSession(
        LoginLifecycle lifecycle,
        Long           userId,
        String         deviceId,
        Set<String>    authorities,
        String         refreshToken,
        String         sessionHandle,
        SignInMethod   signInMethod)
    {
        Instant now     = Instant.now(clock);
        Session source  = lifecycle != null ? lifecycle.getSource() : null;
        Session session =
            new Session()
                .setUserId(userId)
                .setDeviceId(deviceId)
                .setExpires(now.plus(maxLifetime))
                .setAccessToken("")
                .setRefreshTokenHash(refreshTokenService.hash(refreshToken))
                .setHashedHandle(refreshTokenService.hash(sessionHandle))
                .setLastActivity(now)
                .setSignInMethod(signInMethod)
                .setAuthorities(
                    authorities != null
                        ? new LinkedHashSet<>(authorities)
                        : new LinkedHashSet<>())
                .setContinuityId(
                    lifecycle != null && lifecycle.isReplacing() && source.getContinuityId() != null
                        ? source.getContinuityId()
                        : UUID.randomUUID());

        if (source != null)
            session
                .setValues(source.getValues())
                .setCopiedFrom(source.getSessionId());

        session.setCreated(now);
        return session;
    }

    private void
    linkReplacedSource(LoginLifecycle lifecycle,Session created)
    {
        if (lifecycle == null || !lifecycle.isReplacing())
            return;

        Session source = lifecycle.getSource();

        source.setReplacedBy(created.getSessionId());
        repository.save(source);
    }

    private List<Session>
    liveSessions(List<Session> sessions,Instant now)
    {
        return
            sessions
                .stream()
                .filter(session -> !session.isClosed())
                .filter(session -> isLive(session,now))
                .collect(Collectors.toList());
    }

    private Session
    latest(List<Session> sessions)
    {
        return
            sessions
                .stream()
                .max(
                    Comparator.comparing(
                        Session::getLastActivity,
                        Comparator.nullsFirst(Comparator.<Instant>naturalOrder())))
                .orElse(null);
    }

    private void
    closeSession(Session session,SessionClosedReason reason,Instant now)
    {
        session
            .attach(this)
            .close(reason,now);
        repository.save(session);
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

    private UserData
    createOktaUser(OktaIdentity identity,Set<String> assigned)
    {
        CreateUserReply reply =
            userCommandServiceClient
                .createUser(
                    new CreateUserRequest()
                        .setUserToCreate(
                            new CreateUserData()
                                .setUserName(identity.getUserName())
                                .setExternalIdentity(OKTA_PROVIDER + ":" + identity.getSubject())
                                .setAuthorities(new LinkedHashSet<>(assigned))))
                .toCompletableFuture()
                .join();

        if (reply != null && reply.isSuccess() && reply.getCreatedUser() != null)
        {
            logger.info("<completeOktaLogin> user: {} created for an Okta identity",reply.getCreatedUser().getUserId());
            return reply.getCreatedUser();
        }

        logger.info("<completeOktaLogin> user creation failed; looking the identity up again");
        return findUserByExternalIdentity(OKTA_PROVIDER,identity.getSubject());
    }

    private Set<String>
    storedAuthorities(UserData user)
    {
        return
            user.getAuthorities() != null
                ? new LinkedHashSet<>(user.getAuthorities())
                : new LinkedHashSet<>();
    }

    private UserData
    writeBackRoles(UserData user,Set<String> assigned)
    {
        if (assigned.equals(storedAuthorities(user)))
            return user;

        UpdateUserReply reply =
            userCommandServiceClient
                .updateUser(
                    new UpdateUserRequest()
                        .setUserToUpdate(
                            new UpdateUserData()
                                .setUserId(user.getUserId())
                                .setAuthorities(new LinkedHashSet<>(assigned))))
                .toCompletableFuture()
                .join();

        if (reply == null || !reply.isSuccess())
        {
            logger.info("<completeOktaLogin> role write-back failed for user: {}",user.getUserId());
            return null;
        }

        logger.info("<completeOktaLogin> roles updated for user: {}",user.getUserId());
        return
            new UserData()
                .setUserId(user.getUserId())
                .setUserName(user.getUserName())
                .setExternalIdentity(user.getExternalIdentity())
                .setAuthorities(new LinkedHashSet<>(assigned));
    }

    private String
    endSessionUrlFor(Session session)
    {
        if (session.isClosed() || session.getSignInMethod() != SignInMethod.OKTA || isBlank(session.getOktaIdToken()))
            return null;

        try
        {
            return oktaClient.buildEndSessionUrl(session.getOktaIdToken()).orElse(null);
        }
        catch (RuntimeException e)
        {
            logger.warn("<logout> Okta end-session URL unavailable: {}",e.getClass().getSimpleName());
            return null;
        }
    }

    private String
    codeChallenge(String verifier)
    {
        try
        {
            return
                Base64
                    .getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(
                        MessageDigest
                            .getInstance("SHA-256")
                            .digest(verifier.getBytes(StandardCharsets.US_ASCII)));
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private String
    sanitizeReturnTo(String returnTo)
    {
        if (isBlank(returnTo)
            || returnTo.length() > 2048
            || !returnTo.startsWith("/")
            || returnTo.startsWith("//")
            || returnTo.startsWith("/\\"))
            return "/";

        for (char c : returnTo.toCharArray())
            if (Character.isISOControl(c))
                return "/";

        return returnTo;
    }

    private boolean
    matches(String expected,String actual)
    {
        if (expected == null || actual == null)
            return false;

        return
            MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }

    private boolean
    isBlank(String value)
    {
        return value == null || value.isBlank();
    }

    private StartOktaLoginReply
    buildStartOktaReply(StartOktaLoginContext context)
    {
        if (context.isRejected())
        {
            logger.info("<startOktaLogin> Okta login is disabled");
            return
                new StartOktaLoginReply(context.getRequest())
                    .setSuccess(false)
                    .setFailureMessage("Okta login is not available");
        }

        logger.info("<startOktaLogin> transaction: {} started",context.getTransaction().getPrimaryId());
        return
            new StartOktaLoginReply(context.getRequest())
                .setSuccess(true)
                .setSuccessMessage("Okta login started")
                .setAuthorizeUrl(context.getAuthorizeUrl())
                .setTransactionId(context.getTransactionId());
    }

    private StartOktaLoginReply
    buildStartOktaReply(Throwable e,StartOktaLoginRequest request)
    {
        logger.info("<startOktaLogin> exception reply: {}",rootCauseName(e));
        return
            new StartOktaLoginReply(request)
                .setSuccess(false)
                .setFailureMessage("Unable to start Okta login");
    }

    private CompleteOktaLoginReply
    buildCompleteOktaReply(CompleteOktaLoginContext context)
    {
        if (context.isRejected() || context.getCreated() == null)
        {
            logger.info("<completeOktaLogin> refused: {}",context.getFailureReason());
            return
                new CompleteOktaLoginReply(context.getRequest())
                    .setSuccess(false)
                    .setFailureMessage(INVALID_CREDENTIALS);
        }

        Session session = context.getCreated();

        logger.info("<completeOktaLogin> session: {} created",session.getSessionId());
        return
            new CompleteOktaLoginReply(context.getRequest())
                .setSuccess(true)
                .setSuccessMessage("Session: " + session.getSessionId() + " created")
                .setSessionData(mapper.map(session,SessionData.class))
                .setSessionHandle(context.getSessionHandle())
                .setUserName(context.getUserName())
                .setReturnTo(context.getTransaction().getReturnTo());
    }

    private CompleteOktaLoginReply
    buildCompleteOktaReply(Throwable e,CompleteOktaLoginRequest request)
    {
        if (conflictDetector.isConflict(e))
            throw new CompletionException(e);

        logger.info("<completeOktaLogin> exception reply: {}",rootCauseName(e));
        return
            new CompleteOktaLoginReply(request)
                .setSuccess(false)
                .setFailureMessage("Unable to log in");
    }

    private String
    rootCauseName(Throwable e)
    {
        Throwable cause = e;

        while (cause.getCause() != null && cause.getCause() != cause)
            cause = cause.getCause();

        return cause.getClass().getSimpleName();
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
                .setClosedReason(
                    domainEvent.getSource().getClosedReason() != null
                        ? domainEvent.getSource().getClosedReason().name()
                        : null)
                .build();
    }
}

//////////////////////////////////////////////////////////////////////////////
