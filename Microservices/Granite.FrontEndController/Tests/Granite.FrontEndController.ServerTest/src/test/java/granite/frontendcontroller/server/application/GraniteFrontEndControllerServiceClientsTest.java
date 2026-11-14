//////////////////////////////////////////////////////////////////////////////
// GraniteFrontEndControllerServiceClientsTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.application;

import granite.client.client.requestreply.IClientCommandServiceClient;
import granite.client.client.requestreply.IClientQueryServiceClient;
import granite.client.service.requestreply.ClientData;
import granite.client.service.requestreply.CreateClientData;
import granite.client.service.requestreply.CreateClientReply;
import granite.client.service.requestreply.CreateClientRequest;
import granite.client.service.requestreply.DestroyClientsReply;
import granite.client.service.requestreply.DestroyClientsRequest;
import granite.client.service.requestreply.FindAllClientsRequest;
import granite.client.service.requestreply.FindClientsByOwnerRequest;
import granite.client.service.requestreply.FindClientsReply;
import granite.client.service.requestreply.RotateClientSecretReply;
import granite.client.service.requestreply.RotateClientSecretRequest;
import granite.client.service.requestreply.RotateOwnedClientSecretRequest;
import granite.client.service.requestreply.UpdateClientData;
import granite.client.service.requestreply.UpdateClientsReply;
import granite.client.service.requestreply.UpdateClientsRequest;
import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.frontendcontroller.server.control.session.SessionLoginSettings;
import granite.frontendcontroller.server.control.session.SessionTestSupport;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.ClientsInput;
import granite.frontendcontroller.service.requestreply.ClientsOutput;
import granite.frontendcontroller.service.requestreply.GetClientsOutputReply;
import granite.frontendcontroller.service.requestreply.GetClientsOutputRequest;
import granite.frontendcontroller.service.requestreply.GetOwnedClientsOutputReply;
import granite.frontendcontroller.service.requestreply.GetOwnedClientsOutputRequest;
import granite.frontendcontroller.service.requestreply.HandleClientsInputReply;
import granite.frontendcontroller.service.requestreply.HandleClientsInputRequest;
import granite.frontendcontroller.service.requestreply.HandleOwnedClientSecretInputReply;
import granite.frontendcontroller.service.requestreply.HandleOwnedClientSecretInputRequest;
import granite.frontendcontroller.service.requestreply.OwnedClientSecretInput;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindAllUsersReply;
import granite.user.service.requestreply.FindAllUsersRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import strata.foundation.core.mapper.ObjectMapperSupplier;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class GraniteFrontEndControllerServiceClientsTest
{
    private static final String SECRET = "revealed-secret-value";

    private IClientCommandServiceClient clientCommand;
    private IClientQueryServiceClient   clientQuery;
    private IUserQueryServiceClient     userQuery;

    @BeforeEach
    public void
    setUp()
    {
        clientCommand = Mockito.mock(IClientCommandServiceClient.class);
        clientQuery   = Mockito.mock(IClientQueryServiceClient.class);
        userQuery     = Mockito.mock(IUserQueryServiceClient.class);

        Mockito
            .when(clientQuery.findAllClients(Mockito.any(FindAllClientsRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindClientsReply().setSuccess(true).setClients(List.of(new ClientData().setClientId("gc_a")))));
        Mockito
            .when(clientQuery.findClientsByOwner(Mockito.any(FindClientsByOwnerRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new FindClientsReply().setSuccess(true).setClients(List.of(new ClientData().setClientId("gc_own")))));
        Mockito
            .when(userQuery.findAllUsers(Mockito.any(FindAllUsersRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new FindAllUsersReply().setSuccess(true).setUsers(List.of())));
        Mockito
            .when(clientCommand.createClient(Mockito.any(CreateClientRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new CreateClientReply()
                        .setSuccess(true)
                        .setClient(new ClientData().setClientId("gc_new"))
                        .setClientSecret(SECRET)));
        Mockito
            .when(clientCommand.updateClients(Mockito.any(UpdateClientsRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new UpdateClientsReply().setSuccess(true)));
        Mockito
            .when(clientCommand.destroyClients(Mockito.any(DestroyClientsRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new DestroyClientsReply().setSuccess(true)));
        Mockito
            .when(clientCommand.rotateClientSecret(Mockito.any(RotateClientSecretRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new RotateClientSecretReply()
                        .setSuccess(true)
                        .setClient(new ClientData().setClientId("gc_a"))
                        .setClientSecret(SECRET)));
        Mockito
            .when(clientCommand.rotateOwnedClientSecret(Mockito.any(RotateOwnedClientSecretRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new RotateClientSecretReply()
                        .setSuccess(true)
                        .setClient(new ClientData().setClientId("gc_own"))
                        .setClientSecret(SECRET)));
    }

    private HandleClientsInputReply
    save(GraniteFrontEndControllerService service,ClientsInput input)
        throws Exception
    {
        return service.handleClientsInput(new HandleClientsInputRequest().setInput(input)).toCompletableFuture().get();
    }

    private ClientsInput
    input(String command)
    {
        return new ClientsInput().setSource("Client.All").setCommand(command).setDestination("ClientsEditableView");
    }

    private GetClientsOutputReply
    load(GraniteFrontEndControllerService service)
        throws Exception
    {
        return
            service
                .getClientsOutput(new GetClientsOutputRequest().setSource("Client.All").setDestination("ClientsEditableView"))
                .toCompletableFuture()
                .get();
    }

    @Test
    public void
    createRevealsSecretOnce()
        throws Exception
    {
        GraniteFrontEndControllerService service = createService(SessionTestSupport.context("d",11L,1L));

        HandleClientsInputReply saved =
            save(service,input("Save").setClientsToCreate(List.of(new CreateClientData().setName("n"))));

        assertTrue(saved.isSuccess());
        assertEquals("gc_new",saved.getOutput().getRevealedClientId());
        assertEquals(SECRET,saved.getOutput().getRevealedClientSecret());
        assertTrue(saved.getOutput().getFailureMessages().isEmpty());
        assertEquals("Client.All",saved.getOutput().getSource());
        assertEquals("Save",saved.getOutput().getCommand().orElse(null));
        assertEquals("ClientsEditableView",saved.getOutput().getDestination());

        GetClientsOutputReply loaded = load(service);

        assertNull(loaded.getOutput().getRevealedClientId());
        assertNull(loaded.getOutput().getRevealedClientSecret());
    }

    @Test
    public void
    updateWithoutCreateRevealsNothing()
        throws Exception
    {
        GraniteFrontEndControllerService service = createService(SessionTestSupport.context("d",11L,1L));

        HandleClientsInputReply saved =
            save(service,input("Save").setClientsToUpdate(List.of(new UpdateClientData().setClientId("gc_a"))));

        assertNull(saved.getOutput().getRevealedClientSecret());
        assertEquals(1,saved.getOutput().getClients().size());
        Mockito.verify(clientCommand).updateClients(Mockito.any(UpdateClientsRequest.class));
        Mockito.verify(clientCommand,Mockito.never()).createClient(Mockito.any(CreateClientRequest.class));
    }

    @Test
    public void
    failedCreateReportsFailureWithoutSecret()
        throws Exception
    {
        Mockito
            .when(clientCommand.createClient(Mockito.any(CreateClientRequest.class)))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new CreateClientReply().setSuccess(false).setFailureMessage("role not allowed")));

        HandleClientsInputReply saved =
            save(
                createService(SessionTestSupport.context("d",11L,1L)),
                input("Save").setClientsToCreate(List.of(new CreateClientData().setName("bad"))));

        assertNull(saved.getOutput().getRevealedClientSecret());
        assertEquals(List.of("bad: role not allowed"),saved.getOutput().getFailureMessages());
    }

    @Test
    public void
    moreThanOneCreateIsRefusedSoNoSecretIsLost()
        throws Exception
    {
        HandleClientsInputReply saved =
            save(
                createService(SessionTestSupport.context("d",11L,1L)),
                input("Save").setClientsToCreate(List.of(new CreateClientData().setName("a"),new CreateClientData().setName("b"))));

        assertEquals(1,saved.getOutput().getFailureMessages().size());
        Mockito.verify(clientCommand,Mockito.never()).createClient(Mockito.any(CreateClientRequest.class));
    }

    @Test
    public void
    deleteDestroysAndRelists()
        throws Exception
    {
        HandleClientsInputReply deleted =
            save(createService(SessionTestSupport.context("d",11L,1L)),input("Delete").setClientIdsToDelete(List.of("gc_a")));

        ArgumentCaptor<DestroyClientsRequest> captor = ArgumentCaptor.forClass(DestroyClientsRequest.class);

        Mockito.verify(clientCommand).destroyClients(captor.capture());
        assertEquals(List.of("gc_a"),captor.getValue().getClientIds());
        assertEquals(1,deleted.getOutput().getClients().size());
        assertTrue(deleted.getOutput().getFailureMessages().isEmpty());
    }

    @Test
    public void
    rotateRevealsNewSecret()
        throws Exception
    {
        Instant expires = Instant.parse("2027-01-02T00:00:00Z");

        HandleClientsInputReply rotated =
            save(
                createService(SessionTestSupport.context("d",11L,1L)),
                input("Rotate").setClientIdToRotate("gc_a").setSecretExpiresAt(expires));

        ArgumentCaptor<RotateClientSecretRequest> captor = ArgumentCaptor.forClass(RotateClientSecretRequest.class);

        Mockito.verify(clientCommand).rotateClientSecret(captor.capture());
        assertEquals("gc_a",captor.getValue().getClientId());
        assertEquals(expires,captor.getValue().getSecretExpiresAt());
        assertEquals("gc_a",rotated.getOutput().getRevealedClientId());
        assertEquals(SECRET,rotated.getOutput().getRevealedClientSecret());
    }

    @Test
    public void
    unauthenticatedCallerGetsFailureWithoutClientCall()
        throws Exception
    {
        GraniteFrontEndControllerService service = createService(SessionTestSupport.context("d",null,null));

        assertEquals(List.of("not authenticated"),load(service).getOutput().getFailureMessages());

        for (String command : List.of("Save","Delete","Rotate"))
        {
            HandleClientsInputReply reply =
                save(
                    service,
                    input(command)
                        .setClientsToCreate(List.of(new CreateClientData().setName("n")))
                        .setClientIdsToDelete(List.of("gc_a"))
                        .setClientIdToRotate("gc_a"));

            assertEquals(List.of("not authenticated"),reply.getOutput().getFailureMessages(),command);
            assertNull(reply.getOutput().getRevealedClientSecret(),command);
        }

        GetOwnedClientsOutputReply owned =
            service
                .getOwnedClientsOutput(new GetOwnedClientsOutputRequest().setSource("Client.Owned").setDestination("OwnedClientsView"))
                .toCompletableFuture()
                .get();

        assertEquals(List.of("not authenticated"),owned.getOutput().getFailureMessages());

        HandleOwnedClientSecretInputReply ownedRotate =
            service
                .handleOwnedClientSecretInput(
                    new HandleOwnedClientSecretInputRequest()
                        .setInput(
                            new OwnedClientSecretInput()
                                .setClientId("gc_own")
                                .setSource("Client.Owned")
                                .setCommand("Rotate")
                                .setDestination("OwnedClientsView")))
                .toCompletableFuture()
                .get();

        assertEquals(List.of("not authenticated"),ownedRotate.getOutput().getFailureMessages());
        assertNull(ownedRotate.getOutput().getRevealedClientSecret());
        Mockito.verifyNoInteractions(clientCommand);
        Mockito.verifyNoInteractions(clientQuery);
        Mockito.verifyNoInteractions(userQuery);
    }

    @Test
    public void
    clientCredentialCallerIsNotAnAuthenticatedUser()
        throws Exception
    {
        SessionRequestContext context =
            new SessionRequestContext(
                java.util.Map.of(),
                org.springframework.security.oauth2.jwt.Jwt
                    .withTokenValue("t")
                    .header("alg","RS256")
                    .subject("client:gc_x")
                    .build());

        assertEquals(List.of("not authenticated"),load(createService(context)).getOutput().getFailureMessages());
        Mockito.verifyNoInteractions(clientQuery);
    }

    @Test
    public void
    ownedViewUsesVerifiedCaller()
        throws Exception
    {
        GetOwnedClientsOutputReply reply =
            createService(SessionTestSupport.context("d",11L,7L))
                .getOwnedClientsOutput(
                    new GetOwnedClientsOutputRequest()
                        .setUserId(999L)
                        .setSource("Client.Owned")
                        .setDestination("OwnedClientsView"))
                .toCompletableFuture()
                .get();

        ArgumentCaptor<FindClientsByOwnerRequest> captor = ArgumentCaptor.forClass(FindClientsByOwnerRequest.class);

        Mockito.verify(clientQuery).findClientsByOwner(captor.capture());
        assertEquals(7L,captor.getValue().getUserId());
        assertEquals(List.of("gc_own"),reply.getOutput().getClients().stream().map(client -> client.getClientId()).toList());
        assertEquals(7L,reply.getOutput().getUserId());
        assertEquals("OwnedClientsView",reply.getOutput().getDestination());
        Mockito.verify(clientQuery,Mockito.never()).findAllClients(Mockito.any(FindAllClientsRequest.class));
    }

    @Test
    public void
    ownedRotateUsesVerifiedCallerNotPayload()
        throws Exception
    {
        HandleOwnedClientSecretInputReply reply =
            createService(SessionTestSupport.context("d",11L,7L))
                .handleOwnedClientSecretInput(
                    new HandleOwnedClientSecretInputRequest()
                        .setInput(
                            new OwnedClientSecretInput()
                                .setClientId("gc_own")
                                .setUserId(999L)
                                .setSource("Client.Owned")
                                .setCommand("Rotate")
                                .setDestination("OwnedClientsView")))
                .toCompletableFuture()
                .get();

        ArgumentCaptor<RotateOwnedClientSecretRequest> captor = ArgumentCaptor.forClass(RotateOwnedClientSecretRequest.class);

        Mockito.verify(clientCommand).rotateOwnedClientSecret(captor.capture());
        assertEquals("gc_own",captor.getValue().getClientId());
        assertEquals(7L,captor.getValue().getOwnerUserId());
        assertEquals("gc_own",reply.getOutput().getRevealedClientId());
        assertEquals(SECRET,reply.getOutput().getRevealedClientSecret());
        assertEquals(7L,reply.getOutput().getUserId());
        Mockito.verify(clientCommand,Mockito.never()).rotateClientSecret(Mockito.any(RotateClientSecretRequest.class));
    }

    @Test
    public void
    clientsOutputToStringOmitsSecret()
        throws Exception
    {
        ClientsOutput output = new ClientsOutput().setRevealedClientId("gc_a").setRevealedClientSecret(SECRET);

        assertFalse(output.toString().contains(SECRET));
        assertFalse(new ClientsInput().toString().contains(SECRET));

        String json = new ObjectMapperSupplier().get().writeValueAsString(output);

        assertTrue(json.contains(SECRET));
    }

    private GraniteFrontEndControllerService
    createService(SessionRequestContext requestContext)
    {
        return
            new GraniteFrontEndControllerService(
                new GraniteFrontEndControllerContext(
                    new ModelMapper(),
                    Runnable::run,
                    userQuery,
                    Mockito.mock(IUserCommandServiceClient.class),
                    Mockito.mock(ISessionCommandServiceClient.class),
                    Mockito.mock(ISessionQueryServiceClient.class),
                    requestContext,
                    new SessionLoginSettings(true,false),
                    SessionTestSupport.COOKIES,
                    new GraniteApplicationRolesManagerSupplier().get(),
                    List.of(),
                    new GraniteHelpServiceClients(),
                    new GraniteClientServiceClients()
                        .setClientCommandClient(() -> clientCommand)
                        .setClientQueryClient(() -> clientQuery)));
    }
}

//////////////////////////////////////////////////////////////////////////////
