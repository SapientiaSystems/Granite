//////////////////////////////////////////////////////////////////////////////
// SessionCommandServiceDefaultRoleTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.foundation.core.security.ApplicationRoles;
import granite.foundation.core.security.RoleDefinition;
import granite.session.server.domain.IOktaLoginTransactionRepository;
import granite.session.server.domain.ISessionRepository;
import granite.session.server.domain.Session;
import granite.session.server.platform.MockSessionEventSender;
import granite.session.service.requestreply.LoginReply;
import granite.session.service.requestreply.LoginRequest;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.AuthenticateUserReply;
import granite.user.service.requestreply.AuthenticateUserRequest;
import granite.user.service.requestreply.CredentialKind;
import granite.user.service.requestreply.MergeUserValuesReply;
import granite.user.service.requestreply.MergeUserValuesRequest;
import granite.user.service.requestreply.UpdateUserReply;
import granite.user.service.requestreply.UpdateUserRequest;
import granite.user.service.requestreply.UserData;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class SessionCommandServiceDefaultRoleTest
{
    private static final Long    USER_ID   = 101L;
    private static final String  USER_NAME = "alice@example.com";
    private static final String  PASSWORD  = "hunter2";
    private static final String  DEVICE_ID = "device-A";
    private static final Instant NOW       = Instant.parse("2026-10-26T12:00:00Z");
    private static final String  THEME     = "preferences.granite.theme";

    private static final Set<String> EXPANDED_DEFAULT = Set.of("ROLE_PROMOTIONS_APPROVER","ROLE_PROMOTIONS_REVIEWER");

    private static final ApplicationRoles POPLAR =
        new ApplicationRoles(
            List.of(
                new RoleDefinition("ROLE_PROMOTIONS_REVIEWER","Promotions Reviewer",Set.of()),
                new RoleDefinition("ROLE_PROMOTIONS_APPROVER","Promotions Approver",Set.of("ROLE_PROMOTIONS_REVIEWER"))),
            "ROLE_PROMOTIONS_APPROVER");

    private ISessionRepository        repository;
    private IJwtTokenService          jwtTokenService;
    private IUserCommandServiceClient userCommandServiceClient;
    private SessionCommandService     subject;

    @BeforeEach
    public void
    setUp()
    {
        repository               = Mockito.mock(ISessionRepository.class);
        jwtTokenService          = Mockito.mock(IJwtTokenService.class);
        userCommandServiceClient = Mockito.mock(IUserCommandServiceClient.class);

        AtomicLong next = new AtomicLong(500L);

        Mockito
            .when(repository.save(Mockito.any(Session.class)))
            .thenAnswer(invocation ->
            {
                Session s = invocation.getArgument(0);

                if (s.getSessionId() == null)
                    s.setSessionId(next.getAndIncrement());

                return s;
            });
        Mockito.when(jwtTokenService.mint(Mockito.any(Session.class))).thenReturn("minted.jwt.token");

        subject =
            new SessionCommandService(
                repository,
                Mockito.mock(IOktaLoginTransactionRepository.class),
                jwtTokenService,
                new RefreshTokenService(),
                userCommandServiceClient,
                Mockito.mock(IUserQueryServiceClient.class),
                Mockito.mock(IOktaClient.class),
                new OktaSettings(),
                new CatalogRoleLoginGate(POPLAR),
                new MockSessionEventSender(),
                new ModelMapper().registerModule(new MappingModule()),
                Duration.ofHours(24),
                Duration.ofMinutes(2),
                Clock.fixed(NOW,ZoneOffset.UTC),
                POPLAR);
    }

    @Test
    public void
    firstPasswordLoginGetsDefaultRole()
    {
        stubAuthenticate(true,Set.of());
        stubUpdate(true);
        stubMerge(true);

        LoginReply reply = login();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());

        ArgumentCaptor<UpdateUserRequest> update = ArgumentCaptor.forClass(UpdateUserRequest.class);

        Mockito.verify(userCommandServiceClient).updateUser(update.capture());
        assertEquals(USER_ID,update.getValue().getUserToUpdate().getUserId());
        assertEquals(EXPANDED_DEFAULT,update.getValue().getUserToUpdate().getAuthorities().get());

        ArgumentCaptor<Session> minted = ArgumentCaptor.forClass(Session.class);

        Mockito.verify(jwtTokenService).mint(minted.capture());
        assertEquals(EXPANDED_DEFAULT,minted.getValue().getAuthorities());
    }

    @Test
    public void
    firstLoginSetsMarkerAndKeepsOtherValues()
    {
        stubAuthenticate(true,Set.of(),Map.of(THEME,"dark"));
        stubUpdate(true);
        stubMerge(true);

        assertTrue(login().isSuccess());

        ArgumentCaptor<UpdateUserRequest> update = ArgumentCaptor.forClass(UpdateUserRequest.class);

        Mockito.verify(userCommandServiceClient).updateUser(update.capture());
        assertTrue(update.getValue().getUserToUpdate().getValues().isEmpty());
        assertEquals(EXPANDED_DEFAULT,update.getValue().getUserToUpdate().getAuthorities().get());

        ArgumentCaptor<MergeUserValuesRequest> merge = ArgumentCaptor.forClass(MergeUserValuesRequest.class);

        Mockito.verify(userCommandServiceClient).mergeUserValues(merge.capture());
        assertEquals(USER_ID,merge.getValue().getUserId());
        assertEquals(Map.of(SessionCommandService.ROLES_ASSIGNED,true),merge.getValue().getValues());
    }

    @Test
    public void
    markerMergeFailureRefuses()
    {
        stubAuthenticate(true,Set.of());
        stubUpdate(true);
        stubMerge(false);

        assertRefused(login());
    }

    @Test
    public void
    markerMergeNullReplyRefuses()
    {
        stubAuthenticate(true,Set.of());
        stubUpdate(true);
        Mockito
            .when(userCommandServiceClient.mergeUserValues(Mockito.any(MergeUserValuesRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        assertRefused(login());
    }

    @Test
    public void
    markerMergeExceptionRefuses()
    {
        stubAuthenticate(true,Set.of());
        stubUpdate(true);
        Mockito
            .when(userCommandServiceClient.mergeUserValues(Mockito.any(MergeUserValuesRequest.class)))
            .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("boom")));

        assertRefused(login());
    }

    @Test
    public void
    roleWriteFailureSkipsMerge()
    {
        stubAuthenticate(true,Set.of());
        stubUpdate(false);

        assertRefused(login());
        Mockito.verify(userCommandServiceClient,Mockito.never()).mergeUserValues(Mockito.any(MergeUserValuesRequest.class));
    }

    @Test
    public void
    clearedRolesAreNotReGranted()
    {
        stubAuthenticate(true,Set.of(),Map.of(SessionCommandService.ROLES_ASSIGNED,true));

        assertRefused(login());
        Mockito.verify(userCommandServiceClient,Mockito.never()).updateUser(Mockito.any(UpdateUserRequest.class));
    }

    @Test
    public void
    existingRolesUntouched()
    {
        stubAuthenticate(true,Set.of("ROLE_PROMOTIONS_APPROVER","ROLE_PROMOTIONS_REVIEWER"));

        LoginReply reply = login();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        Mockito.verify(userCommandServiceClient,Mockito.never()).updateUser(Mockito.any(UpdateUserRequest.class));
    }

    @Test
    public void
    defaultRoleWriteFailureRefuses()
    {
        stubAuthenticate(true,Set.of());
        stubUpdate(false);

        assertRefused(login());
    }

    @Test
    public void
    defaultRoleWriteNullReplyRefuses()
    {
        stubAuthenticate(true,Set.of());
        Mockito
            .when(userCommandServiceClient.updateUser(Mockito.any(UpdateUserRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        assertRefused(login());
    }

    @Test
    public void
    invalidCredentialsNeverAssign()
    {
        stubAuthenticate(false,Set.of());

        assertRefused(login());
        Mockito.verify(userCommandServiceClient,Mockito.never()).updateUser(Mockito.any(UpdateUserRequest.class));
    }

    private void
    assertRefused(LoginReply reply)
    {
        assertFalse(reply.isSuccess());
        assertEquals("invalid credentials",reply.getFailureMessage());
        assertNull(reply.getAccessToken());
        Mockito.verify(repository,Mockito.never()).save(Mockito.any(Session.class));
    }

    private LoginReply
    login()
    {
        return
            subject
                .login(
                    new LoginRequest()
                        .setUserName(USER_NAME)
                        .setCredential(PASSWORD)
                        .setCredentialKind(CredentialKind.PASSWORD)
                        .setDeviceId(DEVICE_ID))
                .toCompletableFuture()
                .join();
    }

    private void
    stubAuthenticate(boolean valid,Set<String> authorities)
    {
        stubAuthenticate(valid,authorities,Map.of());
    }

    private void
    stubAuthenticate(boolean valid,Set<String> authorities,Map<String,Object> values)
    {
        Mockito
            .when(userCommandServiceClient.authenticateUser(Mockito.any(AuthenticateUserRequest.class)))
            .thenAnswer(invocation ->
                CompletableFuture.completedFuture(
                    new AuthenticateUserReply((AuthenticateUserRequest)invocation.getArgument(0))
                        .setSuccess(true)
                        .setValid(valid)
                        .setUser(
                            valid
                                ? new UserData()
                                    .setUserId(USER_ID)
                                    .setUserName(USER_NAME)
                                    .setAuthorities(authorities)
                                    .setValues(values)
                                : null)));
    }

    private void
    stubMerge(boolean success)
    {
        Mockito
            .when(userCommandServiceClient.mergeUserValues(Mockito.any(MergeUserValuesRequest.class)))
            .thenAnswer(invocation ->
                CompletableFuture.completedFuture(
                    new MergeUserValuesReply((MergeUserValuesRequest)invocation.getArgument(0)).setSuccess(success)));
    }

    private void
    stubUpdate(boolean success)
    {
        Mockito
            .when(userCommandServiceClient.updateUser(Mockito.any(UpdateUserRequest.class)))
            .thenAnswer(invocation ->
                CompletableFuture.completedFuture(
                    new UpdateUserReply((UpdateUserRequest)invocation.getArgument(0)).setSuccess(success)));
    }
}

//////////////////////////////////////////////////////////////////////////////
