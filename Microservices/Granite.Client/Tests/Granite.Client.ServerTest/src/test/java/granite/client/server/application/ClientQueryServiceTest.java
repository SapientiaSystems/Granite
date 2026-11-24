//////////////////////////////////////////////////////////////////////////////
// ClientQueryServiceTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import granite.client.service.requestreply.ClientData;
import granite.client.service.requestreply.CreateClientData;
import granite.client.service.requestreply.CreateClientRequest;
import granite.client.service.requestreply.FindAllClientsRequest;
import granite.client.service.requestreply.FindClientsByIdsRequest;
import granite.client.service.requestreply.FindClientsByOwnerRequest;
import granite.client.service.requestreply.FindClientsReply;
import granite.client.service.requestreply.IClientCommandService;
import granite.client.service.requestreply.IClientQueryService;
import granite.client.service.requestreply.UpdateClientData;
import granite.client.service.requestreply.UpdateClientsRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import strata.server.spring.inject.RequestAttributeMap;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class ClientQueryServiceTest
{
    private static ApplicationContext context;

    @BeforeAll
    public static void
    initialize()
    {
        context = new AnnotationConfigApplicationContext(TestConfiguration.class);
    }

    @BeforeEach
    public void
    setUp() { RequestContextHolder.setRequestAttributes(new RequestAttributeMap()); }

    @AfterEach
    public void
    tearDown() { RequestContextHolder.resetRequestAttributes(); }

    @Test
    public void
    findClientsByOwnerOrdersByName()
        throws Exception
    {
        String tag   = UUID.randomUUID().toString().substring(0,8);
        long   owner = 70000L + Math.abs(tag.hashCode() % 10000);
        String c     = createOwned("c-" + tag,owner);
        String a     = createOwned("a-" + tag,owner);
        String b     = createOwned("b-" + tag,owner);

        createOwned("other-" + tag,owner + 1);

        FindClientsReply reply =
            await(query().findClientsByOwner(new FindClientsByOwnerRequest().setUserId(owner)));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(
            List.of("a-" + tag,"b-" + tag,"c-" + tag),
            reply.getClients().stream().map(ClientData::getName).toList());
        assertEquals(List.of(a,b,c),reply.getClients().stream().map(ClientData::getClientId).toList());
    }

    @Test
    public void
    findAllOmitsSecretHash()
        throws Exception
    {
        String id = createOwned("all-" + UUID.randomUUID(),90001L);

        FindClientsReply all   = await(query().findAllClients(new FindAllClientsRequest()));
        FindClientsReply byIds =
            await(query().findClientsByIds(new FindClientsByIdsRequest().setClientIds(Set.of(id))));

        assertTrue(all.isSuccess());
        assertTrue(all.getClients().stream().anyMatch(d -> d.getClientId().equals(id)));
        assertEquals(1,byIds.getClients().size());

        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

        for (FindClientsReply reply : List.of(all,byIds))
        {
            String json = mapper.writeValueAsString(reply).replace("secretExpiresAt","").toLowerCase();

            assertFalse(json.contains("secret"));
            assertFalse(json.contains("sha256"));
        }
    }

    @Test
    public void
    findByIdsRejectsEmptySet()
        throws Exception
    {
        FindClientsReply reply = await(query().findClientsByIds(new FindClientsByIdsRequest()));

        assertFalse(reply.isSuccess());
    }

    private String
    createOwned(String name,long owner)
        throws Exception
    {
        IClientCommandService commands = context.getBean(IClientCommandService.class);
        String                id       =
            await(
                commands.createClient(
                    new CreateClientRequest()
                        .setClient(
                            new CreateClientData()
                                .setName(name)
                                .setAuthorities(Set.of("ROLE_USER"))
                                .setEnabled(true))))
                .getClient()
                .getClientId();

        await(
            commands.updateClients(
                new UpdateClientsRequest()
                    .setClients(
                        List.of(
                            new UpdateClientData()
                                .setClientId(id)
                                .setName(name)
                                .setAuthorities(Set.of("ROLE_USER"))
                                .setOwners(Set.of(owner))
                                .setEnabled(true)))));
        return id;
    }

    private IClientQueryService
    query() { return context.getBean(IClientQueryService.class); }

    private static <T> T
    await(CompletionStage<T> stage)
        throws Exception
    {
        return stage.toCompletableFuture().get();
    }
}

//////////////////////////////////////////////////////////////////////////////
