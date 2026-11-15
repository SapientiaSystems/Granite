//////////////////////////////////////////////////////////////////////////////
// SessionApplicationBindingTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import com.nimbusds.jwt.SignedJWT;
import granite.foundation.core.security.ApplicationRolesManager;
import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.session.server.domain.IOktaLoginTransactionRepository;
import granite.session.server.domain.ISessionRepository;
import granite.session.server.domain.OktaLoginTransaction;
import granite.session.server.domain.Session;
import granite.session.server.platform.MockSessionEventSender;
import granite.session.service.requestreply.CompleteOktaLoginReply;
import granite.session.service.requestreply.CompleteOktaLoginRequest;
import granite.session.service.requestreply.LoginReply;
import granite.session.service.requestreply.LoginRequest;
import granite.session.service.requestreply.RefreshReply;
import granite.session.service.requestreply.RefreshRequest;
import granite.session.service.requestreply.StartOktaLoginReply;
import granite.session.service.requestreply.StartOktaLoginRequest;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.AuthenticateUserReply;
import granite.user.service.requestreply.AuthenticateUserRequest;
import granite.user.service.requestreply.CredentialKind;
import granite.user.service.requestreply.FindUsersByExternalIdentityReply;
import granite.user.service.requestreply.FindUsersByExternalIdentityRequest;
import granite.user.service.requestreply.FindUsersByKeyReply;
import granite.user.service.requestreply.FindUsersByKeyRequest;
import granite.user.service.requestreply.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class SessionApplicationBindingTest
{
    private static final Long    USER_ID         = 7L;
    private static final String  USER_NAME       = "carol@example.com";
    private static final String  PASSWORD        = "pw";
    private static final String  DEVICE_ID       = "device-A";
    private static final String  CLIENT_REDIRECT = "https://client.example.com/login/callback";
    private static final String  RAW_ID          = "raw-transaction-id-0123456789abcdef";
    private static final Instant NOW             = Instant.parse("2026-11-14T12:00:00Z");

    private FakeOkta                        okta;
    private OktaSettings                    settings;
    private ISessionRepository              repository;
    private IOktaLoginTransactionRepository transactions;
    private IUserCommandServiceClient       userCommands;
    private IUserQueryServiceClient         userQueries;
    private RefreshTokenService             tokens;
    private JwtTokenService                 jwt;
    private Set<String>                     authorities;
    private SessionCommandService           subject;

    @BeforeEach
    public void
    setUp()
        throws Exception
    {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);

        KeyPair pair = generator.generateKeyPair();

        okta         = new FakeOkta();
        settings     = okta.settings().setRedirectUris(Map.of("client",CLIENT_REDIRECT));
        repository   = Mockito.mock(ISessionRepository.class);
        transactions = Mockito.mock(IOktaLoginTransactionRepository.class);
        userCommands = Mockito.mock(IUserCommandServiceClient.class);
        userQueries  = Mockito.mock(IUserQueryServiceClient.class);
        tokens       = new RefreshTokenService();
        jwt          =
            new JwtTokenService(
                (RSAPrivateKey)pair.getPrivate(),
                (RSAPublicKey)pair.getPublic(),
                "granite-session",
                List.of("granite-services"),
                Duration.ofMinutes(15));
        authorities  = Set.of("ROLE_USER");

        AtomicLong next = new AtomicLong(900L);

        Mockito
            .when(repository.save(Mockito.any(Session.class)))
            .thenAnswer(
                invocation ->
                {
                    Session s = invocation.getArgument(0);

                    if (s.getSessionId() == null)
                        s.setSessionId(next.getAndIncrement());

                    return s;
                });
        Mockito
            .when(transactions.save(Mockito.any(OktaLoginTransaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(repository.findOpenByDeviceId(Mockito.anyString())).thenReturn(List.of());
        Mockito.when(repository.findOpenByUserId(Mockito.anyLong())).thenReturn(List.of());
        Mockito
            .when(userCommands.authenticateUser(Mockito.any(AuthenticateUserRequest.class)))
            .thenAnswer(
                invocation ->
                    CompletableFuture.completedFuture(
                        new AuthenticateUserReply((AuthenticateUserRequest)invocation.getArgument(0))
                            .setSuccess(true)
                            .setValid(true)
                            .setUser(
                                new UserData()
                                    .setUserId(USER_ID)
                                    .setUserName(USER_NAME)
                                    .setAuthorities(authorities)
                                    .setValues(Map.of("granite.rolesAssigned",true)))));
        Mockito
            .when(userQueries.findUsersByKey(Mockito.any(FindUsersByKeyRequest.class)))
            .thenAnswer(
                invocation ->
                    CompletableFuture.completedFuture(
                        new FindUsersByKeyReply(invocation.getArgument(0))
                            .setFoundUsers(
                                List.of(
                                    new UserData()
                                        .setUserId(USER_ID)
                                        .setUserName(USER_NAME)
                                        .setAuthorities(authorities)))
                            .setSuccess(true)));

        subject = newSubject();
    }

    @AfterEach
    public void
    tearDown()
    {
        okta.stop();
    }

    @Test
    public void
    unknownApplicationRefused()
    {
        LoginReply reply = login("other");

        assertFalse(reply.isSuccess());
        assertEquals("invalid application",reply.getFailureMessage());
        assertNull(reply.getAccessToken());
        Mockito.verify(userCommands,Mockito.never()).authenticateUser(Mockito.any());

        StartOktaLoginReply start = startOkta("other");

        assertFalse(start.isSuccess());
        assertEquals("invalid application",start.getFailureMessage());
        Mockito.verify(transactions,Mockito.never()).save(Mockito.any(OktaLoginTransaction.class));
    }

    @Test
    public void
    missingApplicationDefaultsToGranite()
        throws Exception
    {
        LoginReply reply = login(null);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals("granite",reply.getSessionData().getApplication());
        assertEquals("granite",SignedJWT.parse(reply.getAccessToken()).getJWTClaimsSet().getStringClaim("app"));
        assertEquals("granite",new Session().setApplication(null).getApplication());
        assertEquals("granite",new OktaLoginTransaction().getApplication());
    }

    @Test
    public void
    graniteLoginRefusesClientAdminOnlyButClientLoginAcceptsIt()
    {
        authorities = Set.of("ROLE_CLIENT_ADMIN");

        LoginReply granite = login("granite");

        assertFalse(granite.isSuccess());
        assertEquals("invalid credentials",granite.getFailureMessage());
        assertNull(granite.getSessionData());

        LoginReply client = login("client");

        assertTrue(client.isSuccess(),client.getFailureMessage());
        assertEquals("client",client.getSessionData().getApplication());

        authorities = Set.of("ROLE_USER","ROLE_CLIENT_ADMIN");
        assertTrue(login("granite").isSuccess());
    }

    @Test
    public void
    clientLoginRequiresClientAdmin()
    {
        authorities = Set.of("ROLE_ADMIN");

        LoginReply reply = login("client");

        assertFalse(reply.isSuccess());
        assertEquals("invalid credentials",reply.getFailureMessage());
        assertNull(reply.getAccessToken());
        assertNull(reply.getSessionData());
    }

    @Test
    public void
    sessionTokenCarriesAppAndRefreshKeepsIt()
        throws Exception
    {
        authorities = Set.of("ROLE_CLIENT_ADMIN");

        LoginReply login = login("client");

        assertTrue(login.isSuccess(),login.getFailureMessage());
        assertEquals("client",SignedJWT.parse(login.getAccessToken()).getJWTClaimsSet().getStringClaim("app"));

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);

        Mockito.verify(repository,Mockito.atLeastOnce()).save(captor.capture());

        Session saved = captor.getValue();

        assertEquals("client",saved.getApplication());
        Mockito
            .when(repository.findByRefreshTokenHash(tokens.hash(login.getRefreshToken())))
            .thenReturn(Optional.of(saved));

        RefreshReply refresh =
            subject
                .refresh(new RefreshRequest().setRefreshToken(login.getRefreshToken()))
                .toCompletableFuture()
                .join();

        assertTrue(refresh.isSuccess(),refresh.getFailureMessage());
        assertEquals("client",SignedJWT.parse(refresh.getAccessToken()).getJWTClaimsSet().getStringClaim("app"));
    }

    @Test
    public void
    oktaStartUsesPerApplicationRedirectAndCompletionReusesIt()
    {
        StartOktaLoginReply clientStart = startOkta("client");

        assertTrue(clientStart.isSuccess(),clientStart.getFailureMessage());
        assertEquals(CLIENT_REDIRECT,query(clientStart.getAuthorizeUrl()).get("redirect_uri"));

        StartOktaLoginReply graniteStart = startOkta(null);

        assertTrue(graniteStart.isSuccess(),graniteStart.getFailureMessage());
        assertEquals(FakeOkta.REDIRECT_URI,query(graniteStart.getAuthorizeUrl()).get("redirect_uri"));

        ArgumentCaptor<OktaLoginTransaction> captor = ArgumentCaptor.forClass(OktaLoginTransaction.class);

        Mockito.verify(transactions,Mockito.times(2)).save(captor.capture());
        assertEquals("client",captor.getAllValues().get(0).getApplication());
        assertEquals("granite",captor.getAllValues().get(1).getApplication());

        OktaLoginTransaction transaction = captor.getAllValues().get(0);

        transaction.setHashedTransactionId(tokens.hash(RAW_ID));
        Mockito
            .when(transactions.findByHashedTransactionId(tokens.hash(RAW_ID)))
            .thenReturn(Optional.of(transaction));
        okta.stubTokenReturning(okta.idToken("00u1okta",transaction.getNonce()));
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
                                        .setUserName("carol")
                                        .setExternalIdentity("okta:00u1okta")
                                        .setAuthorities(Set.of("ROLE_CLIENT_ADMIN"))))
                            .setSuccess(true)));

        CompleteOktaLoginReply reply =
            subject
                .completeOktaLogin(
                    new CompleteOktaLoginRequest()
                        .setCode("okta-authorization-code-0123456789")
                        .setState(transaction.getState())
                        .setTransactionId(RAW_ID)
                        .setDeviceId(DEVICE_ID))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals("client",reply.getSessionData().getApplication());
        assertEquals(CLIENT_REDIRECT,okta.tokenRequests().get(0).get("redirect_uri"));
    }

    private LoginReply
    login(String application)
    {
        return
            subject
                .login(
                    new LoginRequest()
                        .setUserName(USER_NAME)
                        .setCredential(PASSWORD)
                        .setCredentialKind(CredentialKind.PASSWORD)
                        .setDeviceId(DEVICE_ID)
                        .setApplication(application))
                .toCompletableFuture()
                .join();
    }

    private StartOktaLoginReply
    startOkta(String application)
    {
        return
            subject
                .startOktaLogin(
                    new StartOktaLoginRequest()
                        .setDeviceId(DEVICE_ID)
                        .setReturnTo("/")
                        .setApplication(application))
                .toCompletableFuture()
                .join();
    }

    private Map<String,String>
    query(String url)
    {
        return UriComponentsBuilder.fromUriString(url).build().getQueryParams().toSingleValueMap();
    }

    private SessionCommandService
    newSubject()
    {
        ApplicationRolesManager roles = new GraniteApplicationRolesManagerSupplier().get();

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
                new ApplicationLoginGate(
                    List.of("granite","client"),
                    new CatalogRoleLoginGate(roles),
                    Map.of("client",new RequiredAuthoritiesLoginGate(List.of(ApplicationRolesManager.CLIENT_ADMIN)))),
                new MockSessionEventSender(),
                new ModelMapper().registerModule(new MappingModule()),
                Duration.ofHours(24),
                Duration.ofMinutes(2),
                Clock.fixed(NOW,ZoneOffset.UTC),
                roles,
                null);
    }
}

//////////////////////////////////////////////////////////////////////////////
