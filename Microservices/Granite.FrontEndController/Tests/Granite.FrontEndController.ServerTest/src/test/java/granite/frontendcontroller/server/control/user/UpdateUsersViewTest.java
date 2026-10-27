//////////////////////////////////////////////////////////////////////////////
// UpdateUsersViewTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.user;

import granite.foundation.core.security.GraniteApplicationRoles;
import granite.frontendcontroller.service.requestreply.UsersInput;
import granite.frontendcontroller.service.requestreply.UsersOutput;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.CreateUserData;
import granite.user.service.requestreply.CreateUsersReply;
import granite.user.service.requestreply.CreateUsersRequest;
import granite.user.service.requestreply.FindAllUsersReply;
import granite.user.service.requestreply.FindAllUsersRequest;
import granite.user.service.requestreply.UpdateUserData;
import granite.user.service.requestreply.UpdateUsersReply;
import granite.user.service.requestreply.UpdateUsersRequest;
import granite.user.service.requestreply.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class UpdateUsersViewTest
{
    private IUserQueryServiceClient   queryClient;
    private IUserCommandServiceClient commandClient;
    private UpdateUsersView           view;

    @BeforeEach
    public void
    setUp()
    {
        queryClient = Mockito.mock(IUserQueryServiceClient.class);
        commandClient = Mockito.mock(IUserCommandServiceClient.class);
        view = new UpdateUsersView(queryClient,commandClient,GraniteApplicationRoles.create());

        Mockito
            .when(queryClient.findAllUsers(Mockito.any(FindAllUsersRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindAllUsersReply()
                        .setSuccess(true)
                        .setUsers(List.of(new UserData().setUserId(1L),new UserData().setUserId(2L)))));
        Mockito
            .when(commandClient.createUsers(Mockito.any(CreateUsersRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new CreateUsersReply().setSuccess(true)));
        Mockito
            .when(commandClient.updateUsers(Mockito.any(UpdateUsersRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new UpdateUsersReply().setSuccess(true)));
    }

    @Test
    public void
    createsThenUpdatesThenRequeries()
        throws Exception
    {
        List<CreateUserData> creates = List.of(new CreateUserData().setUserName("new"));
        List<UpdateUserData> updates = List.of(new UpdateUserData().setUserId(2L).setUserName("renamed"));

        UsersOutput output = run(new UsersInput().setUsersToCreate(creates).setUsersToUpdate(updates));

        assertEquals(3,output.getRoles().size());

        InOrder order = Mockito.inOrder(commandClient,queryClient);
        order
            .verify(commandClient)
            .createUsers(Mockito.argThat(r -> r.getUsersToCreate().equals(creates)));
        order
            .verify(commandClient)
            .updateUsers(Mockito.argThat(r -> r.getUsersToUpdate().equals(updates)));
        order.verify(queryClient).findAllUsers(Mockito.any(FindAllUsersRequest.class));
        assertEquals(2,output.getUsers().size());
        assertTrue(output.getFailureMessages().isEmpty());
    }

    @Test
    public void
    skipsEmptyBatches()
        throws Exception
    {
        run(new UsersInput().setUsersToUpdate(List.of(new UpdateUserData().setUserId(2L))));

        Mockito.verify(commandClient,Mockito.never()).createUsers(Mockito.any(CreateUsersRequest.class));
        Mockito.verify(commandClient).updateUsers(Mockito.any(UpdateUsersRequest.class));
    }

    @Test
    public void
    reportsFailedUsers()
        throws Exception
    {
        Mockito
            .when(commandClient.createUsers(Mockito.any(CreateUsersRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new CreateUsersReply()
                        .setSuccess(true)
                        .setFailedUsers(List.of(new CreateUserData().setUserName("dupe")))));

        UsersOutput output =
            run(new UsersInput().setUsersToCreate(List.of(new CreateUserData().setUserName("dupe"))));

        assertEquals(1,output.getFailureMessages().size());
        assertTrue(output.getFailureMessages().get(0).contains("dupe"));
    }

    @Test
    public void
    updateFailureAfterCreatesReturnsRefreshedListAndMessages()
        throws Exception
    {
        Mockito
            .when(commandClient.updateUsers(Mockito.any(UpdateUsersRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new UpdateUsersReply().setSuccess(false).setFailureMessage("boom")));

        UsersOutput output =
            run(
                new UsersInput()
                    .setUsersToCreate(List.of(new CreateUserData().setUserName("new")))
                    .setUsersToUpdate(List.of(new UpdateUserData().setUserId(2L))));

        assertEquals(2,output.getUsers().size());
        assertEquals(List.of("boom"),output.getFailureMessages());
    }

    @Test
    public void
    failedRequeryReportsMessage()
        throws Exception
    {
        Mockito
            .when(queryClient.findAllUsers(Mockito.any(FindAllUsersRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindAllUsersReply().setSuccess(false).setFailureMessage("db down")));

        UsersOutput output = run(new UsersInput().setUsersToUpdate(List.of(new UpdateUserData().setUserId(2L))));

        assertTrue(output.getUsers().isEmpty());
        assertEquals(List.of("Unable to refresh user list: db down"),output.getFailureMessages());
    }

    @Test
    public void
    adminSavePreservesReservedKeys()
        throws Exception
    {
        Map<String,Object> storedValues = new HashMap<>();

        storedValues.put("preferences.granite.theme","dark");
        storedValues.put("granite.rolesAssigned",true);
        storedValues.put("note","old");
        Mockito
            .when(queryClient.findAllUsers(Mockito.any(FindAllUsersRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindAllUsersReply()
                        .setSuccess(true)
                        .setUsers(List.of(new UserData().setUserId(1L),new UserData().setUserId(2L).setValues(storedValues)))));

        Map<String,Object> incoming = new HashMap<>();

        incoming.put("preferences.granite.theme","light");
        incoming.put("preferences.extra","x");
        incoming.put("granite.rolesAssigned",false);
        incoming.put("granite.other","y");
        incoming.put("note","new");

        run(new UsersInput().setUsersToUpdate(List.of(new UpdateUserData().setUserId(2L).setUserName("renamed").setValues(incoming))));

        ArgumentCaptor<UpdateUsersRequest> captor = ArgumentCaptor.forClass(UpdateUsersRequest.class);

        Mockito.verify(commandClient).updateUsers(captor.capture());

        UpdateUserData sent = captor.getValue().getUsersToUpdate().get(0);

        assertEquals(
            Map.of("preferences.granite.theme","dark","granite.rolesAssigned",true,"note","new"),
            sent.getValues().get());
        assertEquals(2L,sent.getUserId());
        assertEquals("renamed",sent.getUserName().get());
    }

    @Test
    public void
    updateWithoutValuesQueriesUsersOnlyForTheRefresh()
        throws Exception
    {
        run(new UsersInput().setUsersToUpdate(List.of(new UpdateUserData().setUserId(2L))));

        Mockito.verify(queryClient,Mockito.times(1)).findAllUsers(Mockito.any(FindAllUsersRequest.class));
    }

    private UsersOutput
    run(UsersInput input)
        throws Exception
    {
        return
            view
                .apply(
                    input
                        .setSource("User.All")
                        .setCommand("Save")
                        .setDestination("UsersEditableView"))
                .toCompletableFuture()
                .get();
    }
}

//////////////////////////////////////////////////////////////////////////////
