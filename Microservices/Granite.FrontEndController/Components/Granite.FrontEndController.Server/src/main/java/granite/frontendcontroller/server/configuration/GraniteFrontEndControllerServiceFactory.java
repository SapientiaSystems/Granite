//////////////////////////////////////////////////////////////////////////////
// GraniteFrontEndControllerServiceFactory.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import granite.frontendcontroller.server.application.SessionFrontEndControllerService;
import granite.frontendcontroller.server.application.UsersFrontEndControllerService;
import granite.frontendcontroller.server.control.session.SessionLoginSettings;
import granite.frontendcontroller.server.platform.ForwardedBearerClientRequestFilter;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.ISessionFrontEndControllerService;
import granite.frontendcontroller.service.requestreply.IUsersFrontEndControllerService;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.session.client.requestreply.SessionCommandServiceClient;
import granite.session.client.requestreply.SessionQueryServiceClient;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.client.requestreply.UserCommandServiceClient;
import granite.user.client.requestreply.UserQueryServiceClient;
import jakarta.ws.rs.client.ClientBuilder;
import org.modelmapper.ModelMapper;
import org.springframework.boot.ssl.SslBundles;
import strata.foundation.core.configuration.IConfiguration;

import java.util.concurrent.Executor;

public
class GraniteFrontEndControllerServiceFactory
{
    private final IConfiguration configuration;
    private final SslBundles     sslBundles;

    public
    GraniteFrontEndControllerServiceFactory(IConfiguration configuration,SslBundles sslBundles)
    {
        this.configuration = configuration;
        this.sslBundles = sslBundles;
    }

    public IUsersFrontEndControllerService
    createUsersService(ModelMapper mapper,Executor executor)
    {
        return new UsersFrontEndControllerService(mapper,executor,userQueryClient(),userCommandClient());
    }

    public ISessionFrontEndControllerService
    createSessionService(ModelMapper mapper,Executor executor)
    {
        return
            new SessionFrontEndControllerService(
                mapper,
                executor,
                sessionCommandClient(),
                sessionQueryClient(),
                userQueryClient(),
                SessionRequestContext.fromCurrentRequest(),
                SessionLoginSettings.fromConfiguration(configuration));
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
