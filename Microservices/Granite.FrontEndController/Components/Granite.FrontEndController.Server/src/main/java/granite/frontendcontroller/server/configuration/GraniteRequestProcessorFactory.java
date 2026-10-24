//////////////////////////////////////////////////////////////////////////////
// GraniteRequestProcessorFactory.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import granite.frontendcontroller.server.control.PrefixRoutingRequestProcessor;
import granite.frontendcontroller.server.control.session.SessionLoginSettings;
import granite.frontendcontroller.server.control.session.SessionRequestProcessor;
import granite.frontendcontroller.server.control.user.UserRequestProcessor;
import granite.frontendcontroller.server.platform.ForwardedBearerClientRequestFilter;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.session.client.requestreply.SessionCommandServiceClient;
import granite.session.client.requestreply.SessionQueryServiceClient;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.client.requestreply.UserCommandServiceClient;
import granite.user.client.requestreply.UserQueryServiceClient;
import jakarta.ws.rs.client.ClientBuilder;
import org.springframework.boot.ssl.SslBundles;
import strata.foundation.core.configuration.IConfiguration;
import strata.frontendcontroller.server.application.IRequestProcessor;

import java.util.Map;

public
class GraniteRequestProcessorFactory
{
    private final IConfiguration configuration;
    private final SslBundles     sslBundles;

    public
    GraniteRequestProcessorFactory(IConfiguration configuration,SslBundles sslBundles)
    {
        this.configuration = configuration;
        this.sslBundles = sslBundles;
    }

    public IRequestProcessor
    create()
    {
        SessionRequestContext context = SessionRequestContext.fromCurrentRequest();
        IRequestProcessor     session =
            new SessionRequestProcessor(
                sessionCommandClient(),
                sessionQueryClient(),
                userQueryClient(),
                context,
                SessionLoginSettings.fromConfiguration(configuration));
        IRequestProcessor     user = new UserRequestProcessor(userQueryClient(),userCommandClient());

        return new PrefixRoutingRequestProcessor(Map.of("Session.",session),user);
    }

    public IUserQueryServiceClient
    userQueryClient()
    {
        return new UserQueryServiceClient(createClientBuilder(),configuration.getProperty("user-query-service.base-url"));
    }

    public IUserCommandServiceClient
    userCommandClient()
    {
        return new UserCommandServiceClient(createClientBuilder(),configuration.getProperty("user-command-service.base-url"));
    }

    public ISessionCommandServiceClient
    sessionCommandClient()
    {
        return
            new SessionCommandServiceClient(
                createClientBuilder(),
                configuration.getProperty("session-command-service.base-url"));
    }

    public ISessionQueryServiceClient
    sessionQueryClient()
    {
        return
            new SessionQueryServiceClient(
                createClientBuilder(),
                configuration.getProperty("session-query-service.base-url"));
    }

    private ClientBuilder
    createClientBuilder()
    {
        return
            ClientBuilder
                .newBuilder()
                .register(ForwardedBearerClientRequestFilter.fromCurrentRequest())
                .sslContext(
                    sslBundles
                        .getBundle("granite-services")
                        .createSslContext());
    }
}

//////////////////////////////////////////////////////////////////////////////
