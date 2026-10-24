//////////////////////////////////////////////////////////////////////////////
// LoginTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.platform.MockSessionEventSender;
import granite.session.server.domain.ISessionRepository;
import granite.session.server.domain.Session;
import granite.session.service.event.SessionEvent;
import granite.session.service.requestreply.LoginReply;
import granite.session.service.requestreply.LoginRequest;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.AuthenticateUserReply;
import granite.user.service.requestreply.AuthenticateUserRequest;
import granite.user.service.requestreply.CredentialKind;
import granite.user.service.requestreply.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import strata.foundation.core.event.StandardEventType;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class LoginTest
{
    private static final Long     USER_ID    = 101L;
    private static final String   USER_NAME  = "alice@example.com";
    private static final String   PASSWORD   = "hunter2";
    private static final String   DEVICE_ID  = "device-A";
    private static final Duration TTL        = Duration.ofMinutes(15);
    private static final String   MINTED_JWT = "minted.jwt.token";
    private static final Instant  NOW        = Instant.parse("2026-10-06T12:00:00Z");

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

        stubSaveAssignsSessionId(500L);
        Mockito.when(jwtTokenService.mint(Mockito.any(Session.class))).thenReturn(MINTED_JWT);
    }

    @Test
    public void
    loginWithValidPasswordMintsSession()
    {
        stubAuthenticate(USER_NAME,PASSWORD,CredentialKind.PASSWORD,true,USER_ID);

        LoginReply reply = login(USER_NAME,PASSWORD,CredentialKind.PASSWORD,DEVICE_ID);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(MINTED_JWT,reply.getAccessToken());
        assertNotNull(reply.getSessionData());
        assertEquals(USER_ID,reply.getSessionData().getUserId());
        assertEquals(DEVICE_ID,reply.getSessionData().getDeviceId());
        assertEquals(NOW.plus(TTL),reply.getSessionData().getExpires());
    }

    @Test
    public void
    loginIssuesRefreshTokenAndStoresOnlyItsHash()
    {
        stubAuthenticate(USER_NAME,PASSWORD,CredentialKind.PASSWORD,true,USER_ID);

        LoginReply reply = login(USER_NAME,PASSWORD,CredentialKind.PASSWORD,DEVICE_ID);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertNotNull(reply.getRefreshToken());
        assertFalse(reply.getRefreshToken().isBlank());

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);

        Mockito.verify(repository,Mockito.atLeastOnce()).save(captor.capture());

        Session saved = captor.getValue();

        assertEquals(
            new RefreshTokenService().hash(reply.getRefreshToken()),
            saved.getRefreshTokenHash());
        assertNotEquals(reply.getRefreshToken(),saved.getRefreshTokenHash());
        assertNull(saved.getPreviousRefreshTokenHash());
    }

    @Test
    public void
    loginIssuesSessionHandleAndStoresOnlyItsHash()
    {
        stubAuthenticate(USER_NAME,PASSWORD,CredentialKind.PASSWORD,true,USER_ID);

        LoginReply reply = login(USER_NAME,PASSWORD,CredentialKind.PASSWORD,DEVICE_ID);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertNotNull(reply.getSessionHandle());
        assertTrue(
            reply.getSessionHandle().matches("[A-Za-z0-9_-]{43}"),
            "handle should be 256-bit base64url without padding");
        assertNotEquals(reply.getRefreshToken(),reply.getSessionHandle());

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);

        Mockito.verify(repository,Mockito.atLeastOnce()).save(captor.capture());

        Session saved = captor.getValue();

        assertEquals(
            new RefreshTokenService().hash(reply.getSessionHandle()),
            saved.getHashedHandle());
        assertNotEquals(reply.getSessionHandle(),saved.getHashedHandle());
        assertNull(saved.getClosedReason());
    }

    @Test
    public void
    loginSetsLastActivity()
    {
        stubAuthenticate(USER_NAME,PASSWORD,CredentialKind.PASSWORD,true,USER_ID);

        LoginReply reply = login(USER_NAME,PASSWORD,CredentialKind.PASSWORD,DEVICE_ID);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);

        Mockito.verify(repository,Mockito.atLeastOnce()).save(captor.capture());
        assertEquals(NOW,captor.getValue().getLastActivity());
    }

    @Test
    public void
    loginWithWrongPasswordReturnsSuccessFalse()
    {
        stubAuthenticate(USER_NAME,"wrong",CredentialKind.PASSWORD,false,null);

        LoginReply reply = login(USER_NAME,"wrong",CredentialKind.PASSWORD,DEVICE_ID);

        assertFalse(reply.isSuccess());
        assertEquals("invalid credentials",reply.getFailureMessage());
        assertNull(reply.getAccessToken());
        assertNull(reply.getRefreshToken());
        assertNull(reply.getSessionData());
    }

    @Test
    public void
    loginWithValidPasswordFlowsAuthoritiesThroughToSession()
    {
        Set<String> authorities = Set.of("ROLE_USER","ROLE_ADMIN");

        stubAuthenticate(USER_NAME,PASSWORD,CredentialKind.PASSWORD,true,USER_ID,authorities);

        LoginReply              reply   = login(USER_NAME,PASSWORD,CredentialKind.PASSWORD,DEVICE_ID);
        ArgumentCaptor<Session> captor  = ArgumentCaptor.forClass(Session.class);

        Mockito.verify(jwtTokenService).mint(captor.capture());

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(authorities,captor.getValue().getAuthorities());
        assertEquals(authorities,reply.getSessionData().getAuthorities());
    }

    @Test
    public void
    loginWithNullCredentialFailsValidation()
    {
        LoginReply reply =
            subject
                .login(
                    new LoginRequest()
                        .setUserName(USER_NAME)
                        .setCredential(null)
                        .setCredentialKind(CredentialKind.PASSWORD)
                        .setDeviceId(DEVICE_ID))
                .toCompletableFuture()
                .join();

        assertFalse(reply.isSuccess());
        assertEquals("Unable to log in",reply.getFailureMessage());
        assertNotNull(reply.getException());
    }

    @Test
    public void
    loginWithBlankCredentialFailsValidation()
    {
        LoginReply reply =
            subject
                .login(
                    new LoginRequest()
                        .setUserName(USER_NAME)
                        .setCredential("   ")
                        .setCredentialKind(CredentialKind.PASSWORD)
                        .setDeviceId(DEVICE_ID))
                .toCompletableFuture()
                .join();

        assertFalse(reply.isSuccess());
        assertEquals("Unable to log in",reply.getFailureMessage());
        assertNotNull(reply.getException());
    }

    @Test
    public void
    loginWithNullCredentialKindFailsValidation()
    {
        LoginReply reply =
            subject
                .login(
                    new LoginRequest()
                        .setUserName(USER_NAME)
                        .setCredential(PASSWORD)
                        .setCredentialKind(null)
                        .setDeviceId(DEVICE_ID))
                .toCompletableFuture()
                .join();

        assertFalse(reply.isSuccess());
        assertEquals("Unable to log in",reply.getFailureMessage());
        assertNotNull(reply.getException());
    }

    @Test
    public void
    loginWithAccessTokenMintsNewSessionForSameUser()
    {
        String  existingToken = "existing.jwt.token";
        Session existing      =
            new Session()
                .setSessionId(400L)
                .setUserId(USER_ID)
                .setDeviceId(DEVICE_ID)
                .setExpires(NOW.plus(Duration.ofMinutes(10)))
                .setAccessToken(existingToken);

        Mockito
            .when(repository.findByAccessToken(existingToken))
            .thenReturn(Optional.of(existing));

        LoginReply reply = login(USER_NAME,existingToken,CredentialKind.ACCESS_TOKEN,DEVICE_ID);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(MINTED_JWT,reply.getAccessToken());
        assertNotNull(reply.getSessionData());
        assertEquals(USER_ID,reply.getSessionData().getUserId());
        assertEquals(500L,reply.getSessionData().getSessionId());
    }

    @Test
    public void
    loginWithAccessTokenDoesNotDestroyOriginalSession()
    {
        String  existingToken = "existing.jwt.token";
        Session existing      =
            new Session()
                .setSessionId(400L)
                .setUserId(USER_ID)
                .setDeviceId(DEVICE_ID)
                .setExpires(NOW.plus(Duration.ofMinutes(10)))
                .setAccessToken(existingToken);

        Mockito
            .when(repository.findByAccessToken(existingToken))
            .thenReturn(Optional.of(existing));

        LoginReply reply = login(USER_NAME,existingToken,CredentialKind.ACCESS_TOKEN,DEVICE_ID);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        Mockito.verify(repository,Mockito.never()).delete(Mockito.any(Session.class));
    }

    @Test
    public void
    loginWithExpiredAccessTokenReturnsSuccessFalse()
    {
        String  expiredToken = "expired.jwt.token";
        Session expired      =
            new Session()
                .setSessionId(401L)
                .setUserId(USER_ID)
                .setDeviceId(DEVICE_ID)
                .setExpires(NOW.minus(Duration.ofMinutes(1)))
                .setAccessToken(expiredToken);

        Mockito
            .when(repository.findByAccessToken(expiredToken))
            .thenReturn(Optional.of(expired));

        LoginReply reply = login(USER_NAME,expiredToken,CredentialKind.ACCESS_TOKEN,DEVICE_ID);

        assertFalse(reply.isSuccess());
        assertEquals("invalid credentials",reply.getFailureMessage());
        assertNull(reply.getAccessToken());
    }

    @Test
    public void
    loginWithSecurityCodeReturnsUnsupportedMessage()
    {
        LoginReply reply = login(USER_NAME,"123456",CredentialKind.SECURITY_CODE,DEVICE_ID);

        assertFalse(reply.isSuccess());
        assertEquals("CredentialKind SECURITY_CODE is not supported",reply.getFailureMessage());
        assertNull(reply.getAccessToken());
    }

    @Test
    public void
    loginFiresSessionCreatedEventOnSuccess()
    {
        stubAuthenticate(USER_NAME,PASSWORD,CredentialKind.PASSWORD,true,USER_ID);

        LoginReply reply = login(USER_NAME,PASSWORD,CredentialKind.PASSWORD,DEVICE_ID);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());

        List<SessionEvent> events = sender.getEvents();

        assertEquals(1,events.size(),"expected exactly one SessionCreated event");
        assertEquals(StandardEventType.CREATED,events.get(0).getEventType());
        assertEquals(500L,events.get(0).getSourceId());
    }

    @Test
    public void
    loginWithoutPermittedAuthorityIsRefused()
    {
        SessionCommandService gated = gatedSubject();

        stubAuthenticate(USER_NAME,PASSWORD,CredentialKind.PASSWORD,true,USER_ID,Set.of("ROLE_USER"));

        LoginReply reply = login(gated,USER_NAME,PASSWORD,CredentialKind.PASSWORD,DEVICE_ID);

        assertRefused(reply);
        Mockito.verify(repository,Mockito.never()).save(Mockito.any(Session.class));
        Mockito.verify(jwtTokenService,Mockito.never()).mint(Mockito.any(Session.class));
    }

    @Test
    public void
    loginWithAuditorAuthoritySucceeds()
    {
        SessionCommandService gated = gatedSubject();

        stubAuthenticate(
            USER_NAME,PASSWORD,CredentialKind.PASSWORD,true,USER_ID,Set.of("ROLE_USER","ROLE_AUDITOR"));

        LoginReply reply = login(gated,USER_NAME,PASSWORD,CredentialKind.PASSWORD,DEVICE_ID);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
    }

    @Test
    public void
    loginWithNullAuthoritiesIsRefused()
    {
        SessionCommandService gated = gatedSubject();

        stubAuthenticate(USER_NAME,PASSWORD,CredentialKind.PASSWORD,true,USER_ID,null);

        LoginReply reply = login(gated,USER_NAME,PASSWORD,CredentialKind.PASSWORD,DEVICE_ID);

        assertRefused(reply);
        Mockito.verify(repository,Mockito.never()).save(Mockito.any(Session.class));
        Mockito.verify(jwtTokenService,Mockito.never()).mint(Mockito.any(Session.class));
    }

    @Test
    public void
    loginWithAccessTokenCarriesExistingSessionAuthorities()
    {
        String  existingToken = "existing.jwt.token";
        Session existing      =
            new Session()
                .setSessionId(400L)
                .setUserId(USER_ID)
                .setDeviceId(DEVICE_ID)
                .setExpires(NOW.plus(Duration.ofMinutes(10)))
                .setAccessToken(existingToken)
                .setAuthorities(Set.of("ROLE_ADMIN"));

        Mockito
            .when(repository.findByAccessToken(existingToken))
            .thenReturn(Optional.of(existing));

        LoginReply reply =
            login(gatedSubject(),USER_NAME,existingToken,CredentialKind.ACCESS_TOKEN,DEVICE_ID);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);

        Mockito.verify(repository,Mockito.atLeastOnce()).save(captor.capture());
        assertEquals(Set.of("ROLE_ADMIN"),captor.getValue().getAuthorities());
    }

    @Test
    public void
    loginWithAccessTokenWithoutPermittedAuthorityIsRefused()
    {
        String  existingToken = "existing.jwt.token";
        Session existing      =
            new Session()
                .setSessionId(400L)
                .setUserId(USER_ID)
                .setDeviceId(DEVICE_ID)
                .setExpires(NOW.plus(Duration.ofMinutes(10)))
                .setAccessToken(existingToken)
                .setAuthorities(Set.of("ROLE_USER"));

        Mockito
            .when(repository.findByAccessToken(existingToken))
            .thenReturn(Optional.of(existing));

        LoginReply reply =
            login(gatedSubject(),USER_NAME,existingToken,CredentialKind.ACCESS_TOKEN,DEVICE_ID);

        assertRefused(reply);
        Mockito.verify(repository,Mockito.never()).save(Mockito.any(Session.class));
        Mockito.verify(jwtTokenService,Mockito.never()).mint(Mockito.any(Session.class));
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

    private void
    assertRefused(LoginReply reply)
    {
        assertFalse(reply.isSuccess());
        assertEquals("invalid credentials",reply.getFailureMessage());
        assertNull(reply.getAccessToken());
        assertNull(reply.getRefreshToken());
        assertNull(reply.getSessionData());
    }

    private LoginReply
    login(SessionCommandService service,String userName,String credential,CredentialKind kind,String deviceId)
    {
        return
            service
                .login(
                    new LoginRequest()
                        .setUserName(userName)
                        .setCredential(credential)
                        .setCredentialKind(kind)
                        .setDeviceId(deviceId))
                .toCompletableFuture()
                .join();
    }

    private LoginReply
    login(String userName,String credential,CredentialKind kind,String deviceId)
    {
        return
            subject
                .login(
                    new LoginRequest()
                        .setUserName(userName)
                        .setCredential(credential)
                        .setCredentialKind(kind)
                        .setDeviceId(deviceId))
                .toCompletableFuture()
                .join();
    }

    private void
    stubAuthenticate(
        String         userName,
        String         credential,
        CredentialKind kind,
        boolean        valid,
        Long           userId)
    {
        stubAuthenticate(userName,credential,kind,valid,userId,Set.of());
    }

    private void
    stubAuthenticate(
        String         userName,
        String         credential,
        CredentialKind kind,
        boolean        valid,
        Long           userId,
        Set<String>    authorities)
    {
        Mockito
            .when(
                userCommandServiceClient.authenticateUser(
                    Mockito.argThat(
                        req ->
                            req != null
                                && userName.equals(req.getUserName())
                                && credential.equals(req.getCredential())
                                && kind.equals(req.getCredentialKind()))))
            .thenAnswer(invocation ->
            {
                UserData user =
                    userId != null
                        ? new UserData()
                              .setUserId(userId)
                              .setUserName(userName)
                              .setAuthorities(authorities)
                        : null;

                return
                    CompletableFuture.completedFuture(
                        new AuthenticateUserReply(
                            (AuthenticateUserRequest)invocation.getArgument(0))
                            .setSuccess(true)
                            .setValid(valid)
                            .setUser(user));
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
