//////////////////////////////////////////////////////////////////////////////
// SessionCommandServiceOktaRolesTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.foundation.core.security.ApplicationRoles;
import granite.foundation.core.security.GraniteApplicationRoles;
import granite.foundation.core.security.RoleDefinition;
import granite.session.server.domain.IOktaLoginTransactionRepository;
import granite.session.server.domain.ISessionRepository;
import granite.session.server.domain.OktaLoginTransaction;
import granite.session.server.domain.Session;
import granite.session.server.platform.MockSessionEventSender;
import granite.session.service.requestreply.CompleteOktaLoginReply;
import granite.session.service.requestreply.CompleteOktaLoginRequest;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.CreateUserReply;
import granite.user.service.requestreply.CreateUserRequest;
import granite.user.service.requestreply.FindUsersByExternalIdentityReply;
import granite.user.service.requestreply.FindUsersByExternalIdentityRequest;
import granite.user.service.requestreply.UpdateUserReply;
import granite.user.service.requestreply.UpdateUserRequest;
import granite.user.service.requestreply.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class SessionCommandServiceOktaRolesTest
{
    private static final String  DEVICE_ID      = "device-A";
    private static final String  TRANSACTION_ID = "raw-transaction-id-0123456789abcdef";
    private static final String  STATE          = "state-0123456789abcdef";
    private static final String  NONCE          = "nonce-0123456789abcdef";
    private static final String  VERIFIER       = "verifier-0123456789abcdef0123456789abcdef01234";
    private static final String  CODE           = "okta-authorization-code-0123456789";
    private static final String  SUB            = "00u1okta";
    private static final Long    USER_ID        = 42L;
    private static final Instant NOW            = Instant.parse("2026-10-24T12:00:00Z");
    private static final String  APPROVER       = "ROLE_PROMOTIONS_APPROVER";

    private FakeOkta                        okta;
    private OktaSettings                    settings;
    private ApplicationRoles                roles;
    private ISessionRepository              repository;
    private IOktaLoginTransactionRepository transactions;
    private IUserCommandServiceClient       userCommands;
    private IUserQueryServiceClient         userQueries;
    private RefreshTokenService             tokens;
    private List<Session>                   savedSessions;

    @BeforeEach
    public void
    setUp()
    {
        okta          = new FakeOkta();
        roles         =
            new ApplicationRoles(
                List.of(
                    new RoleDefinition(GraniteApplicationRoles.AUDITOR,"Auditor",Set.of()),
                    new RoleDefinition(GraniteApplicationRoles.USER,"User",Set.of()),
                    new RoleDefinition(APPROVER,"Approver",Set.of(GraniteApplicationRoles.AUDITOR,GraniteApplicationRoles.USER))),
                GraniteApplicationRoles.USER);
        settings      = okta.settings().setGroupMapping(Map.of("Granite Admins",ApplicationRoles.ADMIN));
        repository    = Mockito.mock(ISessionRepository.class);
        transactions  = Mockito.mock(IOktaLoginTransactionRepository.class);
        userCommands  = Mockito.mock(IUserCommandServiceClient.class);
        userQueries   = Mockito.mock(IUserQueryServiceClient.class);
        tokens        = new RefreshTokenService();
        savedSessions = new ArrayList<>();

        OktaLoginTransaction transaction =
            new OktaLoginTransaction()
                .setHashedTransactionId(tokens.hash(TRANSACTION_ID))
                .setState(STATE)
                .setNonce(NONCE)
                .setCodeVerifier(VERIFIER)
                .setReturnTo("/users")
                .setDeviceId(DEVICE_ID)
                .setExpires(NOW.plus(Duration.ofMinutes(5)));

        Mockito
            .when(transactions.findByHashedTransactionId(tokens.hash(TRANSACTION_ID)))
            .thenReturn(Optional.of(transaction));
        Mockito.when(repository.findOpenByDeviceId(Mockito.anyString())).thenReturn(List.of());
        Mockito.when(repository.findOpenByUserId(Mockito.anyLong())).thenReturn(List.of());
        stubSessionSave();
        stubUpdateUser(true);
        okta.stubTokenReturning(okta.idToken(SUB,NONCE));
    }

    @AfterEach
    public void
    tearDown()
    {
        okta.stop();
    }

    @Test
    public void
    onCreateStoresGroupRolesForNewUser()
    {
        stubNoUser();

        CompleteOktaLoginReply reply = complete();

        ArgumentCaptor<CreateUserRequest> captor = ArgumentCaptor.forClass(CreateUserRequest.class);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        Mockito.verify(userCommands).createUser(captor.capture());
        assertEquals(Set.of(ApplicationRoles.ADMIN),captor.getValue().getUserToCreate().getAuthorities());
        assertEquals(Set.of(ApplicationRoles.ADMIN),reply.getSessionData().getAuthorities());
    }

    @Test
    public void
    onCreateIgnoresGroupsForExistingUser()
    {
        stubExistingUser(Set.of(GraniteApplicationRoles.USER));

        CompleteOktaLoginReply reply = complete();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(Set.of(GraniteApplicationRoles.USER),reply.getSessionData().getAuthorities());
        Mockito.verify(userCommands,Mockito.never()).updateUser(Mockito.any(UpdateUserRequest.class));
    }

    @Test
    public void
    everyLoginWritesBackChangedRoles()
    {
        settings.setRoleAssignment(OktaRoleAssignment.EVERY_LOGIN).setGroupMapping(Map.of("Granite Admins",APPROVER));
        stubExistingUser(Set.of(GraniteApplicationRoles.USER));

        CompleteOktaLoginReply reply = complete();

        ArgumentCaptor<UpdateUserRequest> captor = ArgumentCaptor.forClass(UpdateUserRequest.class);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        Mockito.verify(userCommands,Mockito.times(1)).updateUser(captor.capture());
        assertEquals(USER_ID,captor.getValue().getUserToUpdate().getUserId());
        assertEquals(
            Optional.of(Set.of(APPROVER,GraniteApplicationRoles.AUDITOR,GraniteApplicationRoles.USER)),
            captor.getValue().getUserToUpdate().getAuthorities());
        assertEquals(Set.of(APPROVER,GraniteApplicationRoles.AUDITOR,GraniteApplicationRoles.USER),reply.getSessionData().getAuthorities());
    }

    @Test
    public void
    everyLoginSkipsWriteWhenUnchanged()
    {
        settings.setRoleAssignment(OktaRoleAssignment.EVERY_LOGIN);
        stubExistingUser(Set.of(ApplicationRoles.ADMIN));

        CompleteOktaLoginReply reply = complete();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        Mockito.verify(userCommands,Mockito.never()).updateUser(Mockito.any(UpdateUserRequest.class));
        assertEquals(Set.of(ApplicationRoles.ADMIN),reply.getSessionData().getAuthorities());
    }

    @Test
    public void
    everyLoginRefuseRejectsUnmappedExistingUser()
    {
        settings
            .setRoleAssignment(OktaRoleAssignment.EVERY_LOGIN)
            .setUnmappedGroups(OktaUnmappedGroups.REFUSE)
            .setGroupMapping(Map.of());
        stubExistingUser(Set.of(GraniteApplicationRoles.USER));

        CompleteOktaLoginReply reply = complete();

        assertFalse(reply.isSuccess());
        assertEquals("invalid credentials",reply.getFailureMessage());
        Mockito.verify(userCommands,Mockito.never()).updateUser(Mockito.any(UpdateUserRequest.class));
        assertTrue(savedSessions.isEmpty());
    }
    @Test
    public void
    everyLoginWriteFailureRefuses()
    {
        settings.setRoleAssignment(OktaRoleAssignment.EVERY_LOGIN);
        stubExistingUser(Set.of(GraniteApplicationRoles.USER));
        stubUpdateUser(false);

        CompleteOktaLoginReply reply = complete();

        assertFalse(reply.isSuccess());
        assertEquals("invalid credentials",reply.getFailureMessage());
        assertTrue(savedSessions.isEmpty());
    }

    @Test
    public void
    refuseModeRejectsUnmappedNewUser()
    {
        settings
            .setGroupMapping(Map.of())
            .setUnmappedGroups(OktaUnmappedGroups.REFUSE);
        stubNoUser();

        CompleteOktaLoginReply reply = complete();

        assertFalse(reply.isSuccess());
        assertEquals("invalid credentials",reply.getFailureMessage());
        Mockito.verify(userCommands,Mockito.never()).createUser(Mockito.any(CreateUserRequest.class));
    }

    @Test
    public void
    defaultRoleFallbackCreatesUserWithDefaultRole()
    {
        settings.setGroupMapping(Map.of());
        stubNoUser();

        CompleteOktaLoginReply reply = complete();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(Set.of(GraniteApplicationRoles.USER),reply.getSessionData().getAuthorities());
    }

    private CompleteOktaLoginReply
    complete()
    {
        IJwtTokenService jwt = Mockito.mock(IJwtTokenService.class);

        Mockito.when(jwt.mint(Mockito.any(Session.class))).thenReturn("minted.jwt");

        SessionCommandService subject =
            new SessionCommandService(
                repository,
                transactions,
                jwt,
                tokens,
                userCommands,
                userQueries,
                okta.client(settings,Clock.systemUTC()),
                settings,
                new RequiredAuthoritiesLoginGate(List.of()),
                new MockSessionEventSender(),
                new ModelMapper().registerModule(new MappingModule()),
                Duration.ofHours(24),
                Duration.ofMinutes(2),
                Clock.fixed(NOW,ZoneOffset.UTC),
                roles);

        return
            subject
                .completeOktaLogin(
                    new CompleteOktaLoginRequest()
                        .setCode(CODE)
                        .setState(STATE)
                        .setTransactionId(TRANSACTION_ID)
                        .setDeviceId(DEVICE_ID))
                .toCompletableFuture()
                .join();
    }

    private void
    stubExistingUser(Set<String> authorities)
    {
        Mockito
            .when(userQueries.findUsersByExternalIdentity(Mockito.any(FindUsersByExternalIdentityRequest.class)))
            .thenAnswer(
                invocation ->
                    CompletableFuture.completedFuture(
                        new FindUsersByExternalIdentityReply(invocation.getArgument(0))
                            .setUsers(
                                List.of(
                                    new UserData()
                                        .setUserId(USER_ID)
                                        .setUserName("alice")
                                        .setExternalIdentity("okta:" + SUB)
                                        .setAuthorities(authorities)))
                            .setSuccess(true)));
    }

    private void
    stubNoUser()
    {
        Mockito
            .when(userQueries.findUsersByExternalIdentity(Mockito.any(FindUsersByExternalIdentityRequest.class)))
            .thenAnswer(
                invocation ->
                    CompletableFuture.completedFuture(
                        new FindUsersByExternalIdentityReply(invocation.getArgument(0))
                            .setUsers(List.of())
                            .setSuccess(true)));
        Mockito
            .when(userCommands.createUser(Mockito.any(CreateUserRequest.class)))
            .thenAnswer(
                invocation ->
                {
                    CreateUserRequest request = invocation.getArgument(0);

                    return
                        CompletableFuture.completedFuture(
                            new CreateUserReply(request)
                                .setSuccess(true)
                                .setCreatedUser(
                                    new UserData()
                                        .setUserId(USER_ID)
                                        .setUserName(request.getUserToCreate().getUserName())
                                        .setExternalIdentity(request.getUserToCreate().getExternalIdentity())
                                        .setAuthorities(request.getUserToCreate().getAuthorities())));
                });
    }

    private void
    stubUpdateUser(boolean success)
    {
        Mockito
            .when(userCommands.updateUser(Mockito.any(UpdateUserRequest.class)))
            .thenAnswer(
                invocation ->
                    CompletableFuture.completedFuture(
                        new UpdateUserReply(invocation.getArgument(0)).setSuccess(success)));
    }

    private void
    stubSessionSave()
    {
        AtomicLong next = new AtomicLong(500L);

        Mockito
            .when(repository.save(Mockito.any(Session.class)))
            .thenAnswer(
                invocation ->
                {
                    Session session = invocation.getArgument(0);

                    if (session.getSessionId() == null)
                        session.setSessionId(next.getAndIncrement());

                    if (!savedSessions.contains(session))
                        savedSessions.add(session);

                    return session;
                });
    }
}

//////////////////////////////////////////////////////////////////////////////
