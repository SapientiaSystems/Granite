//////////////////////////////////////////////////////////////////////////////
// ServiceTokenPropagationTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.configuration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import granite.foundation.core.security.GraniteJwtAuthenticationConverter;
import granite.foundation.core.security.GraniteJwtDecoderFactory;
import granite.foundation.core.security.GraniteResourceServerSettings;
import granite.session.server.application.IJwtTokenService;
import granite.session.server.application.IServiceTokenProvider;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.AuthenticateUserRequest;
import granite.user.service.requestreply.FindUsersByExternalIdentityRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class ServiceTokenPropagationTest
{
    private static final String AUTHENTICATE_USER_PATH         = "/user-command-service/authenticate-user";
    private static final String FIND_BY_EXTERNAL_IDENTITY_PATH = "/user-query-service/find-users-by-external-identity";

    private final GraniteSessionCommandServerConfiguration subject =
        new GraniteSessionCommandServerConfiguration();

    private WireMockServer        userService;
    private MockEnvironment       environment;
    private IServiceTokenProvider serviceTokenProvider;
    private JwtDecoder            decoder;

    @BeforeEach
    public void
    setUp()
    {
        userService = new WireMockServer(wireMockConfig().dynamicPort());
        userService.start();
        userService.stubFor(post(urlEqualTo(AUTHENTICATE_USER_PATH)).willReturn(okJson("{\"success\":true}")));
        userService.stubFor(post(urlEqualTo(FIND_BY_EXTERNAL_IDENTITY_PATH)).willReturn(okJson("{\"success\":true}")));

        environment = new MockEnvironment();
        environment.setActiveProfiles("local");
        environment.setProperty("granite.user-command-service.host",userService.baseUrl());
        environment.setProperty("granite.user-query-service.host",userService.baseUrl());

        IJwtTokenService jwtTokenService = subject.jwtTokenService(environment,new DefaultResourceLoader());

        serviceTokenProvider = subject.serviceTokenProvider(jwtTokenService,Clock.systemUTC(),environment);
        decoder              =
            new GraniteJwtDecoderFactory()
                .create(
                    subject.jwkSetProvider(environment,new DefaultResourceLoader()).getPublicJwkSet(),
                    GraniteResourceServerSettings.from(environment));
    }

    @AfterEach
    public void
    tearDown()
    {
        userService.stop();
    }

    @Test
    public void
    userQueryCallsCarryValidServiceToken()
    {
        IUserQueryServiceClient client =
            subject.userQueryServiceClient(noSslBundles(),environment,serviceTokenProvider);

        client
            .findUsersByExternalIdentity(
                new FindUsersByExternalIdentityRequest()
                    .setProvider("google")
                    .setExternalId("sub-1"))
            .toCompletableFuture()
            .join();

        assertServiceIdentity(lastRequest(FIND_BY_EXTERNAL_IDENTITY_PATH));
    }

    @Test
    public void
    userCommandCallsCarryValidServiceToken()
    {
        IUserCommandServiceClient client =
            subject.userCommandServiceClient(noSslBundles(),environment,serviceTokenProvider);

        client
            .authenticateUser(new AuthenticateUserRequest().setUserName("alice").setCredential("secret"))
            .toCompletableFuture()
            .join();

        assertServiceIdentity(lastRequest(AUTHENTICATE_USER_PATH));
    }

    @Test
    public void
    serviceIdentityIsConfigurable()
    {
        environment.setProperty("granite.session.service-token.subject","service:retail-session");
        environment.setProperty("granite.session.service-token.authorities","SCOPE_internal,SCOPE_audit");

        IServiceTokenProvider custom =
            subject.serviceTokenProvider(
                subject.jwtTokenService(environment,new DefaultResourceLoader()),
                Clock.systemUTC(),
                environment);
        Jwt                   jwt    = decoder.decode(custom.getToken());

        assertEquals("service:retail-session",jwt.getSubject());
        assertEquals(List.of("SCOPE_internal","SCOPE_audit"),jwt.getClaimAsStringList("authorities"));
    }

    private void
    assertServiceIdentity(LoggedRequest request)
    {
        String header = request.getHeader("Authorization");

        assertTrue(header != null && header.startsWith("Bearer "),"missing bearer header: " + header);

        Jwt jwt = decoder.decode(header.substring("Bearer ".length()));

        assertEquals("service:granite-session",jwt.getSubject());
        assertEquals(
            List.of("SCOPE_internal"),
            new GraniteJwtAuthenticationConverter()
                .readAuthorities(jwt)
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
    }

    private LoggedRequest
    lastRequest(String expectedPath)
    {
        List<LoggedRequest> requests = userService.findAll(postRequestedFor(anyUrl()));

        assertEquals(1,requests.size());
        assertEquals(expectedPath,requests.get(0).getUrl());
        return requests.get(0);
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<SslBundles>
    noSslBundles()
    {
        return Mockito.mock(ObjectProvider.class);
    }
}

//////////////////////////////////////////////////////////////////////////////
