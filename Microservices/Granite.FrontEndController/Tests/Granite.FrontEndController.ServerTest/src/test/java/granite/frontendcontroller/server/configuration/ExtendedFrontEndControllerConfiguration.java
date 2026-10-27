//////////////////////////////////////////////////////////////////////////////
// ExtendedFrontEndControllerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import granite.foundation.core.security.ApplicationRoles;
import granite.frontendcontroller.server.application.GraniteFrontEndControllerContext;
import granite.frontendcontroller.server.application.PreferenceDefinition;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindAllUsersReply;
import granite.user.service.requestreply.FindAllUsersRequest;
import jakarta.inject.Named;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import strata.foundation.spring.inject.RequestScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.CompletableFuture;

@Configuration
public
class ExtendedFrontEndControllerConfiguration
    extends AbstractGraniteFrontEndControllerConfiguration<IExtendedFrontEndControllerService>
{
    private final IUserQueryServiceClient userQueryClient;
    private final ApplicationRoles        roles;

    public
    ExtendedFrontEndControllerConfiguration()
    {
        this(defaultQueryClient(),null);
    }

    public
    ExtendedFrontEndControllerConfiguration(IUserQueryServiceClient userQueryClient,ApplicationRoles roles)
    {
        this.userQueryClient = userQueryClient;
        this.roles = roles;
    }

    private static IUserQueryServiceClient
    defaultQueryClient()
    {
        IUserQueryServiceClient client = Mockito.mock(IUserQueryServiceClient.class);

        Mockito
            .when(client.findAllUsers(Mockito.any(FindAllUsersRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new FindAllUsersReply().setSuccess(true).setUsers(List.of())));

        return client;
    }

    @Override
    public ApplicationRoles
    applicationRoles()
    {
        return roles == null ? super.applicationRoles() : roles;
    }

    @Override
    protected List<PreferenceDefinition>
    preferenceDefinitions()
    {
        List<PreferenceDefinition> definitions = new ArrayList<>(super.preferenceDefinitions());

        definitions.add(PreferenceDefinition.bool("echo.loud",false));

        return definitions;
    }

    @Override
    protected IExtendedFrontEndControllerService
    createService(GraniteFrontEndControllerContext context)
    {
        return new ExtendedFrontEndControllerService(context);
    }

    @Bean
    @RequestScoped
    @Named("Granite")
    public IExtendedFrontEndControllerService
    frontEndControllerService(
        @Named("FrontEndController")
        Executor         executor,
        ModelMapper      mapper,
        ApplicationRoles roles)
    {
        return createService(createContext(mapper,executor,roles));
    }

    @Bean
    @RequestScoped
    @Named("Granite")
    public ExtendedFrontEndControllerServiceController
    frontEndControllerServiceController(
        @Named("Granite")
        IExtendedFrontEndControllerService service)
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
