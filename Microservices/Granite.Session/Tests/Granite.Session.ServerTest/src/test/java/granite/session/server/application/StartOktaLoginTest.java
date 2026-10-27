//////////////////////////////////////////////////////////////////////////////
// StartOktaLoginTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.session.server.domain.IOktaLoginTransactionRepository;
import granite.session.server.domain.ISessionRepository;
import granite.session.server.domain.OktaLoginTransaction;
import granite.session.server.domain.Session;
import granite.session.server.platform.MockSessionEventSender;
import granite.session.service.requestreply.StartOktaLoginReply;
import granite.session.service.requestreply.StartOktaLoginRequest;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class StartOktaLoginTest
{
    private static final String  DEVICE_ID = "device-A";
    private static final Instant NOW       = Instant.parse("2026-10-24T12:00:00Z");

    private FakeOkta                        okta;
    private ISessionRepository              repository;
    private IOktaLoginTransactionRepository transactions;
    private RefreshTokenService             tokens;
    private OktaSettings                    settings;

    @BeforeEach
    public void
    setUp()
    {
        okta         = new FakeOkta();
        repository   = Mockito.mock(ISessionRepository.class);
        transactions = Mockito.mock(IOktaLoginTransactionRepository.class);
        tokens       = new RefreshTokenService();
        settings     = okta.settings();

        Mockito.when(repository.findOpenByDeviceId(Mockito.anyString())).thenReturn(List.of());
        Mockito
            .when(transactions.save(Mockito.any(OktaLoginTransaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    public void
    tearDown()
    {
        okta.stop();
    }

    @Test
    public void
    startStoresATransactionAndReturnsTheAuthorizeUrl()
        throws Exception
    {
        StartOktaLoginReply  reply       = start("/users");
        OktaLoginTransaction transaction = savedTransaction();
        Map<String,String>   query       = query(reply.getAuthorizeUrl());

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertTrue(reply.getAuthorizeUrl().startsWith(okta.url(FakeOkta.AUTHORIZE_PATH)));
        assertEquals(tokens.hash(reply.getTransactionId()),transaction.getHashedTransactionId());
        assertNotEquals(reply.getTransactionId(),transaction.getHashedTransactionId());
        assertEquals(transaction.getState(),query.get("state"));
        assertEquals(transaction.getNonce(),query.get("nonce"));
        assertEquals(challengeOf(transaction.getCodeVerifier()),query.get("code_challenge"));
        assertEquals("S256",query.get("code_challenge_method"));
        assertEquals("code",query.get("response_type"));
        assertEquals("/users",transaction.getReturnTo());
        assertEquals(DEVICE_ID,transaction.getDeviceId());
        assertEquals(NOW.plus(Duration.ofMinutes(5)),transaction.getExpires());
        assertFalse(query.containsKey("prompt"));
        assertTrue(transaction.getCodeVerifier().length() >= 43);
        assertFalse(reply.getAuthorizeUrl().contains(transaction.getCodeVerifier()));
    }

    @Test
    public void
    eachStartUsesFreshSecrets()
    {
        StartOktaLoginReply first  = start("/");
        StartOktaLoginReply second = start("/");

        assertNotEquals(first.getTransactionId(),second.getTransactionId());
        assertNotEquals(query(first.getAuthorizeUrl()).get("state"),query(second.getAuthorizeUrl()).get("state"));
        assertNotEquals(query(first.getAuthorizeUrl()).get("nonce"),query(second.getAuthorizeUrl()).get("nonce"));
    }

    @Test
    public void
    activeSessionOnTheDeviceAsksOktaForALogin()
    {
        Session active =
            new Session()
                .setUserId(7L)
                .setDeviceId(DEVICE_ID)
                .setExpires(NOW.plus(Duration.ofHours(1)));

        active.setCreated(NOW.minusSeconds(60));
        Mockito.when(repository.findOpenByDeviceId(DEVICE_ID)).thenReturn(List.of(active));

        assertEquals("login",query(start("/").getAuthorizeUrl()).get("prompt"));
    }

    @Test
    public void
    expiredSessionOnTheDeviceDoesNotForceALogin()
    {
        Session stale =
            new Session()
                .setUserId(7L)
                .setDeviceId(DEVICE_ID)
                .setExpires(NOW.minusSeconds(1));

        stale.setCreated(NOW.minus(Duration.ofHours(25)));
        Mockito.when(repository.findOpenByDeviceId(DEVICE_ID)).thenReturn(List.of(stale));

        assertFalse(query(start("/").getAuthorizeUrl()).containsKey("prompt"));
    }

    @Test
    public void
    offSiteReturnToBecomesRoot()
    {
        for (String returnTo : new String[] {"https://evil.example.com/","//evil.example.com","/\\evil",null,"","users"})
        {
            Mockito.clearInvocations(transactions);
            start(returnTo);
            assertEquals("/",savedTransaction().getReturnTo(),String.valueOf(returnTo));
        }
    }

    @Test
    public void
    disabledOktaFailsWithoutContactingOkta()
    {
        settings.setEnabled(false);

        StartOktaLoginReply reply = start("/");

        assertFalse(reply.isSuccess());
        assertNull(reply.getAuthorizeUrl());
        assertNull(reply.getTransactionId());
        assertEquals(0,okta.discoveryRequests());
        Mockito.verifyNoInteractions(transactions);
    }

    @Test
    public void
    missingDeviceFails()
    {
        StartOktaLoginReply reply =
            subject()
                .startOktaLogin(new StartOktaLoginRequest().setReturnTo("/"))
                .toCompletableFuture()
                .join();

        assertFalse(reply.isSuccess());
        Mockito.verifyNoInteractions(transactions);
    }

    @Test
    public void
    unreachableOktaFailsGenerically()
    {
        okta.stop();

        StartOktaLoginReply reply = start("/");

        assertFalse(reply.isSuccess());
        assertEquals("Unable to start Okta login",reply.getFailureMessage());
        assertNull(reply.getException());
    }

    private StartOktaLoginReply
    start(String returnTo)
    {
        return
            subject()
                .startOktaLogin(new StartOktaLoginRequest().setDeviceId(DEVICE_ID).setReturnTo(returnTo))
                .toCompletableFuture()
                .join();
    }

    private SessionCommandService
    subject()
    {
        Clock clock = Clock.fixed(NOW,ZoneOffset.UTC);

        return
            new SessionCommandService(
                repository,
                transactions,
                Mockito.mock(IJwtTokenService.class),
                tokens,
                Mockito.mock(IUserCommandServiceClient.class),
                Mockito.mock(IUserQueryServiceClient.class),
                okta.client(settings,Clock.systemUTC()),
                settings,
                new RequiredAuthoritiesLoginGate(List.of()),
                new MockSessionEventSender(),
                new ModelMapper().registerModule(new MappingModule()),
                Duration.ofHours(24),
                Duration.ofMinutes(2),
                clock,
                new GraniteApplicationRolesManagerSupplier().get());
    }

    private OktaLoginTransaction
    savedTransaction()
    {
        ArgumentCaptor<OktaLoginTransaction> captor = ArgumentCaptor.forClass(OktaLoginTransaction.class);

        Mockito.verify(transactions).save(captor.capture());
        return captor.getValue();
    }

    private Map<String,String>
    query(String url)
    {
        return UriComponentsBuilder.fromUriString(url).build().getQueryParams().toSingleValueMap();
    }

    private String
    challengeOf(String verifier)
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
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
