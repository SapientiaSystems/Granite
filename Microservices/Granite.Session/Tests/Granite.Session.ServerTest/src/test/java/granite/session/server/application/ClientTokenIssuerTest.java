//////////////////////////////////////////////////////////////////////////////
// ClientTokenIssuerTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.client.client.requestreply.IClientCommandServiceClient;
import granite.client.service.requestreply.VerifyClientCredentialsReply;
import granite.client.service.requestreply.VerifyClientCredentialsRequest;
import granite.foundation.core.security.GraniteJwtAuthenticationConverter;
import granite.foundation.core.security.GraniteJwtDecoderFactory;
import granite.foundation.core.security.GraniteResourceServerSettings;
import granite.session.server.configuration.GraniteSessionCommandServerConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Duration;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class ClientTokenIssuerTest
{
    private static final String CLIENT_ID = "gc_abcdefghijklmnopqrstuvwxyz234567";

    private final GraniteSessionCommandServerConfiguration subject =
        new GraniteSessionCommandServerConfiguration();

    private MockEnvironment             environment;
    private IJwtTokenService            jwtTokenService;
    private IClientCommandServiceClient clientService;
    private JwtDecoder                  decoder;

    @BeforeEach
    public void
    setUp()
    {
        environment = new MockEnvironment();
        environment.setActiveProfiles("local");

        jwtTokenService = subject.jwtTokenService(environment,new DefaultResourceLoader());
        clientService   = Mockito.mock(IClientCommandServiceClient.class);
        decoder         =
            new GraniteJwtDecoderFactory()
                .create(
                    subject.jwkSetProvider(environment,new DefaultResourceLoader()).getPublicJwkSet(),
                    GraniteResourceServerSettings.from(environment));
    }

    @Test
    public void
    issuesClientTokenWithSubjectAuthoritiesAndLifetime()
    {
        reply(valid("ROLE_AUDITOR","ROLE_USER"));

        ClientTokenResult result = issue();
        Jwt               jwt    = decoder.decode(result.getToken());

        assertEquals(ClientTokenOutcome.OK,result.getOutcome());
        assertEquals(900L,result.getExpiresInSeconds());
        assertEquals("client:" + CLIENT_ID,jwt.getSubject());
        assertEquals(List.of("ROLE_AUDITOR","ROLE_USER"),authorities(jwt));
        assertEquals(Duration.ofMinutes(15),Duration.between(jwt.getIssuedAt(),jwt.getExpiresAt()));
        assertNotNull(jwt.getId());
        assertNull(jwt.getClaim("did"));
    }

    @Test
    public void
    sendsCredentialsToClientService()
    {
        reply(valid("ROLE_USER"));
        issue();

        ArgumentCaptor<VerifyClientCredentialsRequest> captor =
            ArgumentCaptor.forClass(VerifyClientCredentialsRequest.class);

        Mockito.verify(clientService).verifyClientCredentials(captor.capture());
        assertEquals(CLIENT_ID,captor.getValue().getClientId());
        assertEquals("secret",captor.getValue().getClientSecret());
    }

    @Test
    public void
    filtersAdminAndScopesDefensively()
    {
        reply(valid("ROLE_ADMIN","ROLE_USER","SCOPE_internal"));

        assertEquals(List.of("ROLE_USER"),authorities(decoder.decode(issue().getToken())));
    }

    @Test
    public void
    invalidCredentialsAreRefused()
    {
        reply(new VerifyClientCredentialsReply().setSuccess(true).setValid(false).setFailureMessage("invalid client"));
        assertEquals(ClientTokenOutcome.INVALID_CLIENT,issue().getOutcome());

        reply(new VerifyClientCredentialsReply().setSuccess(false).setFailureMessage("invalid client"));
        assertEquals(ClientTokenOutcome.INVALID_CLIENT,issue().getOutcome());
    }

    @Test
    public void
    blankCredentialsAreRefusedWithoutCallingTheClientService()
    {
        IClientTokenIssuer issuer = subject.clientTokenIssuer(jwtTokenService,clientService,environment);

        assertEquals(ClientTokenOutcome.INVALID_CLIENT,issuer.issue("","secret").toCompletableFuture().join().getOutcome());
        assertEquals(ClientTokenOutcome.INVALID_CLIENT,issuer.issue(CLIENT_ID,"").toCompletableFuture().join().getOutcome());
        Mockito.verifyNoInteractions(clientService);
    }

    @Test
    public void
    infrastructureFailuresAreUnavailable()
    {
        reply(new VerifyClientCredentialsReply().setSuccess(false).setFailureMessage("unable to verify client"));
        assertEquals(ClientTokenOutcome.UNAVAILABLE,issue().getOutcome());

        reply(new VerifyClientCredentialsReply().setSuccess(false).setFailureMessage("boom"));
        assertEquals(ClientTokenOutcome.UNAVAILABLE,issue().getOutcome());

        Mockito
            .when(clientService.verifyClientCredentials(Mockito.any()))
            .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("down")));
        assertEquals(ClientTokenOutcome.UNAVAILABLE,issue().getOutcome());

        Mockito
            .when(clientService.verifyClientCredentials(Mockito.any()))
            .thenThrow(new IllegalStateException("down"));
        assertEquals(ClientTokenOutcome.UNAVAILABLE,issue().getOutcome());
    }

    @Test
    public void
    lifetimeIsConfigurable()
    {
        environment.setProperty("granite.session.client-token.lifetime","PT1H");
        reply(valid("ROLE_USER"));

        ClientTokenResult result = issue();
        Jwt               jwt    = decoder.decode(result.getToken());

        assertEquals(3600L,result.getExpiresInSeconds());
        assertEquals(Duration.ofHours(1),Duration.between(jwt.getIssuedAt(),jwt.getExpiresAt()));
    }

    @Test
    public void
    lifetimeAboveOneHourFailsStartup()
    {
        environment.setProperty("granite.session.client-token.lifetime","PT61M");

        assertThrows(
            IllegalArgumentException.class,
            () -> subject.clientTokenIssuer(jwtTokenService,clientService,environment));
    }

    @Test
    public void
    resultDoesNotExposeTokenInToString()
    {
        assertFalse(ClientTokenResult.ok("tok",1L).toString().contains("tok"));
        assertTrue(ClientTokenResult.unavailable().toString().contains("UNAVAILABLE"));
    }

    private ClientTokenResult
    issue()
    {
        return
            subject
                .clientTokenIssuer(jwtTokenService,clientService,environment)
                .issue(CLIENT_ID,"secret")
                .toCompletableFuture()
                .join();
    }

    private void
    reply(VerifyClientCredentialsReply reply)
    {
        Mockito
            .when(clientService.verifyClientCredentials(Mockito.any()))
            .thenReturn(CompletableFuture.completedFuture(reply));
    }

    private static VerifyClientCredentialsReply
    valid(String... authorities)
    {
        return
            new VerifyClientCredentialsReply()
                .setSuccess(true)
                .setValid(true)
                .setClientId(CLIENT_ID)
                .setAuthorities(new TreeSet<>(List.of(authorities)));
    }

    private static List<String>
    authorities(Jwt jwt)
    {
        return
            new GraniteJwtAuthenticationConverter()
                .readAuthorities(jwt)
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }
}

//////////////////////////////////////////////////////////////////////////////
