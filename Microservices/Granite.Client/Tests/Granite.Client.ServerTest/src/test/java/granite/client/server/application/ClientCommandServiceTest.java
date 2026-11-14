//////////////////////////////////////////////////////////////////////////////
// ClientCommandServiceTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.application;

import granite.client.server.domain.Client;
import granite.client.server.domain.IClientRepository;
import granite.client.service.requestreply.ClientData;
import granite.client.service.requestreply.CreateClientData;
import granite.client.service.requestreply.CreateClientReply;
import granite.client.service.requestreply.CreateClientRequest;
import granite.client.service.requestreply.DestroyClientsReply;
import granite.client.service.requestreply.DestroyClientsRequest;
import granite.client.service.requestreply.IClientCommandService;
import granite.client.service.requestreply.RotateClientSecretReply;
import granite.client.service.requestreply.RotateClientSecretRequest;
import granite.client.service.requestreply.RotateOwnClientSecretReply;
import granite.client.service.requestreply.RotateOwnClientSecretRequest;
import granite.client.service.requestreply.RotateOwnedClientSecretRequest;
import granite.client.service.requestreply.UpdateClientData;
import granite.client.service.requestreply.UpdateClientsReply;
import granite.client.service.requestreply.UpdateClientsRequest;
import granite.client.service.requestreply.VerifyClientCredentialsReply;
import granite.client.service.requestreply.VerifyClientCredentialsRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import strata.server.spring.inject.RequestAttributeMap;

