//////////////////////////////////////////////////////////////////////////////
// ExtendedFrontEndControllerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import granite.foundation.core.security.ApplicationRoles;
import granite.frontendcontroller.server.application.GraniteFrontEndControllerContext;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import org.mockito.Mockito;

public
class ExtendedFrontEndControllerConfiguration
    extends AbstractGraniteFrontEndControllerConfiguration<IExtendedFrontEndControllerService>
{
    private final IUserQueryServiceClient userQueryClient;
    private final ApplicationRoles        roles;

    public
    ExtendedFrontEndControllerConfiguration(IUserQueryServiceClient userQueryClient,ApplicationRoles roles)
    {
        this.userQueryClient = userQueryClient;
        this.roles = roles;
    }

    @Override
    public ApplicationRoles
    applicationRoles()
    {
        return roles == null ? super.applicationRoles() : roles;
    }

    @Override
    protected IExtendedFrontEndControllerService
    createService(GraniteFrontEndControllerContext context)
    {
        return new ExtendedFrontEndControllerService(context);
    }

    @Override
    protected Object
    createController(IExtendedFrontEndControllerService service)
    {
        return new ExtendedFrontEndControllerServiceController(service);
    }

    @Override
    protected IUserQueryServiceClient
    createUserQueryClient() { return userQueryClient; }

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
