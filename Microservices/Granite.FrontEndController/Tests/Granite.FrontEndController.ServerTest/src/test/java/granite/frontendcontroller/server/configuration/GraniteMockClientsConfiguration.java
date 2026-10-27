//////////////////////////////////////////////////////////////////////////////
// GraniteMockClientsConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindAllUsersReply;
import granite.user.service.requestreply.FindAllUsersRequest;
import org.mockito.Mockito;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Configuration
public
class GraniteMockClientsConfiguration
    extends GraniteFrontEndControllerConfiguration
{
    @Override
    protected IUserQueryServiceClient
    createUserQueryClient()
    {
        IUserQueryServiceClient client = Mockito.mock(IUserQueryServiceClient.class);

        Mockito
            .when(client.findAllUsers(Mockito.any(FindAllUsersRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new FindAllUsersReply().setSuccess(true).setUsers(List.of())));

        return client;
    }

    @Override
    protected IUserCommandServiceClient
    createUserCommandClient() { return Mockito.mock(IUserCommandServiceClient.class); }

    @Override
    protected ISessionCommandServiceClient
    createSessionCommandClient() { return Mockito.mock(ISessionCommandServiceClient.class); }

    @Override
    protected ISessionQueryServiceClient
    createSessionQueryClient() { return Mockito.mock(ISessionQueryServiceClient.class); }
}

//////////////////////////////////////////////////////////////////////////////
