//////////////////////////////////////////////////////////////////////////////
// UserRequestProcessorTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.user;

import granite.frontendcontroller.service.requestreply.UsersInput;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.CreateUsersReply;
import granite.user.service.requestreply.CreateUsersRequest;
import granite.user.service.requestreply.DestroyUsersReply;
import granite.user.service.requestreply.DestroyUsersRequest;
import granite.user.service.requestreply.FindAllUsersReply;
import granite.user.service.requestreply.FindAllUsersRequest;
import granite.user.service.requestreply.UpdateUserData;
import granite.user.service.requestreply.UpdateUsersReply;
import granite.user.service.requestreply.UpdateUsersRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class UserRequestProcessorTest
{
    private IUserQueryServiceClient   queryClient;
    private IUserCommandServiceClient commandClient;
    private UserRequestProcessor      processor;

    @BeforeEach
    public void
    setUp()
    {
        queryClient = Mockito.mock(IUserQueryServiceClient.class);
        commandClient = Mockito.mock(IUserCommandServiceClient.class);
        processor = new UserRequestProcessor(queryClient,commandClient);

        Mockito
            .when(queryClient.findAllUsers(Mockito.any(FindAllUsersRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindAllUsersReply().setSuccess(true).setUsers(List.of())));
        Mockito
            .when(commandClient.createUsers(Mockito.any(CreateUsersRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new CreateUsersReply().setSuccess(true)));
        Mockito
            .when(commandClient.updateUsers(Mockito.any(UpdateUsersRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new UpdateUsersReply().setSuccess(true)));
        Mockito
            .when(commandClient.destroyUsers(Mockito.any(DestroyUsersRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new DestroyUsersReply().setSuccess(true)));
    }

    @Test
    public void
    routesLoadSaveDelete()
        throws Exception
    {
        processor
            .process(new GetOutputRequest().setSource("User.All").setDestination("UsersEditableView"))
            .toCompletableFuture()
            .get();
        Mockito.verify(queryClient,Mockito.times(1)).findAllUsers(Mockito.any(FindAllUsersRequest.class));

        processor
            .process(
                new HandleInputRequest()
                    .setInput(
                        new UsersInput()
                            .setUsersToUpdate(List.of(new UpdateUserData().setUserId(2L)))
                            .setSource("User.All")
                            .setCommand("Save")
                            .setDestination("UsersEditableView")))
            .toCompletableFuture()
            .get();
        Mockito.verify(commandClient).updateUsers(Mockito.any(UpdateUsersRequest.class));

        processor
            .process(
                new HandleInputRequest()
                    .setInput(
                        new UsersInput()
                            .setUserId(1L)
                            .setUserIdsToDelete(List.of(2L))
                            .setSource("User.All")
                            .setCommand("Delete")
                            .setDestination("UsersEditableView")))
            .toCompletableFuture()
            .get();
        Mockito.verify(commandClient).destroyUsers(Mockito.any(DestroyUsersRequest.class));
    }

    @Test
    public void
    unknownKeyFails()
    {
        ExecutionException e =
            assertThrows(
                ExecutionException.class,
                () ->
                    processor
                        .process(new GetOutputRequest().setSource("Nope").setDestination("Nothing"))
                        .toCompletableFuture()
                        .get());

        assertInstanceOf(IllegalArgumentException.class,e.getCause());
        assertTrue(e.getCause().getMessage().contains("No action found for key"));
    }
}

//////////////////////////////////////////////////////////////////////////////
