//////////////////////////////////////////////////////////////////////////////
// TestConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import granite.session.server.configuration.GraniteSessionCommandServerConfiguration;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.AuthenticateUserReply;
import granite.user.service.requestreply.AuthenticateUserRequest;
import granite.user.service.requestreply.CreateUserReply;
import granite.user.service.requestreply.CreateUserRequest;
import granite.user.service.requestreply.FindUsersByExternalIdentityReply;
import granite.user.service.requestreply.FindUsersByExternalIdentityRequest;
import granite.user.service.requestreply.UserData;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import strata.foundation.core.configuration.IConfiguration;
import strata.foundation.core.inject.ApplicationConfigurationProvider;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.ArgumentMatchers.any;

@Configuration
@PropertySource({"classpath:application.properties","classpath:application-local.properties"})
public
class TestConfiguration
    extends GraniteSessionCommandServerConfiguration
{
    private static final String USER_PREFIX     = "user-";
    private static final String USER_SUFFIX     = "@example.com";
    private static final String PASSWORD_PREFIX = "password-";

    private final Map<String,UserData> externalUsers = new ConcurrentHashMap<>();
    private final AtomicLong          externalIds   = new AtomicLong(5000L);

    @Override
    @Bean
    @Scope("singleton")
    public IConfiguration
    configuration(Environment environment)
    {
        return new ApplicationConfigurationProvider("application-local").get();
    }

    @Override
    @Bean
    @Scope("singleton")
    public Clock
    clock()
    {
        return new TickingClock(Instant.now(),Duration.ofSeconds(1));
    }

    @Bean
    @Scope("singleton")
    public ObjectMapper
    objectMapper()
    {
        return new ObjectMapper();
    }

    @Bean(destroyMethod = "stop")
    @Scope("singleton")
    public FakeOkta
    fakeOkta()
    {
        return new FakeOkta();
    }

    @Override
    @Bean
    @Scope("singleton")
    public OktaSettings
    oktaSettings(Environment environment)
    {
        return
            fakeOkta()
                .settings()
                .setGroupMapping(Map.of("Granite Admins","ROLE_Admin"));
    }

    @Override
    @Bean
    @Scope("singleton")
    public IOktaClient
    oktaClient(OktaSettings settings,Environment environment,ResourceLoader loader,Clock clock)
    {
        return fakeOkta().client(settings,Clock.systemUTC());
    }

    @Override
    @Bean
    @Scope("singleton")
    public IUserCommandServiceClient
    userCommandServiceClient(
        ObjectProvider<SslBundles> sslBundles,
        Environment                environment,
        IServiceTokenProvider      serviceTokenProvider)
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
                            .setUser(
                                new UserData()
                                    .setUserId(ids.computeIfAbsent(userName,k -> next.incrementAndGet()))
                                    .setUserName(userName)
                                    .setAuthorities(java.util.Set.of("ROLE_User","ROLE_Admin")))
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
                    UserData          created =
                        new UserData()
                            .setUserId(externalIds.incrementAndGet())
                            .setUserName(request.getUserToCreate().getUserName())
                            .setExternalIdentity(request.getUserToCreate().getExternalIdentity())
                            .setAuthorities(request.getUserToCreate().getAuthorities());

                    externalUsers.put(created.getExternalIdentity(),created);
                    return
                        CompletableFuture.completedFuture(
                            new CreateUserReply(request)
                                .setSuccess(true)
                                .setCreatedUser(created));
                });

        return client;
    }

    @Override
    @Bean
    @Scope("singleton")
    public IUserQueryServiceClient
    userQueryServiceClient(
        ObjectProvider<SslBundles> sslBundles,
        Environment                environment,
        IServiceTokenProvider      serviceTokenProvider)
    {
        IUserQueryServiceClient client = Mockito.mock(IUserQueryServiceClient.class);

        Mockito
            .when(client.findUsersByExternalIdentity(any(FindUsersByExternalIdentityRequest.class)))
            .thenAnswer(
                invocation ->
                {
                    FindUsersByExternalIdentityRequest request = invocation.getArgument(0);

                    UserData                           found   =
                        externalUsers.get(request.getProvider() + ":" + request.getExternalId());

                    return
                        CompletableFuture.completedFuture(
                            new FindUsersByExternalIdentityReply(request)
                                .setUsers(found != null ? List.of(found) : List.of())
                                .setSuccess(true));
                });

        return client;
    }
}

//////////////////////////////////////////////////////////////////////////////