import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class ClientCommandServiceTest
{
    private static final Instant START = Instant.parse("2026-11-14T12:00:00Z");

    private static ApplicationContext context;

    @BeforeAll
    public static void
    initialize()
    {
        context = new AnnotationConfigApplicationContext(TestConfiguration.class);
    }

    @BeforeEach
    public void
    setUp()
    {
        RequestContextHolder.setRequestAttributes(new RequestAttributeMap());
        TestConfiguration.CLOCK.set(START);
        TestCallerSubject.set(null);
    }

    @AfterEach
    public void
    tearDown()
    {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void
    createGeneratesIdAndSecretAndStoresOnlyHash()
    {
        CreateClientReply reply = create(uniqueName(),Set.of("ROLE_USER"),null);

        assertTrue(reply.isSuccess());
        assertTrue(reply.getClient().getClientId().matches("gc_[a-z2-7]{32}"));
        assertEquals(43,reply.getClientSecret().length());
        assertTrue(reply.getClient().isEnabled());
        assertEquals(Set.of("ROLE_USER"),reply.getClient().getAuthorities());
        assertNotNull(reply.getClient().getCreatedAt());
        assertTrue(verify(reply.getClient().getClientId(),reply.getClientSecret()).isValid());

        Client stored = inTransaction(r -> r.findByClientId(reply.getClient().getClientId()).orElseThrow());

        assertTrue(ClientCredentials.matches(stored.getSecretSha256(),reply.getClientSecret()));
        assertEquals(
            0,
            java.util.Arrays
                .stream(ClientData.class.getDeclaredFields())
                .filter(f -> f.getName().toLowerCase().contains("secret") && !f.getName().equals("secretExpiresAt"))
                .count());
    }

    @Test
    public void
    createRejectsAdminScopeAndUnknownRoles()
    {
        for (String role : List.of("ROLE_ADMIN","SCOPE_internal","ROLE_NOPE"))
        {
            CreateClientReply reply = create(uniqueName(),Set.of(role),null);

            assertFalse(reply.isSuccess(),role);
            assertNull(reply.getClientSecret());
        }
    }

    @Test
    public void
    createRejectsBlankAndDuplicateNames()
    {
        String name = uniqueName();

        assertTrue(create(name,Set.of(),null).isSuccess());
        assertFalse(create(name,Set.of(),null).isSuccess());
        assertFalse(create("  ",Set.of(),null).isSuccess());
        assertFalse(create("x".repeat(101),Set.of(),null).isSuccess());
    }

    @Test
    public void
    updateChecksOnlyNewlyAddedRoles()
    {
        String        name    = uniqueName();
        ClientData    created = create(name,Set.of("ROLE_USER"),null).getClient();
        String        id      = created.getClientId();

        inTransaction(r -> { r.save(r.findByClientId(id).orElseThrow().setAuthorities(Set.of("ROLE_USER","ROLE_OLD"))); return null; });

        UpdateClientsReply kept = update(id,name,"described",Set.of("ROLE_USER","ROLE_OLD"));

        assertTrue(kept.isSuccess(),kept.getFailureMessage());
        assertEquals(Set.of("ROLE_USER","ROLE_OLD"),kept.getClients().get(0).getAuthorities());
        assertEquals("described",kept.getClients().get(0).getDescription());

        UpdateClientsReply unknown = update(id,name,"described",Set.of("ROLE_USER","ROLE_OLD","ROLE_BOGUS"));

        assertFalse(unknown.isSuccess());
        assertTrue(unknown.getFailureMessage().contains("unknown role: ROLE_BOGUS"),unknown.getFailureMessage());

        UpdateClientsReply admin = update(id,name,"described",Set.of("ROLE_USER","ROLE_ADMIN"));

        assertFalse(admin.isSuccess());
        assertTrue(admin.getFailureMessage().contains("role not allowed for clients: ROLE_ADMIN"),admin.getFailureMessage());

        Client stored = inTransaction(r -> r.findByClientId(id).orElseThrow());

        assertEquals(Set.of("ROLE_USER","ROLE_OLD"),stored.getAuthorities());
    }

    @Test
    public void
    rotateInvalidatesOldSecret()
    {
        CreateClientReply created = create(uniqueName(),Set.of("ROLE_USER"),null);
        String            id      = created.getClient().getClientId();

        RotateClientSecretReply rotated = await(subject().rotateClientSecret(new RotateClientSecretRequest().setClientId(id)));

        assertTrue(rotated.isSuccess());
        assertFalse(verify(id,created.getClientSecret()).isValid());
        assertTrue(verify(id,rotated.getClientSecret()).isValid());
    }

    @Test
    public void
    rotateOwnedRejectsNonOwnerLikeUnknown()
    {
        CreateClientReply created = create(uniqueName(),Set.of("ROLE_USER"),null);
        String            id      = created.getClient().getClientId();

        update(id,created.getClient().getName(),null,Set.of("ROLE_USER"),Set.of(1001L));

        RotateClientSecretReply stranger =
            await(subject().rotateOwnedClientSecret(new RotateOwnedClientSecretRequest().setClientId(id).setOwnerUserId(2002L)));
        RotateClientSecretReply unknown =
            await(subject().rotateOwnedClientSecret(new RotateOwnedClientSecretRequest().setClientId("gc_nope").setOwnerUserId(1001L)));

        assertFalse(stranger.isSuccess());
        assertFalse(unknown.isSuccess());
        assertEquals("not found",stranger.getFailureMessage());
        assertEquals("not found",unknown.getFailureMessage());
        assertNull(stranger.getClientSecret());
        assertTrue(verify(id,created.getClientSecret()).isValid());

        RotateClientSecretReply owner =
            await(subject().rotateOwnedClientSecret(new RotateOwnedClientSecretRequest().setClientId(id).setOwnerUserId(1001L)));

        assertTrue(owner.isSuccess());
        assertFalse(verify(id,created.getClientSecret()).isValid());
        assertTrue(verify(id,owner.getClientSecret()).isValid());
    }

    @Test
    public void
    rotateOwnUsesCallerSubject()
    {
        CreateClientReply created = create(uniqueName(),Set.of("ROLE_USER"),null);
        String            id      = created.getClient().getClientId();

        for (String subject : new String[] {"service:x","1001","client:gc_nope",null})
        {
            TestCallerSubject.set(subject);

            RotateOwnClientSecretReply reply = await(subject().rotateOwnClientSecret(new RotateOwnClientSecretRequest()));

            assertFalse(reply.isSuccess(),String.valueOf(subject));
            assertEquals("not found",reply.getFailureMessage());
        }

        assertTrue(verify(id,created.getClientSecret()).isValid());

        TestCallerSubject.set("client:" + id);

        RotateOwnClientSecretReply reply = await(subject().rotateOwnClientSecret(new RotateOwnClientSecretRequest()));

        assertTrue(reply.isSuccess());
        assertEquals(id,reply.getClientId());
        assertFalse(verify(id,created.getClientSecret()).isValid());
        assertTrue(verify(id,reply.getClientSecret()).isValid());
    }

    @Test
    public void
    verifyHonoursExpiryBoundaryAndEnabled()
    {
        Instant           expiry  = Instant.parse("2026-11-15T00:00:00Z");
        CreateClientReply created = create(uniqueName(),Set.of("ROLE_USER"),expiry);
        String            id      = created.getClient().getClientId();
        String            secret  = created.getClientSecret();

        TestConfiguration.CLOCK.set(expiry.minusMillis(1));
        assertTrue(verify(id,secret).isValid());

        TestConfiguration.CLOCK.set(expiry);

        VerifyClientCredentialsReply expired = verify(id,secret);

        assertFalse(expired.isValid());

        TestConfiguration.CLOCK.set(START);
        assertTrue(verify(id,secret).isValid());

        UpdateClientData disable =
            new UpdateClientData()
                .setClientId(id)
                .setName(created.getClient().getName())
                .setAuthorities(Set.of("ROLE_USER"))
                .setEnabled(false)
                .setSecretExpiresAt(expiry);

        assertTrue(await(subject().updateClients(new UpdateClientsRequest().setClients(List.of(disable)))).isSuccess());

        VerifyClientCredentialsReply disabled = verify(id,secret);
        VerifyClientCredentialsReply wrong    = verify(id,"wrong");
        VerifyClientCredentialsReply unknown  = verify("gc_unknown",secret);

        for (VerifyClientCredentialsReply reply : List.of(expired,disabled,wrong,unknown))
        {
            assertFalse(reply.isValid());
            assertFalse(reply.isSuccess());
            assertEquals("invalid client",reply.getFailureMessage());
            assertNull(reply.getClientId());
            assertTrue(reply.getAuthorities() == null || reply.getAuthorities().isEmpty());
            assertNull(reply.getException());
        }
    }

    @Test
    public void
    verifyMintsOnlyCurrentCatalogRoles()
    {
        CreateClientReply created = create(uniqueName(),Set.of("ROLE_USER","ROLE_CLIENT_ADMIN"),null);
        String            id      = created.getClient().getClientId();

        inTransaction(r -> { r.save(r.findByClientId(id).orElseThrow().setAuthorities(Set.of("ROLE_USER","ROLE_OLD","ROLE_ADMIN","SCOPE_internal"))); return null; });

        VerifyClientCredentialsReply reply = verify(id,created.getClientSecret());

        assertTrue(reply.isValid());
        assertEquals(id,reply.getClientId());
        assertEquals(Set.of("ROLE_USER"),reply.getAuthorities());
    }

    @Test
    public void
    verifyRecordsLastTokenIssuedWithoutVersionBump()
    {
        CreateClientReply created = create(uniqueName(),Set.of("ROLE_USER"),null);
        String            id      = created.getClient().getClientId();
        Client            before  = inTransaction(r -> r.findByClientId(id).orElseThrow());

        assertNull(before.getLastTokenIssuedAt());

        TestConfiguration.CLOCK.set(START.plusSeconds(60));
        assertTrue(verify(id,created.getClientSecret()).isValid());

        Client after = inTransaction(r -> r.findByClientId(id).orElseThrow());

        assertEquals(START.plusSeconds(60),after.getLastTokenIssuedAt());
        assertEquals(before.getVersion(),after.getVersion());
    }

    @Test
    public void
    eventsCarryNoSecret()
    {
        int               from    = CapturingClientEventMapper.PAYLOADS.size();
        CreateClientReply created = create(uniqueName(),Set.of("ROLE_USER"),null);
        String            id      = created.getClient().getClientId();
        String            first   = created.getClientSecret();
        String            second  = await(subject().rotateClientSecret(new RotateClientSecretRequest().setClientId(id))).getClientSecret();

        assertTrue(await(subject().destroyClients(new DestroyClientsRequest().setClientIds(List.of(id)))).isSuccess());

        List<String> payloads = CapturingClientEventMapper.PAYLOADS.subList(from,CapturingClientEventMapper.PAYLOADS.size());

        assertTrue(payloads.size() >= 3);

        for (String secret : List.of(first,second))
        {
            byte[] hash = ClientCredentials.hash(secret);

            for (String payload : payloads)
            {
                assertFalse(payload.contains(secret));
                assertFalse(payload.contains(HexFormat.of().formatHex(hash)));
                assertFalse(payload.contains(Base64.getEncoder().encodeToString(hash)));
                assertFalse(payload.contains(Base64.getUrlEncoder().withoutPadding().encodeToString(hash)));
            }
        }
    }

    @Test
    public void
    destroyRemovesClients()
    {
        String name = uniqueName();
        String id   = create(name,Set.of("ROLE_USER"),null).getClient().getClientId();

        DestroyClientsReply reply = await(subject().destroyClients(new DestroyClientsRequest().setClientIds(List.of(id))));

        assertTrue(reply.isSuccess());
        assertEquals(List.of(id),reply.getClientIds());
        assertTrue(inTransaction(r -> r.findByClientId(id)).isEmpty());
        assertTrue(create(name,Set.of("ROLE_USER"),null).isSuccess());
    }

    private IClientCommandService
    subject() { return context.getBean(IClientCommandService.class); }

    private CreateClientReply
    create(String name,Set<String> authorities,Instant expiry)
    {
        return
            await(
                subject()
                    .createClient(
                        new CreateClientRequest()
                            .setClient(
                                new CreateClientData()
                                    .setName(name)
                                    .setAuthorities(authorities)
                                    .setEnabled(true)
                                    .setSecretExpiresAt(expiry))));
    }

    private UpdateClientsReply
    update(String id,String name,String description,Set<String> authorities)
    {
        return update(id,name,description,authorities,Set.of());
    }

    private UpdateClientsReply
    update(String id,String name,String description,Set<String> authorities,Set<Long> owners)
    {
        return
            await(
                subject()
                    .updateClients(
                        new UpdateClientsRequest()
                            .setClients(
                                List.of(
                                    new UpdateClientData()
                                        .setClientId(id)
                                        .setName(name)
                                        .setDescription(description)
                                        .setAuthorities(authorities)
                                        .setOwners(owners)
                                        .setEnabled(true)))));
    }

    private VerifyClientCredentialsReply
    verify(String id,String secret)
    {
        return
            await(
                subject()
                    .verifyClientCredentials(
                        new VerifyClientCredentialsRequest().setClientId(id).setClientSecret(secret)));
    }

    private <T> T
    inTransaction(Function<IClientRepository,T> action)
    {
        return
            new TransactionTemplate(context.getBean(PlatformTransactionManager.class))
                .execute(status -> action.apply(context.getBean(IClientRepository.class)));
    }

    private static <T> T
    await(CompletionStage<T> stage)
    {
        try
        {
            return stage.toCompletableFuture().get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private static String
    uniqueName() { return "client-" + UUID.randomUUID(); }
}

//////////////////////////////////////////////////////////////////////////////
