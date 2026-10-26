//////////////////////////////////////////////////////////////////////////////
// DeleteUsersViewTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.user;

import granite.frontendcontroller.service.requestreply.UsersInput;
import granite.frontendcontroller.service.requestreply.UsersOutput;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.DestroyUsersReply;
import granite.user.service.requestreply.DestroyUsersRequest;
import granite.user.service.requestreply.FindAllUsersReply;
import granite.user.service.requestreply.FindAllUsersRequest;
import granite.user.service.requestreply.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class DeleteUsersViewTest
{
    private IUserQueryServiceClient   queryClient;
    private IUserCommandServiceClient commandClient;
    private DeleteUsersView           view;

    @BeforeEach
    public void
    setUp()
    {
        queryClient = Mockito.mock(IUserQueryServiceClient.class);
        commandClient = Mockito.mock(IUserCommandServiceClient.class);
        view = new DeleteUsersView(queryClient,commandClient);

        Mockito
            .when(queryClient.findAllUsers(Mockito.any(FindAllUsersRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindAllUsersReply()
                        .setSuccess(true)
                        .setUsers(List.of(new UserData().setUserId(7L)))));
        Mockito
            .when(commandClient.destroyUsers(Mockito.any(DestroyUsersRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new DestroyUsersReply().setSuccess(true)));
    }

    @Test
    public void
    refusesCallersOwnId()
        throws Exception
    {
        UsersOutput output = run(new UsersInput().setUserId(7L).setUserIdsToDelete(List.of(7L,8L)));

        Mockito.verify(commandClient).destroyUsers(Mockito.argThat(r -> r.getUserIds().equals(Set.of(8L))));
        assertEquals(1,output.getFailureMessages().size());
        assertTrue(output.getFailureMessages().get(0).contains("7"));
        assertEquals(1,output.getUsers().size());
    }

    @Test
    public void
    reportsFailedUserIds()
        throws Exception
    {
        Mockito
            .when(commandClient.destroyUsers(Mockito.any(DestroyUsersRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new DestroyUsersReply().setSuccess(true).setFailedUserIds(Set.of(9L))));

        UsersOutput output = run(new UsersInput().setUserId(1L).setUserIdsToDelete(List.of(9L)));

        assertEquals(1,output.getFailureMessages().size());
        assertTrue(output.getFailureMessages().get(0).contains("9"));
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

        UsersOutput output = run(new UsersInput().setUserId(1L).setUserIdsToDelete(List.of(9L)));

        assertTrue(output.getUsers().isEmpty());
        assertEquals(List.of("Unable to refresh user list: db down"),output.getFailureMessages());
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
                        .setCommand("Delete")
                        .setDestination("UsersEditableView"))
                .toCompletableFuture()
                .get();
    }
}

//////////////////////////////////////////////////////////////////////////////
