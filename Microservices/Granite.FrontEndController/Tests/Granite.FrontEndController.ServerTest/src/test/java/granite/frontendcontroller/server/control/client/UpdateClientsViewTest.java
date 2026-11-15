//////////////////////////////////////////////////////////////////////////////
// UpdateClientsViewTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.client;

import granite.client.client.requestreply.IClientCommandServiceClient;
import granite.client.client.requestreply.IClientQueryServiceClient;
import granite.client.service.requestreply.ClientData;
import granite.client.service.requestreply.CreateClientData;
import granite.client.service.requestreply.CreateClientReply;
import granite.client.service.requestreply.CreateClientRequest;
import granite.client.service.requestreply.FindAllClientsRequest;
import granite.client.service.requestreply.FindClientsReply;
import granite.client.service.requestreply.UpdateClientData;
import granite.client.service.requestreply.UpdateClientsRequest;
import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.frontendcontroller.server.application.GraniteClientServiceClients;
import granite.frontendcontroller.server.control.session.SessionTestSupport;
import granite.frontendcontroller.service.requestreply.ClientsInput;
import granite.frontendcontroller.service.requestreply.ClientsOutput;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindAllUsersReply;
import granite.user.service.requestreply.FindAllUsersRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class UpdateClientsViewTest
{
    @Test
    public void
    failingUpdateStageKeepsTheCreatedClientSecret()
        throws Exception
    {
        IClientCommandServiceClient command = Mockito.mock(IClientCommandServiceClient.class);
        IClientQueryServiceClient   query   = Mockito.mock(IClientQueryServiceClient.class);
        IUserQueryServiceClient     users   = Mockito.mock(IUserQueryServiceClient.class);

        Mockito
            .when(command.createClient(Mockito.any(CreateClientRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new CreateClientReply()
                        .setSuccess(true)
                        .setClient(new ClientData().setClientId("gc_new"))
                        .setClientSecret("the-secret")));
        Mockito
            .when(command.updateClients(Mockito.any(UpdateClientsRequest.class)))
            .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("update down")));
        Mockito
            .when(query.findAllClients(Mockito.any(FindAllClientsRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new FindClientsReply().setSuccess(true).setClients(List.of())));
        Mockito
            .when(users.findAllUsers(Mockito.any(FindAllUsersRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new FindAllUsersReply().setSuccess(true).setUsers(List.of())));

        GraniteClientServiceClients clients =
            new GraniteClientServiceClients()
                .setClientCommandClient(() -> command)
                .setClientQueryClient(() -> query);

        ClientsOutput output =
            new UpdateClientsView(
                clients,
                new ClientsLoader(clients,users,new GraniteApplicationRolesManagerSupplier().get()),
                SessionTestSupport.context("d",11L,1L))
                .apply(
                    new ClientsInput()
                        .setSource("Client.All")
                        .setCommand("Save")
                        .setDestination("ClientsEditableView")
                        .setClientsToCreate(List.of(new CreateClientData().setName("n")))
                        .setClientsToUpdate(List.of(new UpdateClientData().setClientId("gc_x"))))
                .toCompletableFuture()
                .get();

        assertEquals("gc_new",output.getRevealedClientId());
        assertEquals("the-secret",output.getRevealedClientSecret());
        assertFalse(output.getFailureMessages().isEmpty());
        assertTrue(output.getFailureMessages().stream().anyMatch(message -> message.contains("update down")));
        assertFalse(output.getFailureMessages().stream().anyMatch(message -> message.contains("the-secret")));
    }
}

//////////////////////////////////////////////////////////////////////////////
