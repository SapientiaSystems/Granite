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
import granite.user.service.requestreply.CreateUserRequest;
import granite.user.service.requestreply.FindUsersByExternalIdentityReply;
import granite.user.service.requestreply.FindUsersByExternalIdentityRequest;
import granite.user.service.requestreply.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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
                Duration.ofHours(24),
                Duration.ofMinutes(2),
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
        assertEquals(NOW.plus(Duration.ofHours(24)),reply.getSessionData().getExpires());
        Mockito
            .verify(userCommandServiceClient,Mockito.never())
            .createUser(Mockito.any(CreateUserRequest.class));
    }

    @Test
    public void
    loginWithOauth2IssuesSessionHandleAndStoresOnlyItsHash()
    {
        stubValidate(PROVIDER,ID_TOKEN,validIdentity());
        stubFindByExternalIdentity(PROVIDER,SUB,userData(EXISTING_USER_ID));

        LoginWithOauth2Reply reply = loginWithOauth2(PROVIDER,ID_TOKEN,DEVICE_ID);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertNotNull(reply.getSessionHandle());
        assertTrue(reply.getSessionHandle().matches("[A-Za-z0-9_-]{43}"));

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);

        Mockito.verify(repository,Mockito.atLeastOnce()).save(captor.capture());
        assertEquals(
            new RefreshTokenService().hash(reply.getSessionHandle()),
            captor.getValue().getHashedHandle());
        assertEquals(NOW,captor.getValue().getLastActivity());
    }

    @Test
    public void
    loginForUnknownIdentityIsRefusedWithoutProvisioning()
    {
        stubValidate(PROVIDER,ID_TOKEN,validIdentity());
        stubFindByExternalIdentity(PROVIDER,SUB,null);

        LoginWithOauth2Reply reply = loginWithOauth2(PROVIDER,ID_TOKEN,DEVICE_ID);

        assertFalse(reply.isSuccess());
        assertEquals("invalid credentials",reply.getFailureMessage());
        assertNull(reply.getAccessToken());
        assertNull(reply.getSessionData());
        Mockito
            .verify(userCommandServiceClient,Mockito.never())
            .createUser(Mockito.any(CreateUserRequest.class));
        Mockito.verify(repository,Mockito.never()).save(Mockito.any(Session.class));
        Mockito.verify(jwtTokenService,Mockito.never()).mint(Mockito.any(Session.class));
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

    @Test
    public void
    oauth2LoginReplacesExistingSession()
    {
        java.util.UUID cid = java.util.UUID.randomUUID();
        Session        old =
            new Session()
                .setSessionId(450L)
                .setUserId(EXISTING_USER_ID)
                .setDeviceId(DEVICE_ID)
                .setExpires(NOW.plus(Duration.ofHours(1)))
                .setAccessToken("old.jwt")
                .setContinuityId(cid)
                .setAuthorities(Set.of("ROLE_ADMIN"))
                .setValue("k","v");

        old.setCreated(NOW.minus(Duration.ofHours(1)));
        Mockito.when(repository.findOpenByDeviceId(DEVICE_ID)).thenReturn(List.of(old));
        stubValidate(PROVIDER,ID_TOKEN,validIdentity());
        stubFindByExternalIdentity(PROVIDER,SUB,userData(EXISTING_USER_ID).setAuthorities(Set.of("ROLE_USER")));

        LoginWithOauth2Reply reply = loginWithOauth2(PROVIDER,ID_TOKEN,DEVICE_ID);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(cid,reply.getSessionData().getContinuityId());
        assertEquals(java.util.Map.of("k","v"),reply.getSessionData().getValues());
        assertEquals(Set.of("ROLE_USER"),reply.getSessionData().getAuthorities());
        assertEquals(
            granite.session.service.requestreply.SessionClosedReason.REPLACED,
            old.getClosedReason());
        assertEquals(reply.getSessionData().getSessionId(),old.getReplacedBy());
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
                Duration.ofHours(24),
                Duration.ofMinutes(2),
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
