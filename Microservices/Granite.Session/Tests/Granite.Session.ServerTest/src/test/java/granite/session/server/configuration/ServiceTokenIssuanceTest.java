//////////////////////////////////////////////////////////////////////////////
// ServiceTokenIssuanceTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.configuration;

import granite.foundation.core.security.GraniteJwtAuthenticationConverter;
import granite.foundation.core.security.GraniteJwtDecoderFactory;
import granite.foundation.core.security.GraniteResourceServerSettings;
import granite.session.server.application.IJwtTokenService;
import granite.session.server.application.IServiceTokenIssuer;
import granite.session.service.requestreply.IssueServiceTokenReply;
import granite.session.service.requestreply.IssueServiceTokenRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class ServiceTokenIssuanceTest
{
    private final GraniteSessionCommandServerConfiguration subject =
        new GraniteSessionCommandServerConfiguration();

    private MockEnvironment  environment;
    private IJwtTokenService jwtTokenService;
    private JwtDecoder       decoder;

    @BeforeEach
    public void
    setUp()
        throws Exception
    {
        environment = new MockEnvironment();
        environment.setActiveProfiles("local");
        environment.setProperty("granite.session.service-clients.granite-frontendcontroller.secret-sha256",hex("fec-secret"));
        environment.setProperty("granite.session.service-clients.granite-frontendcontroller.lifetime","PT7M");
        environment.setProperty("granite.session.service-clients.reporting.secret-sha256",hex("reporting-secret"));
        environment.setProperty("granite.session.service-clients.unconfigured.secret-sha256","");

        jwtTokenService = subject.jwtTokenService(environment,new DefaultResourceLoader());
        decoder         =
            new GraniteJwtDecoderFactory()
                .create(
                    subject.jwkSetProvider(environment,new DefaultResourceLoader()).getPublicJwkSet(),
                    GraniteResourceServerSettings.from(environment));
    }

    @Test
    public void
    configuredClientReceivesInternalTokenWithConfiguredLifetime()
    {
        IssueServiceTokenReply reply = issue("granite-frontendcontroller","fec-secret");
        Jwt                    jwt   = decoder.decode(reply.getToken());

        assertTrue(reply.isSuccess());
        assertEquals("granite-frontendcontroller",jwt.getSubject());
        assertEquals(
            List.of("SCOPE_internal"),
            new GraniteJwtAuthenticationConverter()
                .readAuthorities(jwt)
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        assertEquals(Duration.ofMinutes(7),Duration.between(jwt.getIssuedAt(),jwt.getExpiresAt()));
        assertEquals(jwt.getExpiresAt(),reply.getExpiresAt());
    }

    @Test
    public void
    lifetimeDefaultsToTenMinutes()
    {
        Jwt jwt = decoder.decode(issue("reporting","reporting-secret").getToken());

        assertEquals(Duration.ofMinutes(10),Duration.between(jwt.getIssuedAt(),jwt.getExpiresAt()));
    }

    @Test
    public void
    wrongSecretOrUnconfiguredClientIsRefused()
    {
        assertFalse(issue("granite-frontendcontroller","reporting-secret").isSuccess());
        assertFalse(issue("unconfigured","").isSuccess());
        assertFalse(issue("nobody","fec-secret").isSuccess());
    }

    @Test
    public void
    lifetimeAboveOneHourFailsStartup()
    {
        environment.setProperty("granite.session.service-clients.granite-frontendcontroller.lifetime","PT61M");

        assertThrows(IllegalArgumentException.class,() -> subject.serviceTokenIssuer(jwtTokenService,environment));
    }

    @Test
    public void
    hashOfEmptySecretFailsStartup()
        throws Exception
    {
        environment.setProperty("granite.session.service-clients.reporting.secret-sha256",hex(""));

        assertThrows(IllegalArgumentException.class,() -> subject.serviceTokenIssuer(jwtTokenService,environment));
    }

    private IssueServiceTokenReply
    issue(String clientId,String secret)
    {
        IServiceTokenIssuer issuer = subject.serviceTokenIssuer(jwtTokenService,environment);

        return issuer.issue(new IssueServiceTokenRequest().setClientId(clientId).setClientSecret(secret));
    }

    private static String
    hex(String secret)
        throws Exception
    {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8)));
    }
}

//////////////////////////////////////////////////////////////////////////////
