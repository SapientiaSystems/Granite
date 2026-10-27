//////////////////////////////////////////////////////////////////////////////
// CompleteOktaLoginTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.nimbusds.jose.jwk.RSAKey;
import granite.session.server.domain.IOktaLoginTransactionRepository;
import granite.session.server.domain.ISessionRepository;
import granite.session.server.domain.OktaLoginTransaction;
import granite.session.server.domain.Session;
import granite.session.server.domain.SignInMethod;
import granite.session.server.platform.MockSessionEventSender;
import granite.session.service.requestreply.CompleteOktaLoginReply;
import granite.session.service.requestreply.CompleteOktaLoginRequest;
import granite.session.service.requestreply.ISessionCommandService;
import granite.session.service.requestreply.SessionClosedReason;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.CreateUserReply;
import granite.user.service.requestreply.CreateUserRequest;
import granite.user.service.requestreply.FindUsersByExternalIdentityReply;
import granite.user.service.requestreply.FindUsersByExternalIdentityRequest;
import granite.user.service.requestreply.UserData;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class CompleteOktaLoginTest
{
    private static final String  DEVICE_ID      = "device-A";
    private static final String  TRANSACTION_ID = "raw-transaction-id-0123456789abcdef";
    private static final String  STATE          = "state-0123456789abcdef";
    private static final String  NONCE          = "nonce-0123456789abcdef";
    private static final String  VERIFIER       = "verifier-0123456789abcdef0123456789abcdef01234";
    private static final String  CODE           = "okta-authorization-code-0123456789";
    private static final String  SUB            = "00u1okta";
    private static final Long    USER_ID        = 42L;
    private static final Instant NOW            = Instant.parse("2026-10-24T12:00:00Z");
    private static final String  INVALID        = "invalid credentials";

    private FakeOkta                        okta;
    private OktaSettings                    settings;
    private ISessionRepository              repository;
    private IOktaLoginTransactionRepository transactions;
    private IUserCommandServiceClient       userCommands;
    private IUserQueryServiceClient         userQueries;
    private RefreshTokenService             tokens;
    private ILoginGate                      gate;
    private OktaLoginTransaction            transaction;
    private List<Session>                   savedSessions;

    @BeforeEach
    public void
    setUp()
    {
        okta          = new FakeOkta();
        settings      = okta.settings().setGroupMapping(Map.of("Granite Admins","ROLE_Admin"));
        repository    = Mockito.mock(ISessionRepository.class);
        transactions  = Mockito.mock(IOktaLoginTransactionRepository.class);
        userCommands  = Mockito.mock(IUserCommandServiceClient.class);
        userQueries   = Mockito.mock(IUserQueryServiceClient.class);
        tokens        = new RefreshTokenService();
        gate          = new RequiredAuthoritiesLoginGate(List.of());
        savedSessions = new ArrayList<>();
        transaction   =
            new OktaLoginTransaction()
                .setHashedTransactionId(tokens.hash(TRANSACTION_ID))
                .setState(STATE)
                .setNonce(NONCE)
                .setCodeVerifier(VERIFIER)
                .setReturnTo("/users")
                .setDeviceId(DEVICE_ID)
                .setExpires(NOW.plus(Duration.ofMinutes(5)));

        Mockito
            .when(transactions.findByHashedTransactionId(tokens.hash(TRANSACTION_ID)))
            .thenReturn(Optional.of(transaction));
        Mockito.when(repository.findOpenByDeviceId(Mockito.anyString())).thenReturn(List.of());
        Mockito.when(repository.findOpenByUserId(Mockito.anyLong())).thenReturn(List.of());
        stubSessionSave();
        stubExistingUser(Set.of("ROLE_User","ROLE_Auditor"));
        okta.stubTokenReturning(okta.idToken(SUB,NONCE));
    }

    @AfterEach
    public void
    tearDown()
    {
        okta.stop();
    }

    @Test
    public void
    validCallbackCreatesAnOktaSession()
    {
        CompleteOktaLoginReply reply   = complete(request());
        Session                created = lastSaved();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertNotNull(reply.getSessionHandle());
        assertEquals(tokens.hash(reply.getSessionHandle()),created.getHashedHandle());
        assertEquals("/users",reply.getReturnTo());
        assertEquals("alice",reply.getUserName());
        assertEquals(USER_ID,reply.getSessionData().getUserId());
        assertEquals(DEVICE_ID,reply.getSessionData().getDeviceId());
        assertEquals(SignInMethod.OKTA,created.getSignInMethod());
        assertNotNull(created.getOktaIdToken());
        assertEquals(Set.of("Granite Admins","Everyone"),created.getOktaGroups());
        assertEquals("minted.jwt",created.getAccessToken());
        Mockito.verify(transactions).delete(transaction);

        Map<String,String> form = okta.tokenRequests().get(0);

        assertEquals(CODE,form.get("code"));
        assertEquals(VERIFIER,form.get("code_verifier"));
    }

    @Test
    public void
    firstLoginWithoutMappedGroupsCreatesUserWithDefaultRole()
    {
        settings.setGroupMapping(Map.of());
        stubNoUser();

        CompleteOktaLoginReply reply = complete(request());

        ArgumentCaptor<CreateUserRequest> captor = ArgumentCaptor.forClass(CreateUserRequest.class);

        Mockito.verify(userCommands).createUser(captor.capture());
        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(Set.of("ROLE_User"),captor.getValue().getUserToCreate().getAuthorities());
        assertEquals("okta:" + SUB,captor.getValue().getUserToCreate().getExternalIdentity());
        assertEquals(SUB + "@example.com",captor.getValue().getUserToCreate().getUserName());
        assertNull(captor.getValue().getUserToCreate().getPassword());
        assertEquals(Set.of("ROLE_User"),reply.getSessionData().getAuthorities());
    }

    @Test
    public void
    existingUserIsNotCreatedAgain()
    {
        ArgumentCaptor<FindUsersByExternalIdentityRequest> captor =
            ArgumentCaptor.forClass(FindUsersByExternalIdentityRequest.class);

        assertTrue(complete(request()).isSuccess());
        Mockito.verify(userQueries).findUsersByExternalIdentity(captor.capture());
        Mockito.verify(userCommands,Mockito.never()).createUser(Mockito.any());
        assertEquals("okta",captor.getValue().getProvider());
        assertEquals(SUB,captor.getValue().getExternalId());
    }

    @Test
    public void
    storedRolesAreTheSessionAuthorities()
    {
        assertEquals(Set.of("ROLE_User","ROLE_Auditor"),complete(request()).getSessionData().getAuthorities());
    }

    @Test
    public void
    gateRefusesUngatedNewUser()
    {
        gate = new RequiredAuthoritiesLoginGate(List.of("ROLE_Admin","ROLE_Auditor"));
        settings.setGroupMapping(Map.of());
        stubNoUser();

        CompleteOktaLoginReply reply = complete(request());

        assertRejected(reply);
        Mockito.verify(userCommands).createUser(Mockito.any());
        assertTrue(savedSessions.isEmpty());
    }

    @Test
    public void
    gatePermitsAMappedGroupRole()
    {
        gate = new RequiredAuthoritiesLoginGate(List.of("ROLE_Admin"));
        stubNoUser();

        assertTrue(complete(request()).isSuccess());
    }

    @Test
    public void
    wrongStateIsRejected()
    {
        assertRejected(complete(request().setState("other-state")));
        assertEquals(0,okta.tokenRequests().size());
        Mockito.verify(transactions).delete(transaction);
    }

    @Test
    public void
    missingStateIsRejected()
    {
        assertRejected(complete(request().setState(null)));
        assertEquals(0,okta.tokenRequests().size());
    }

    @Test
    public void
    missingCodeIsRejected()
    {
        assertRejected(complete(request().setCode(" ")));
        assertEquals(0,okta.tokenRequests().size());
    }

    @Test
    public void
    unknownTransactionIsRejected()
    {
        assertRejected(complete(request().setTransactionId("unknown")));
        assertEquals(0,okta.tokenRequests().size());
        Mockito.verify(transactions,Mockito.never()).delete(Mockito.any());
    }

    @Test
    public void
    missingTransactionIsRejected()
    {
        assertRejected(complete(request().setTransactionId(null)));
        assertEquals(0,okta.tokenRequests().size());
    }

    @Test
    public void
    expiredTransactionIsRejectedAndDeleted()
    {
        transaction.setExpires(NOW.minusSeconds(1));

        assertRejected(complete(request()));
        assertEquals(0,okta.tokenRequests().size());
        Mockito.verify(transactions).delete(transaction);
    }

    @Test
    public void
    reusedTransactionIsRejected()
    {
        Mockito
            .when(transactions.findByHashedTransactionId(tokens.hash(TRANSACTION_ID)))
            .thenReturn(Optional.of(transaction),Optional.empty());

        assertTrue(complete(request()).isSuccess());
        assertRejected(complete(request()));
        assertEquals(1,okta.tokenRequests().size());
    }

    @Test
    public void
    otherDeviceIsRejected()
    {
        assertRejected(complete(request().setDeviceId("device-B")));
        assertEquals(0,okta.tokenRequests().size());
    }

    @Test
    public void
    wrongNonceIsRejected()
    {
        okta.stubTokenReturning(okta.idToken(SUB,"other-nonce"));
        assertRejected(complete(request()));
    }

    @Test
    public void
    missingNonceIsRejected()
    {
        okta.stubTokenReturning(okta.idToken(SUB,null));
        assertRejected(complete(request()));
    }

    @Test
    public void
    wrongAudienceIsRejected()
    {
        okta.stubTokenReturning(okta.idToken(SUB,NONCE,builder -> builder.audience("other-client")));
        assertRejected(complete(request()));
    }

    @Test
    public void
    wrongIssuerIsRejected()
    {
        okta.stubTokenReturning(okta.idToken(SUB,NONCE,builder -> builder.issuer("https://evil.example.com")));
        assertRejected(complete(request()));
    }

    @Test
    public void
    foreignSignatureIsRejected()
    {
        RSAKey foreign = new RSAKey.Builder(okta.generate()).keyID(okta.getSigningKey().getKeyID()).build();

        okta.stubTokenReturning(okta.sign(foreign,okta.claims(SUB,NONCE,builder -> {})));
        assertRejected(complete(request()));
    }

    @Test
    public void
    tokenEndpointErrorIsRejected()
    {
        okta.stubTokenError();
        assertRejected(complete(request()));
    }

    @Test
    public void
    disabledOktaIsRejected()
    {
        settings.setEnabled(false);

        assertRejected(complete(request()));
        Mockito.verifyNoInteractions(transactions);
    }

    @Test
    public void
    reloginOnTheSameDeviceReplacesAndKeepsContinuity()
    {
        UUID    cid = UUID.randomUUID();
        Session old = openSession(400L,USER_ID).setContinuityId(cid).setValue("cart","3");

        Mockito.when(repository.findOpenByDeviceId(DEVICE_ID)).thenReturn(List.of(old),List.of());

        CompleteOktaLoginReply reply = complete(request());

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(cid,reply.getSessionData().getContinuityId());
        assertEquals(Map.of("cart","3"),reply.getSessionData().getValues());
        assertEquals(400L,reply.getSessionData().getCopiedFrom());
        assertEquals(SessionClosedReason.REPLACED,old.getClosedReason());
    }

    @Test
    public void
    otherUserOnTheDeviceIsDisplaced()
    {
        Session other = openSession(401L,99L).setContinuityId(UUID.randomUUID()).setValue("secret","v");

        Mockito.when(repository.findOpenByDeviceId(DEVICE_ID)).thenReturn(List.of(other),List.of());

        CompleteOktaLoginReply reply = complete(request());

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(SessionClosedReason.DISPLACED,other.getClosedReason());
        assertNotEquals(other.getContinuityId(),reply.getSessionData().getContinuityId());
        assertTrue(reply.getSessionData().getValues().isEmpty());
        assertNull(reply.getSessionData().getCopiedFrom());
    }

    @Test
    public void
    retryAfterAConflictDoesNotExchangeTheCodeTwice()
    {
        AtomicBoolean failed = new AtomicBoolean(false);
        AtomicLong    next   = new AtomicLong(900L);

        Mockito
            .when(repository.save(Mockito.any(Session.class)))
            .thenAnswer(
                invocation ->
                {
                    Session session = invocation.getArgument(0);

                    if (failed.compareAndSet(false,true))
                        throw new OptimisticLockException("conflict");

                    if (session.getSessionId() == null)
                        session.setSessionId(next.getAndIncrement());

                    return session;
                });

        ISessionCommandService retrying = new ConcurrencyRetryingSessionCommandService(subject());

        CompleteOktaLoginReply reply = retrying.completeOktaLogin(request()).toCompletableFuture().join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(1,okta.tokenRequests().size());
    }

    @Test
    public void
    neverLogsSecrets()
    {
        Logger                      logger   = (Logger)LoggerFactory.getLogger("granite.session");
        Level                       previous = logger.getLevel();
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        String                      idToken  = okta.idToken(SUB,NONCE);
        CompleteOktaLoginReply      reply;

        okta.stubTokenReturning(idToken);
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.TRACE);

        try
        {
            reply = complete(request());
            complete(request().setState("other"));
        }
        finally
        {
            logger.detachAppender(appender);
            logger.setLevel(previous);
        }

        assertFalse(appender.list.isEmpty());

        for (ILoggingEvent event : appender.list)
        {
            String message = event.getFormattedMessage();

            for (String secret : new String[] {CODE,TRANSACTION_ID,VERIFIER,STATE,NONCE,idToken,reply.getSessionHandle()})
                assertFalse(message.contains(secret),"secret logged: " + message);
        }
    }

    private CompleteOktaLoginRequest
    request()
    {
        return
            new CompleteOktaLoginRequest()
                .setCode(CODE)
                .setState(STATE)
                .setTransactionId(TRANSACTION_ID)
                .setDeviceId(DEVICE_ID);
    }

    private CompleteOktaLoginReply
    complete(CompleteOktaLoginRequest request)
    {
        return subject().completeOktaLogin(request).toCompletableFuture().join();
    }

    private SessionCommandService
    subject()
    {
        IJwtTokenService jwt = Mockito.mock(IJwtTokenService.class);

        Mockito.when(jwt.mint(Mockito.any(Session.class))).thenReturn("minted.jwt");
        return
            new SessionCommandService(
                repository,
                transactions,
                jwt,
                tokens,
                userCommands,
                userQueries,
                okta.client(settings,Clock.systemUTC()),
                settings,
                gate,
                new MockSessionEventSender(),
                new ModelMapper().registerModule(new MappingModule()),
                Duration.ofHours(24),
                Duration.ofMinutes(2),
                Clock.fixed(NOW,ZoneOffset.UTC));
    }

    private void
    assertRejected(CompleteOktaLoginReply reply)
    {
        assertFalse(reply.isSuccess());
        assertEquals(INVALID,reply.getFailureMessage());
        assertNull(reply.getSessionHandle());
        assertNull(reply.getSessionData());
    }

    private void
    stubExistingUser(Set<String> authorities)
    {
        Mockito
            .when(userQueries.findUsersByExternalIdentity(Mockito.any(FindUsersByExternalIdentityRequest.class)))
            .thenAnswer(
                invocation ->
                    CompletableFuture.completedFuture(
                        new FindUsersByExternalIdentityReply(invocation.getArgument(0))
                            .setUsers(
                                List.of(
                                    new UserData()
                                        .setUserId(USER_ID)
                                        .setUserName("alice")
                                        .setExternalIdentity("okta:" + SUB)
                                        .setAuthorities(authorities)))
                            .setSuccess(true)));
    }

    private void
    stubNoUser()
    {
        Mockito
            .when(userQueries.findUsersByExternalIdentity(Mockito.any(FindUsersByExternalIdentityRequest.class)))
            .thenAnswer(
                invocation ->
                    CompletableFuture.completedFuture(
                        new FindUsersByExternalIdentityReply(invocation.getArgument(0))
                            .setUsers(List.of())
                            .setSuccess(true)));
        Mockito
            .when(userCommands.createUser(Mockito.any(CreateUserRequest.class)))
            .thenAnswer(
                invocation ->
                {
                    CreateUserRequest request = invocation.getArgument(0);

                    return
                        CompletableFuture.completedFuture(
                            new CreateUserReply(request)
                                .setSuccess(true)
                                .setCreatedUser(
                                    new UserData()
                                        .setUserId(USER_ID)
                                        .setUserName(request.getUserToCreate().getUserName())
                                        .setExternalIdentity(request.getUserToCreate().getExternalIdentity())
                                        .setAuthorities(request.getUserToCreate().getAuthorities())));
                });
    }

    private void
    stubSessionSave()
    {
        AtomicLong next = new AtomicLong(500L);

        Mockito
            .when(repository.save(Mockito.any(Session.class)))
            .thenAnswer(
                invocation ->
                {
                    Session session = invocation.getArgument(0);

                    if (session.getSessionId() == null)
                        session.setSessionId(next.getAndIncrement());

                    if (!savedSessions.contains(session))
                        savedSessions.add(session);

                    return session;
                });
    }

    private Session
    lastSaved()
    {
        return savedSessions.get(savedSessions.size() - 1);
    }

    private Session
    openSession(Long id,Long userId)
    {
        Session session =
            new Session()
                .setSessionId(id)
                .setUserId(userId)
                .setDeviceId(DEVICE_ID)
                .setExpires(NOW.plus(Duration.ofHours(6)))
                .setAccessToken("token-" + id)
                .setLastActivity(NOW.minusSeconds(60));

        session.setCreated(NOW.minus(Duration.ofHours(1)));
        return session;
    }
}

//////////////////////////////////////////////////////////////////////////////
