//////////////////////////////////////////////////////////////////////////////
// GraniteFrontEndControllerServiceUsersTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.application;

import granite.frontendcontroller.server.control.session.SessionLoginSettings;
import granite.frontendcontroller.server.control.session.SessionTestSupport;
import granite.frontendcontroller.service.requestreply.GetUsersOutputReply;
import granite.frontendcontroller.service.requestreply.GetUsersOutputRequest;
import granite.frontendcontroller.service.requestreply.HandleUsersInputReply;
import granite.frontendcontroller.service.requestreply.HandleUsersInputRequest;
import granite.frontendcontroller.service.requestreply.UsersInput;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.client.requestreply.ISessionQueryServiceClient;
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
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class GraniteFrontEndControllerServiceUsersTest
{
    private IUserQueryServiceClient          queryClient;
    private IUserCommandServiceClient        commandClient;
    private GraniteFrontEndControllerService service;

    @BeforeEach
    public void
    setUp()
    {
        queryClient = Mockito.mock(IUserQueryServiceClient.class);
        commandClient = Mockito.mock(IUserCommandServiceClient.class);
        service =
            new GraniteFrontEndControllerService(
                new ModelMapper(),
                Runnable::run,
                queryClient,
                commandClient,
                Mockito.mock(ISessionCommandServiceClient.class),
                Mockito.mock(ISessionQueryServiceClient.class),
                SessionTestSupport.context(null,null,null),
                new SessionLoginSettings(true,false));

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
    dispatchesLoadSaveDelete()
        throws Exception
    {
        GetUsersOutputReply load =
            service
                .getUsersOutput(new GetUsersOutputRequest().setSource("User.All").setDestination("UsersEditableView"))
                .toCompletableFuture()
                .get();

        assertTrue(load.isSuccess());
        assertEquals("User.All",load.getOutput().getSource());
        Mockito.verify(queryClient,Mockito.times(1)).findAllUsers(Mockito.any(FindAllUsersRequest.class));

        HandleUsersInputReply save =
            service
                .handleUsersInput(
                    new HandleUsersInputRequest()
                        .setInput(
                            new UsersInput()
                                .setUsersToUpdate(List.of(new UpdateUserData().setUserId(2L)))
                                .setSource("User.All")
                                .setCommand("Save")
                                .setDestination("UsersEditableView")))
                .toCompletableFuture()
                .get();

        assertTrue(save.isSuccess());
        Mockito.verify(commandClient).updateUsers(Mockito.any(UpdateUsersRequest.class));

        HandleUsersInputReply delete =
            service
                .handleUsersInput(
                    new HandleUsersInputRequest()
                        .setInput(
                            new UsersInput()
                                .setUserId(1L)
                                .setUserIdsToDelete(List.of(2L))
                                .setSource("User.All")
                                .setCommand("Delete")
                                .setDestination("UsersEditableView")))
                .toCompletableFuture()
                .get();

        assertTrue(delete.isSuccess());
        Mockito.verify(commandClient).destroyUsers(Mockito.any(DestroyUsersRequest.class));
    }

    @Test
    public void
    unknownOutputKeyFailsTheReply()
        throws Exception
    {
        GetUsersOutputReply reply =
            service
                .getUsersOutput(new GetUsersOutputRequest().setSource("Nope").setDestination("Nothing"))
                .toCompletableFuture()
                .get();

        assertFalse(reply.isSuccess());
        assertEquals("java.lang.IllegalArgumentException",reply.getException().getCause().getExceptionType());
        assertEquals("No action found for key: Nope->Nothing",reply.getException().getCause().getExceptionMessage());
    }

    @Test
    public void
    unknownInputKeyFailsTheReply()
        throws Exception
    {
        HandleUsersInputReply reply =
            service
                .handleUsersInput(
                    new HandleUsersInputRequest()
                        .setInput(new UsersInput().setSource("User.All").setCommand("Unknown").setDestination("UsersEditableView")))
                .toCompletableFuture()
                .get();

        assertFalse(reply.isSuccess());
        assertEquals("java.lang.IllegalArgumentException",reply.getException().getCause().getExceptionType());
        assertEquals("No action found for key: User.All->Unknown->UsersEditableView",reply.getException().getCause().getExceptionMessage());
        Mockito.verifyNoInteractions(commandClient);
    }
}

//////////////////////////////////////////////////////////////////////////////
