//////////////////////////////////////////////////////////////////////////////
// ExtendedFrontEndControllerConfigurationTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.foundation.core.security.RoleDefinition;
import granite.frontendcontroller.service.requestreply.GetPreferencesOutputRequest;
import granite.frontendcontroller.service.requestreply.GetSessionOutputRequest;
import granite.frontendcontroller.service.requestreply.GetUsersOutputReply;
import granite.frontendcontroller.service.requestreply.GetUsersOutputRequest;
import granite.frontendcontroller.service.requestreply.PreferenceDefinitionData;
import granite.frontendcontroller.service.requestreply.PreferencesOutput;
import granite.frontendcontroller.service.requestreply.RoleData;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindAllUsersReply;
import granite.user.service.requestreply.FindAllUsersRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.RequestContextHolder;
import strata.foundation.core.configuration.IConfiguration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class ExtendedFrontEndControllerConfigurationTest
{
    @AfterEach
    public void
    tearDown()
    {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void
    extendedServiceInheritsGraniteOperations()
    {
        ExtendedFrontEndControllerConfiguration subject = configuration(null);
        IExtendedFrontEndControllerService      service = service(subject);
        Object                                  controller = subject.frontEndControllerServiceController(service);

        assertInstanceOf(ExtendedFrontEndControllerServiceController.class,controller);

        List<String> paths = new ArrayList<>();

        for (Method method : controller.getClass().getMethods())
        {
            PostMapping mapping = method.getAnnotation(PostMapping.class);

            if (mapping != null)
                paths.addAll(List.of(mapping.value()));
        }

        assertTrue(
            paths.containsAll(
                List.of(
                    "/get-session-output",
                    "/handle-session-input",
                    "/get-users-output",
                    "/handle-users-input",
                    "/get-echo-output")),
            "paths: " + paths);
        assertEquals(
            "echo",
            service.getEchoOutput(new GetSessionOutputRequest()).toCompletableFuture().join().getOutput().getUserName());
    }

    @Test
    public void
    extendedConfigurationOverridesCatalog()
    {
        ApplicationRolesManager roles =
            new ApplicationRolesManager(
                List.of(
                    new RoleDefinition("ROLE_CLERK","Clerk",Set.of()),
                    new RoleDefinition("ROLE_MANAGER","Manager",Set.of("ROLE_CLERK"))),
                "ROLE_CLERK");

        GetUsersOutputReply reply = usersOutput(configuration(roles));

        assertEquals(List.of("ROLE_ADMIN","ROLE_CLERK","ROLE_MANAGER"),names(reply));
        assertEquals(List.of("ROLE_CLERK"),reply.getOutput().getRoles().get(2).getImpliedRoles());
        assertEquals("Manager",reply.getOutput().getRoles().get(2).getLabel());
    }

    @Test
    public void
    extendedConfigurationAddsDefinition()
    {
        PreferencesOutput output =
            service(configuration(null))
                .getPreferencesOutput(new GetPreferencesOutputRequest().setSource("Preferences.Current").setDestination("PreferencesView"))
                .toCompletableFuture()
                .join()
                .getOutput();

        List<String> keys = new ArrayList<>();

        for (PreferenceDefinitionData definition : output.getDefinitions())
            keys.add(definition.getKey());

        assertEquals(List.of("granite.theme","echo.loud"),keys);
    }

    @Test
    public void
    graniteUsersOutputListsGraniteCatalog()
    {
        assertEquals(List.of("ROLE_ADMIN","ROLE_AUDITOR","ROLE_USER"),names(usersOutput(configuration(null))));
    }

    private ExtendedFrontEndControllerConfiguration
    configuration(ApplicationRolesManager roles)
    {
        IUserQueryServiceClient queryClient = Mockito.mock(IUserQueryServiceClient.class);

        Mockito
            .when(queryClient.findAllUsers(Mockito.any(FindAllUsersRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new FindAllUsersReply().setSuccess(true).setUsers(List.of())));

        ExtendedFrontEndControllerConfiguration subject       = new ExtendedFrontEndControllerConfiguration(queryClient,roles);
        IConfiguration                          configuration = Mockito.mock(IConfiguration.class);

        Mockito.when(configuration.getProperty(Mockito.anyString(),Mockito.anyString())).thenAnswer(i -> i.getArgument(1));
        ReflectionTestUtils.setField(subject,"configuration",configuration);

        return subject;
    }

    private IExtendedFrontEndControllerService
    service(ExtendedFrontEndControllerConfiguration subject)
    {
        return subject.frontEndControllerService(Runnable::run,new ModelMapper(),subject.applicationRoles());
    }

    private GetUsersOutputReply
    usersOutput(ExtendedFrontEndControllerConfiguration subject)
    {
        return
            service(subject)
                .getUsersOutput(new GetUsersOutputRequest().setSource("User.All").setDestination("UsersEditableView"))
                .toCompletableFuture()
                .join();
    }

    private List<String>
    names(GetUsersOutputReply reply)
    {
        List<String> names = new ArrayList<>();

        for (RoleData role : reply.getOutput().getRoles())
            names.add(role.getName());

        return names;
    }
}

//////////////////////////////////////////////////////////////////////////////
