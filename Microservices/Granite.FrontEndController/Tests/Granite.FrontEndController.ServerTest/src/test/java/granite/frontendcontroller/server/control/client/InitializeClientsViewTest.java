//////////////////////////////////////////////////////////////////////////////
// InitializeClientsViewTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.client;

import granite.client.client.requestreply.IClientQueryServiceClient;
import granite.client.service.requestreply.ClientData;
import granite.client.service.requestreply.FindAllClientsRequest;
import granite.client.service.requestreply.FindClientsReply;
import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.frontendcontroller.server.application.GraniteClientServiceClients;
import granite.frontendcontroller.server.control.session.SessionTestSupport;
import granite.frontendcontroller.service.requestreply.ClientsOutput;
import granite.frontendcontroller.service.requestreply.GetClientsOutputRequest;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class InitializeClientsViewTest
{
    @Test
    public void
    clientsViewListsClientsRolesWithoutAdminAndUsers()
        throws Exception
    {
        IClientQueryServiceClient clientQuery = Mockito.mock(IClientQueryServiceClient.class);
        IUserQueryServiceClient   userQuery   = Mockito.mock(IUserQueryServiceClient.class);

        Mockito
            .when(clientQuery.findAllClients(Mockito.any(FindAllClientsRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindClientsReply()
                        .setSuccess(true)
                        .setClients(List.of(new ClientData().setClientId("gc_a"),new ClientData().setClientId("gc_b")))));
        Mockito
            .when(userQuery.findAllUsers(Mockito.any(FindAllUsersRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindAllUsersReply()
                        .setSuccess(true)
                        .setUsers(
                            List.of(
                                new UserData().setUserId(5L).setUserName("alice"),
                                new UserData().setUserId(6L).setUserName("bob")))));

        ClientsOutput output =
            new InitializeClientsView(
                new GraniteClientServiceClients().setClientQueryClient(() -> clientQuery),
                userQuery,
                SessionTestSupport.context("d",11L,1L),
                new GraniteApplicationRolesManagerSupplier().get())
                .apply(new GetClientsOutputRequest().setSource("Client.All").setDestination("ClientsEditableView"))
                .toCompletableFuture()
                .get();

        assertEquals(List.of("gc_a","gc_b"),output.getClients().stream().map(client -> client.getClientId()).toList());
        assertFalse(output.getRoles().isEmpty());
        assertFalse(output.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN")));
        assertTrue(output.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_USER")));
        assertEquals(List.of(5L,6L),output.getUsers().stream().map(user -> user.getUserId()).toList());
        assertEquals(List.of("alice","bob"),output.getUsers().stream().map(user -> user.getUserName()).toList());
        assertTrue(output.getFailureMessages().isEmpty());
        assertNull(output.getRevealedClientId());
        assertNull(output.getRevealedClientSecret());
        assertEquals("Client.All",output.getSource());
        assertEquals("ClientsEditableView",output.getDestination());
        assertEquals(1L,output.getUserId());
    }

    @Test
    public void
    unauthenticatedCallerGetsFailureWithoutClientCall()
        throws Exception
    {
        IClientQueryServiceClient clientQuery = Mockito.mock(IClientQueryServiceClient.class);
        IUserQueryServiceClient   userQuery   = Mockito.mock(IUserQueryServiceClient.class);

        ClientsOutput output =
            new InitializeClientsView(
                new GraniteClientServiceClients().setClientQueryClient(() -> clientQuery),
                userQuery,
                SessionTestSupport.context("d",null,null),
                new GraniteApplicationRolesManagerSupplier().get())
                .apply(new GetClientsOutputRequest().setSource("Client.All").setDestination("ClientsEditableView"))
                .toCompletableFuture()
                .get();

        assertEquals(List.of("not authenticated"),output.getFailureMessages());
        assertTrue(output.getClients().isEmpty());
        assertEquals("ClientsEditableView",output.getDestination());
        Mockito.verifyNoInteractions(clientQuery);
        Mockito.verifyNoInteractions(userQuery);
    }
}

//////////////////////////////////////////////////////////////////////////////
