//////////////////////////////////////////////////////////////////////////////
// TestConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import granite.session.server.domain.DomainConfiguration;
import granite.session.server.platform.CommandPlatformConfiguration;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.AuthenticateUserReply;
import granite.user.service.requestreply.AuthenticateUserRequest;
import granite.user.service.requestreply.CreateUserReply;
import granite.user.service.requestreply.CreateUserRequest;
import granite.user.service.requestreply.FindUsersByExternalIdentityReply;
import granite.user.service.requestreply.FindUsersByExternalIdentityRequest;
import granite.user.service.requestreply.UserData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.mockito.Mockito;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import strata.foundation.core.configuration.IConfiguration;
import strata.foundation.core.inject.ApplicationConfigurationProvider;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Statement;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.mockito.ArgumentMatchers.any;

@Configuration
@EnableTransactionManagement
@PropertySource("classpath:test.properties")
@Import(
    {
        ApplicationConfiguration.class,
        DomainConfiguration.class,
        CommandPlatformConfiguration.class
    })
public
class TestConfiguration
{
    public static final String JWKS_PATH = "/.well-known/jwks.json";

    private static final String USER_PREFIX     = "user-";
    private static final String USER_SUFFIX     = "@example.com";
    private static final String PASSWORD_PREFIX = "password-";

    @Bean
    public static BeanFactoryPostProcessor
    requestScopeRegistrar()
    {
        return
            factory ->
                factory.registerScope(
                    "request",
                    new org.springframework.web.context.request.RequestScope());
    }

    @Bean
    @Scope("singleton")
    public IConfiguration
    configuration()
    {
        return new ApplicationConfigurationProvider("test").get();
    }

    @Bean
    @Scope("singleton")
    public InitializingBean
    schemaInitializer(EntityManagerFactory factory)
    {
        return
            () ->
            {
                String script;

                try (InputStream in = getClass().getResourceAsStream("/schema.sql"))
                {
                    script = new String(in.readAllBytes(),StandardCharsets.UTF_8);
                }

                EntityManager em = factory.createEntityManager();

                try
                {
                    em.unwrap(org.hibernate.Session.class).doWork(
                        connection ->
                        {
                            for (String sql : script.split(";"))
                            {
                                if (!sql.isBlank())
                                {
                                    try (Statement statement = connection.createStatement())
                                    {
                                        statement.execute(sql.trim());
                                    }
                                }
                            }
                        });
                }
                finally
                {
                    em.close();
                }
            };
    }

    @Bean
    @Scope("singleton")
    public Clock
    clock()
    {
        return new TickingClock(Instant.now(),Duration.ofSeconds(1));
    }

    @Bean
    @Scope("singleton")
    public RSAKey
    oauth2SigningKey()
        throws JOSEException
    {
        return
            new RSAKeyGenerator(2048)
                .keyID(UUID.randomUUID().toString())
                .generate();
    }

    @Bean(destroyMethod = "stop")
    @Scope("singleton")
    public WireMockServer
    jwksServer(RSAKey oauth2SigningKey,@Value("${test.wiremock.port}") int port)
    {
        WireMockServer server = new WireMockServer(wireMockConfig().port(port));

        server.start();
        server.stubFor(
            get(urlEqualTo(JWKS_PATH))
                .willReturn(
                    okJson(
                        new JWKSet(oauth2SigningKey.toPublicJWK())
                            .toJSONObject()
                            .toString())));
        return server;
    }

    @Bean
    @Scope("singleton")
    public IUserCommandServiceClient
    userCommandServiceClient()
    {
        IUserCommandServiceClient client = Mockito.mock(IUserCommandServiceClient.class);
        Map<String,Long>          ids    = new ConcurrentHashMap<>();
        AtomicLong                next   = new AtomicLong(1000L);

        Mockito
            .when(client.authenticateUser(any(AuthenticateUserRequest.class)))
            .thenAnswer(
                invocation ->
                {
                    AuthenticateUserRequest request  = invocation.getArgument(0);
                    String                  userName = request.getUserName();
                    boolean                 valid    =
                        userName != null
                            && userName.startsWith(USER_PREFIX)
                            && userName.endsWith(USER_SUFFIX)
                            && (PASSWORD_PREFIX + userName).equals(request.getCredential());
                    AuthenticateUserReply   reply    = new AuthenticateUserReply(request);

                    if (valid)
                        reply
                            .setValid(true)
                            .setUserId(ids.computeIfAbsent(userName,k -> next.incrementAndGet()))
                            .setSuccess(true);
                    else
                        reply
                            .setValid(false)
                            .setSuccess(false)
                            .setFailureMessage("invalid credentials");

                    return CompletableFuture.completedFuture(reply);
                });

        Mockito
            .when(client.createUser(any(CreateUserRequest.class)))
            .thenAnswer(
                invocation ->
                {
                    CreateUserRequest request = invocation.getArgument(0);

                    return
                        CompletableFuture.completedFuture(
                            new CreateUserReply(request)
                                .setCreatedUser(
                                    new UserData()
                                        .setUserId(next.incrementAndGet())
                                        .setUserName(request.getUserToCreate().getUserName())
                                        .setExternalIdentity(
                                            request.getUserToCreate().getExternalIdentity()))
                                .setSuccess(true));
                });

        return client;
    }

    @Bean
    @Scope("singleton")
    public IUserQueryServiceClient
    userQueryServiceClient()
    {
        IUserQueryServiceClient client = Mockito.mock(IUserQueryServiceClient.class);

        Mockito
            .when(client.findUsersByExternalIdentity(any(FindUsersByExternalIdentityRequest.class)))
            .thenAnswer(
                invocation ->
                {
                    FindUsersByExternalIdentityRequest request = invocation.getArgument(0);

                    return
                        CompletableFuture.completedFuture(
                            new FindUsersByExternalIdentityReply(request)
                                .setUsers(
                                    List.of(
                                        new UserData()
                                            .setUserId(
                                                500L + Math.floorMod(request.getExternalId().hashCode(),500))
                                            .setUserName(request.getExternalId())
                                            .setExternalIdentity(
                                                request.getProvider() + ":" + request.getExternalId())))
                                .setSuccess(true));
                });

        return client;
    }
}

//////////////////////////////////////////////////////////////////////////////
