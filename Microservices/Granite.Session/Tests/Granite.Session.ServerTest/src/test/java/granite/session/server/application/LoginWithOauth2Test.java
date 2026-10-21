//////////////////////////////////////////////////////////////////////////////
// LoginWithOauth2Test.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.platform.MockSessionEventSender;
import granite.session.server.domain.ISessionRepository;
import granite.session.server.domain.Session;
import granite.session.service.requestreply.LoginWithOauth2Reply;
import granite.session.service.requestreply.LoginWithOauth2Request;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.CreateUserReply;
import granite.user.service.requestreply.CreateUserRequest;
import granite.user.service.requestreply.FindUsersByExternalIdentityReply;
import granite.user.service.requestreply.FindUsersByExternalIdentityRequest;
import granite.user.service.requestreply.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.modelmapper.ModelMapper;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class LoginWithOauth2Test
{
    private static final Long     EXISTING_USER_ID = 101L;
    private static final Long     PROVISIONED_ID   = 202L;
    private static final String   PROVIDER         = "google";
    private static final String   SUB              = "subject-42";
    private static final String   ID_TOKEN         = "header.payload.signature";
    private static final String   DEVICE_ID        = "device-A";
    private static final Duration TTL              = Duration.ofMinutes(15);
    private static final String   MINTED_JWT       = "minted.jwt.token";
    private static final Instant  NOW              = Instant.parse("2026-10-06T12:00:00Z");

    private ISessionRepository        repository;
    private IJwtTokenService          jwtTokenService;
    private IUserCommandServiceClient userCommandServiceClient;
    private IUserQueryServiceClient   userQueryServiceClient;
    private IOauth2IdTokenValidator   oauth2Validator;
    private MockSessionEventSender    sender;
    private Clock                     clock;
    private SessionCommandService     subject;

    @BeforeEach
    public void
    setUp()
    {
        repository               = Mockito.mock(ISessionRepository.class);
        jwtTokenService          = Mockito.mock(IJwtTokenService.class);
        userCommandServiceClient = Mockito.mock(IUserCommandServiceClient.class);
        userQueryServiceClient   = Mockito.mock(IUserQueryServiceClient.class);
        oauth2Validator          = Mockito.mock(IOauth2IdTokenValidator.class);
        sender                   = new MockSessionEventSender();
        clock                    = Clock.fixed(NOW,ZoneOffset.UTC);
        subject =
            new SessionCommandService(
                repository,
                jwtTokenService,
                new RefreshTokenService(),
                userCommandServiceClient,
                userQueryServiceClient,
                oauth2Validator,
                new RequiredAuthoritiesLoginGate(List.of()),
                sender,
                new ModelMapper().registerModule(new MappingModule()),
                TTL,
                Duration.ofHours(24),
                clock);

        stubSaveAssignsSessionId(600L);
        Mockito.when(jwtTokenService.mint(Mockito.any(Session.class))).thenReturn(MINTED_JWT);
    }

    @Test
    public void
    loginWithValidIdTokenForKnownUserMintsSession()
    {
        stubValidate(PROVIDER,ID_TOKEN,validIdentity());
        stubFindByExternalIdentity(PROVIDER,SUB,userData(EXISTING_USER_ID));

        LoginWithOauth2Reply reply = loginWithOauth2(PROVIDER,ID_TOKEN,DEVICE_ID);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(MINTED_JWT,reply.getAccessToken());
        assertNotNull(reply.getRefreshToken());
        assertNotNull(reply.getSessionData());
        assertEquals(EXISTING_USER_ID,reply.getSessionData().getUserId());
        assertEquals(DEVICE_ID,reply.getSessionData().getDeviceId());
        assertEquals(NOW.plus(TTL),reply.getSessionData().getExpires());
        Mockito
            .verify(userCommandServiceClient,Mockito.never())
            .createUser(Mockito.any(CreateUserRequest.class));
    }

    @Test
    public void
    loginWithValidIdTokenForUnknownUserProvisionsThenMints()
    {
        stubValidate(PROVIDER,ID_TOKEN,validIdentity());
        stubFindByExternalIdentity(PROVIDER,SUB,null);
        stubCreateUserReturns(PROVISIONED_ID);

        LoginWithOauth2Reply reply = loginWithOauth2(PROVIDER,ID_TOKEN,DEVICE_ID);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(MINTED_JWT,reply.getAccessToken());
        assertNotNull(reply.getSessionData());
        assertEquals(PROVISIONED_ID,reply.getSessionData().getUserId());
        Mockito
            .verify(userCommandServiceClient,Mockito.times(1))
            .createUser(Mockito.argThat(
                req ->
                    req != null
                        && req.getUserToCreate() != null
                        && (PROVIDER + ":" + SUB).equals(req.getUserToCreate().getExternalIdentity())
                        && SUB.equals(req.getUserToCreate().getUserName())));
    }

    @Test
    public void
    loginWithWrongAudienceIdTokenReturnsSuccessFalse()
    {
        Mockito
            .when(oauth2Validator.validate(PROVIDER,ID_TOKEN))
            .thenThrow(new Oauth2ValidationException("bad audience: [other-client-id]"));

        LoginWithOauth2Reply reply = loginWithOauth2(PROVIDER,ID_TOKEN,DEVICE_ID);

        assertFalse(reply.isSuccess());
        assertNotNull(reply.getFailureMessage());
        assertTrue(
            reply.getFailureMessage().contains("bad audience"),
            "expected failure message to mention 'bad audience' but was: " + reply.getFailureMessage());
        assertNull(reply.getAccessToken());
        assertNull(reply.getSessionData());
        Mockito
            .verify(userQueryServiceClient,Mockito.never())
            .findUsersByExternalIdentity(Mockito.any(FindUsersByExternalIdentityRequest.class));
    }

    @Test
    public void
    loginWithUnknownProviderReturnsSuccessFalse()
    {
        Mockito
            .when(oauth2Validator.validate("unknown",ID_TOKEN))
            .thenThrow(new Oauth2ValidationException("unknown provider: unknown"));

        LoginWithOauth2Reply reply = loginWithOauth2("unknown",ID_TOKEN,DEVICE_ID);

        assertFalse(reply.isSuccess());
        assertNotNull(reply.getFailureMessage());
        assertTrue(
            reply.getFailureMessage().contains("unknown provider"),
            "expected failure message to mention 'unknown provider' but was: " + reply.getFailureMessage());
        assertNull(reply.getAccessToken());
        assertNull(reply.getSessionData());
    }

    @Test
    public void
    jitProvisioningRaceResolvesToSameUser()
    {
        stubValidate(PROVIDER,ID_TOKEN,validIdentity());

        AtomicBoolean recovery = new AtomicBoolean(false);

        Mockito
            .when(
                userQueryServiceClient.findUsersByExternalIdentity(
                    Mockito.argThat(
                        req ->
                            req != null
                                && PROVIDER.equals(req.getProvider())
                                && SUB.equals(req.getExternalId()))))
            .thenAnswer(
                (Answer<CompletableFuture<FindUsersByExternalIdentityReply>>)invocation ->
                {
                    FindUsersByExternalIdentityRequest req = invocation.getArgument(0);

                    if (recovery.get())
                        return
                            CompletableFuture.completedFuture(
                                new FindUsersByExternalIdentityReply(req)
                                    .setSuccess(true)
                                    .setUsers(List.of(userData(PROVISIONED_ID))));

                    return
                        CompletableFuture.completedFuture(
                            new FindUsersByExternalIdentityReply(req)
                                .setSuccess(true)
                                .setUsers(Collections.emptyList()));
                });

        Mockito
            .when(userCommandServiceClient.createUser(Mockito.any(CreateUserRequest.class)))
            .thenAnswer(
                (Answer<CompletableFuture<CreateUserReply>>)invocation ->
                {
                    recovery.set(true);
                    throw new RuntimeException(
                        "could not execute statement: unique constraint violation on externalIdentity");
                });

        LoginWithOauth2Reply reply = loginWithOauth2(PROVIDER,ID_TOKEN,DEVICE_ID);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertNotNull(reply.getSessionData());
        assertEquals(PROVISIONED_ID,reply.getSessionData().getUserId());
        Mockito
            .verify(userQueryServiceClient,Mockito.times(2))
            .findUsersByExternalIdentity(Mockito.any(FindUsersByExternalIdentityRequest.class));
    }

    @Test
    public void
    jitProvisioningRaceWithRecoveryFailurePropagatesOriginalException()
    {
        stubValidate(PROVIDER,ID_TOKEN,validIdentity());

        RuntimeException original =
            new RuntimeException("unique constraint violation on externalIdentity");
        RuntimeException recoveryFailure =
            new RuntimeException("database connection lost");

        Mockito
            .when(userQueryServiceClient.findUsersByExternalIdentity(Mockito.any(FindUsersByExternalIdentityRequest.class)))
            .thenAnswer(
                invocation ->
                {
                    FindUsersByExternalIdentityRequest req = invocation.getArgument(0);

                    return
                        CompletableFuture.completedFuture(
                            new FindUsersByExternalIdentityReply(req)
                                .setSuccess(true)
                                .setUsers(Collections.emptyList()));
                })
            .thenThrow(recoveryFailure);

        Mockito
            .when(userCommandServiceClient.createUser(Mockito.any(CreateUserRequest.class)))
            .thenThrow(original);

        LoginWithOauth2Reply reply = loginWithOauth2(PROVIDER,ID_TOKEN,DEVICE_ID);

        assertFalse(reply.isSuccess());
        assertNotNull(reply.getException());
        assertTrue(
            String.valueOf(reply.getException().getExceptionMessage()).contains("unique constraint violation on externalIdentity"),
            "expected original message but was: " + reply.getException().getExceptionMessage());
        assertFalse(String.valueOf(reply.getException().getExceptionMessage()).contains("database connection lost"));
        assertEquals(1,original.getSuppressed().length);
        assertTrue(original.getSuppressed()[0] == recoveryFailure);
    }

    @Test
    public void
    jitProvisioningRaceSoftFailureResolvesToSameUser()
    {
        stubValidate(PROVIDER,ID_TOKEN,validIdentity());

        Mockito
            .when(userQueryServiceClient.findUsersByExternalIdentity(Mockito.any(FindUsersByExternalIdentityRequest.class)))
            .thenAnswer(
                invocation ->
                {
                    FindUsersByExternalIdentityRequest req = invocation.getArgument(0);

                    return
                        CompletableFuture.completedFuture(
                            new FindUsersByExternalIdentityReply(req)
                                .setSuccess(true)
                                .setUsers(Collections.emptyList()));
                })
            .thenAnswer(
                invocation ->
                {
                    FindUsersByExternalIdentityRequest req = invocation.getArgument(0);

                    return
                        CompletableFuture.completedFuture(
                            new FindUsersByExternalIdentityReply(req)
                                .setSuccess(true)
                                .setUsers(List.of(userData(42L))));
                });

        Mockito
            .when(userCommandServiceClient.createUser(Mockito.any(CreateUserRequest.class)))
            .thenAnswer(
                invocation ->
                {
                    CreateUserRequest req = invocation.getArgument(0);

                    return
                        CompletableFuture.completedFuture(
                            new CreateUserReply(req)
                                .setSuccess(false)
                                .setFailureMessage("user already exists"));
                });

        LoginWithOauth2Reply reply = loginWithOauth2(PROVIDER,ID_TOKEN,DEVICE_ID);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(42L,reply.getSessionData().getUserId());
        Mockito
            .verify(userQueryServiceClient,Mockito.times(2))
            .findUsersByExternalIdentity(Mockito.any(FindUsersByExternalIdentityRequest.class));
    }

    @Test
    public void
    loginForKnownUserWithoutPermittedAuthorityIsRefused()
    {
        stubValidate(PROVIDER,ID_TOKEN,validIdentity());
        stubFindByExternalIdentity(
            PROVIDER,SUB,userData(EXISTING_USER_ID).setAuthorities(Set.of("ROLE_USER")));

        LoginWithOauth2Reply reply = loginWithOauth2(gatedSubject(),PROVIDER,ID_TOKEN,DEVICE_ID);

        assertFalse(reply.isSuccess());
        assertEquals("invalid credentials",reply.getFailureMessage());
        assertNull(reply.getAccessToken());
        assertNull(reply.getSessionData());
        Mockito.verify(repository,Mockito.never()).save(Mockito.any(Session.class));
    }

    @Test
    public void
    loginForKnownAdminSucceeds()
    {
        stubValidate(PROVIDER,ID_TOKEN,validIdentity());
        stubFindByExternalIdentity(
            PROVIDER,SUB,userData(EXISTING_USER_ID).setAuthorities(Set.of("ROLE_ADMIN")));

        LoginWithOauth2Reply reply = loginWithOauth2(gatedSubject(),PROVIDER,ID_TOKEN,DEVICE_ID);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
    }

    private SessionCommandService
    gatedSubject()
    {
        return
            new SessionCommandService(
                repository,
                jwtTokenService,
                new RefreshTokenService(),
                userCommandServiceClient,
                userQueryServiceClient,
                oauth2Validator,
                new RequiredAuthoritiesLoginGate(List.of("ROLE_ADMIN","ROLE_AUDITOR")),
                sender,
                new ModelMapper().registerModule(new MappingModule()),
                TTL,
                Duration.ofHours(24),
                clock);
    }

    private LoginWithOauth2Reply
    loginWithOauth2(SessionCommandService service,String provider,String idToken,String deviceId)
    {
        return
            service
                .loginWithOauth2(
                    new LoginWithOauth2Request()
                        .setProvider(provider)
                        .setIdToken(idToken)
                        .setDeviceId(deviceId))
                .toCompletableFuture()
                .join();
    }

    private LoginWithOauth2Reply
    loginWithOauth2(String provider,String idToken,String deviceId)
    {
        return
            subject
                .loginWithOauth2(
                    new LoginWithOauth2Request()
                        .setProvider(provider)
                        .setIdToken(idToken)
                        .setDeviceId(deviceId))
                .toCompletableFuture()
                .join();
    }

    private Oauth2ValidatedIdentity
    validIdentity()
    {
        return Oauth2ValidatedIdentity.of(PROVIDER,SUB,Optional.of("user@example.com"));
    }

    private UserData
    userData(Long id)
    {
        return
            new UserData()
                .setUserId(id)
                .setUserName(SUB)
                .setExternalIdentity(PROVIDER + ":" + SUB);
    }

    private void
    stubValidate(String provider,String idToken,Oauth2ValidatedIdentity identity)
    {
        Mockito
            .when(oauth2Validator.validate(provider,idToken))
            .thenReturn(identity);
    }

    private void
    stubFindByExternalIdentity(String provider,String sub,UserData user)
    {
        Mockito
            .when(
                userQueryServiceClient.findUsersByExternalIdentity(
                    Mockito.argThat(
                        req ->
                            req != null
                                && provider.equals(req.getProvider())
                                && sub.equals(req.getExternalId()))))
            .thenAnswer(invocation ->
            {
                FindUsersByExternalIdentityRequest req = invocation.getArgument(0);
                FindUsersByExternalIdentityReply   reply =
                    new FindUsersByExternalIdentityReply(req).setSuccess(true);

                reply.setUsers(user == null ? Collections.emptyList() : List.of(user));
                return CompletableFuture.completedFuture(reply);
            });
    }

    private void
    stubCreateUserReturns(Long newUserId)
    {
        Mockito
            .when(userCommandServiceClient.createUser(Mockito.any(CreateUserRequest.class)))
            .thenAnswer(invocation ->
            {
                CreateUserRequest req     = invocation.getArgument(0);
                UserData          created =
                    new UserData()
                        .setUserId(newUserId)
                        .setUserName(req.getUserToCreate().getUserName())
                        .setExternalIdentity(req.getUserToCreate().getExternalIdentity());

                return
                    CompletableFuture.completedFuture(
                        new CreateUserReply(req)
                            .setSuccess(true)
                            .setCreatedUser(created));
            });
    }

    private void
    stubSaveAssignsSessionId(long startingId)
    {
        AtomicLong next = new AtomicLong(startingId);

        Mockito
            .when(repository.save(Mockito.any(Session.class)))
            .thenAnswer(invocation ->
            {
                Session s = invocation.getArgument(0);

                if (s.getSessionId() == null)
                    s.setSessionId(next.getAndIncrement());

                return s;
            });
    }
}

//////////////////////////////////////////////////////////////////////////////
