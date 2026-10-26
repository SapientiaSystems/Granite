//////////////////////////////////////////////////////////////////////////////
// InitializeUsersViewTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.user;

import granite.frontendcontroller.service.requestreply.GetUsersOutputRequest;
import granite.frontendcontroller.service.requestreply.UsersOutput;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindAllUsersReply;
import granite.user.service.requestreply.FindAllUsersRequest;
import granite.user.service.requestreply.UserData;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class InitializeUsersViewTest
{
    @Test
    public void
    returnsAllUsers()
        throws Exception
    {
        IUserQueryServiceClient queryClient = Mockito.mock(IUserQueryServiceClient.class);
        Mockito
            .when(queryClient.findAllUsers(Mockito.any(FindAllUsersRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindAllUsersReply()
                        .setSuccess(true)
                        .setUsers(List.of(new UserData().setUserId(1L),new UserData().setUserId(2L)))));

        UsersOutput output =
            new InitializeUsersView(queryClient)
                .apply(
                    new GetUsersOutputRequest()
                        .setSource("User.All")
                        .setDestination("UsersEditableView"))
                .toCompletableFuture()
                .get();

        assertEquals(2,output.getUsers().size());
        assertTrue(output.getFailureMessages().isEmpty());
        assertEquals("User.All",output.getSource());
        assertEquals("UsersEditableView",output.getDestination());
    }

    @Test
    public void
    failedQueryReportsMessage()
        throws Exception
    {
        IUserQueryServiceClient queryClient = Mockito.mock(IUserQueryServiceClient.class);
        Mockito
            .when(queryClient.findAllUsers(Mockito.any(FindAllUsersRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(new FindAllUsersReply().setSuccess(false)));

        UsersOutput output =
            new InitializeUsersView(queryClient)
                .apply(new GetUsersOutputRequest().setSource("User.All").setDestination("UsersEditableView"))
                .toCompletableFuture()
                .get();

        assertTrue(output.getUsers().isEmpty());
        assertEquals(List.of("Unable to refresh user list: unknown error"),output.getFailureMessages());
    }
}

//////////////////////////////////////////////////////////////////////////////
