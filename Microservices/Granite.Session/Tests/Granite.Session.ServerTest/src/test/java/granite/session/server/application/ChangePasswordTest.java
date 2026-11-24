//////////////////////////////////////////////////////////////////////////////
// ChangePasswordTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.session.server.platform.MockSessionEventSender;
import granite.session.server.domain.IOktaLoginTransactionRepository;
import granite.session.server.domain.ISessionRepository;
import granite.session.server.domain.Session;
import granite.session.service.event.SessionEvent;
import granite.session.service.requestreply.ChangePasswordReply;
import granite.session.service.requestreply.ChangePasswordRequest;
import granite.session.service.requestreply.LoginReply;
import granite.session.service.requestreply.LoginRequest;
import granite.session.service.requestreply.SessionClosedReason;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.ChangeUserPasswordReply;
import granite.user.service.requestreply.ChangeUserPasswordRequest;
import granite.user.service.requestreply.CredentialKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import strata.foundation.core.event.StandardEventType;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class ChangePasswordTest
{
    private static final Duration TTL     = Duration.ofMinutes(15);
    private static final Instant  NOW     = Instant.parse("2026-10-06T12:00:00Z");
    private static final String   CURRENT = "current-secret-1";
    private static final String   NEXT    = "next-secret-22";

    private ISessionRepository        repository;
    private IUserCommandServiceClient userClient;
    private MockSessionEventSender    sender;

    @BeforeEach
    public void
    setUp()
    {
        repository = Mockito.mock(ISessionRepository.class);
        userClient = Mockito.mock(IUserCommandServiceClient.class);
        sender     = new MockSessionEventSender();
    }

    @Test
    public void
    successClosesOtherSessionsAndKeepsCurrent()
    {
        Session current = open(901L,1L,"device-A");
        Session other   = open(902L,1L,"device-B");

        Mockito.when(repository.findById(901L)).thenReturn(Optional.of(current));
        Mockito.when(repository.findOpenByUserId(1L)).thenReturn(List.of(current,other));
        stubUser(true,null);

        ChangePasswordReply reply = change(subject(true),901L);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertFalse(current.isClosed());
        assertEquals(SessionClosedReason.PASSWORD_CHANGED,other.getClosedReason());
        Mockito.verify(repository).save(other);
        Mockito.verify(repository,Mockito.never()).save(current);

        List<SessionEvent> events = sender.getEvents();

        assertEquals(1,events.size());
        assertEquals(StandardEventType.UPDATED,events.get(0).getEventType());
        assertEquals("PASSWORD_CHANGED",events.get(0).getClosedReason());

        ArgumentCaptor<ChangeUserPasswordRequest> captor = ArgumentCaptor.forClass(ChangeUserPasswordRequest.class);

        Mockito.verify(userClient).changeUserPassword(captor.capture());
        assertEquals(1L,captor.getValue().getUserId());
        assertEquals(CURRENT,captor.getValue().getCurrentPassword());
        assertEquals(NEXT,captor.getValue().getNewPassword());
    }

    @Test
    public void
    userFailureMessagePassesThroughAndNothingIsClosed()
    {
        Session current = open(901L,1L,"device-A");
        Session other   = open(902L,1L,"device-B");

        Mockito.when(repository.findById(901L)).thenReturn(Optional.of(current));
        Mockito.when(repository.findOpenByUserId(1L)).thenReturn(List.of(current,other));
        stubUser(false,"password must be at least 8 characters");

        ChangePasswordReply reply = change(subject(true),901L);

        assertFalse(reply.isSuccess());
        assertEquals("password must be at least 8 characters",reply.getFailureMessage());
        assertFalse(other.isClosed());
        assertTrue(sender.getEvents().isEmpty());
    }

    @Test
    public void
    userExceptionGivesGenericFailure()
    {
        Session current = open(901L,1L,"device-A");

        Mockito.when(repository.findById(901L)).thenReturn(Optional.of(current));
        Mockito
            .when(userClient.changeUserPassword(Mockito.any()))
            .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("boom")));

        ChangePasswordReply reply = change(subject(true),901L);

        assertFalse(reply.isSuccess());
        assertEquals("Unable to change password",reply.getFailureMessage());
        Mockito.verify(repository,Mockito.never()).findOpenByUserId(Mockito.any());
    }

    @Test
    public void
    disabledRefusesWithoutCallingUser()
    {
        ChangePasswordReply reply = change(subject(false),901L);

        assertFalse(reply.isSuccess());
        assertEquals("password change is not available",reply.getFailureMessage());
        Mockito.verify(userClient,Mockito.never()).changeUserPassword(Mockito.any());
        Mockito.verify(repository,Mockito.never()).findById(Mockito.any());
    }

    @Test
    public void
    closedSessionRefusedWithoutCallingUser()
    {
        Session closed = open(901L,1L,"device-A").close(SessionClosedReason.LOGOUT,NOW.minusSeconds(30));

        Mockito.when(repository.findById(901L)).thenReturn(Optional.of(closed));

        ChangePasswordReply reply = change(subject(true),901L);

        assertFalse(reply.isSuccess());
        assertEquals("invalid credentials",reply.getFailureMessage());
        Mockito.verify(userClient,Mockito.never()).changeUserPassword(Mockito.any());
    }

    @Test
    public void
    unknownSessionRefusedWithoutCallingUser()
    {
        Mockito.when(repository.findById(901L)).thenReturn(Optional.empty());

        ChangePasswordReply reply = change(subject(true),901L);

        assertFalse(reply.isSuccess());
        assertEquals("invalid credentials",reply.getFailureMessage());
        Mockito.verify(userClient,Mockito.never()).changeUserPassword(Mockito.any());
    }

    @Test
    public void
    expiredSessionRefusedWithoutCallingUser()
    {
        Session expired = open(901L,1L,"device-A").setExpires(NOW.minusSeconds(1));

        Mockito.when(repository.findById(901L)).thenReturn(Optional.of(expired));

        ChangePasswordReply reply = change(subject(true),901L);

        assertFalse(reply.isSuccess());
        assertEquals("invalid credentials",reply.getFailureMessage());
        Mockito.verify(userClient,Mockito.never()).changeUserPassword(Mockito.any());
    }

    @Test
    public void
    passwordLoginRefusedWhenDisabled()
    {
        LoginReply reply = login(subject(false),CredentialKind.PASSWORD);

        assertFalse(reply.isSuccess());
        assertEquals("Password login is not available",reply.getFailureMessage());
        assertNull(reply.getAccessToken());
        Mockito.verify(userClient,Mockito.never()).authenticateUser(Mockito.any());
    }

    @Test
    public void
    accessTokenLoginIsNotBlockedWhenPasswordDisabled()
    {
        Mockito.when(repository.findByAccessToken("token.unknown")).thenReturn(Optional.empty());

        LoginReply reply = login(subject(false),CredentialKind.ACCESS_TOKEN);

        assertFalse(reply.isSuccess());
        assertEquals("invalid credentials",reply.getFailureMessage());
        Mockito.verify(repository).findByAccessToken("token.unknown");
    }

    @Test
    public void
    requestToStringOmitsPasswords()
    {
        String text =
            new ChangePasswordRequest()
                .setSessionId(5L)
                .setCurrentPassword(CURRENT)
                .setNewPassword(NEXT)
                .toString();

        assertNotNull(text);
        assertFalse(text.contains(CURRENT));
        assertFalse(text.contains(NEXT));
    }

    private void
    stubUser(boolean success,String failureMessage)
    {
        Mockito
            .when(userClient.changeUserPassword(Mockito.any()))
            .thenReturn(
                CompletableFuture.completedFuture(
                    new ChangeUserPasswordReply()
                        .setSuccess(success)
                        .setFailureMessage(failureMessage)));
    }

    private Session
    open(Long sessionId,Long userId,String deviceId)
    {
        return
            new Session()
                .setSessionId(sessionId)
                .setUserId(userId)
                .setDeviceId(deviceId)
                .setExpires(NOW.plus(TTL))
                .setAccessToken("token." + sessionId);
    }

    private SessionCommandService
    subject(boolean passwordEnabled)
    {
        return
            new SessionCommandService(
                repository,
                Mockito.mock(IOktaLoginTransactionRepository.class),
                Mockito.mock(IJwtTokenService.class),
                new RefreshTokenService(),
                userClient,
                Mockito.mock(IUserQueryServiceClient.class),
                Mockito.mock(IOktaClient.class),
                new OktaSettings(),
                new PasswordLoginSettings().setEnabled(passwordEnabled),
                new ApplicationLoginGate(List.of(Session.DEFAULT_APPLICATION),new RequiredAuthoritiesLoginGate(List.of()),Map.of()),
                sender,
                new ModelMapper().registerModule(new MappingModule()),
                Duration.ofHours(24),
                Duration.ofMinutes(2),
                Clock.fixed(NOW,ZoneOffset.UTC),
                new GraniteApplicationRolesManagerSupplier().get(),
                null);
    }

    private ChangePasswordReply
    change(SessionCommandService subject,Long sessionId)
    {
        return
            subject
                .changePassword(
                    new ChangePasswordRequest()
                        .setSessionId(sessionId)
                        .setCurrentPassword(CURRENT)
                        .setNewPassword(NEXT))
                .toCompletableFuture()
                .join();
    }

    private LoginReply
    login(SessionCommandService subject,CredentialKind kind)
    {
        return
            subject
                .login(
                    new LoginRequest()
                        .setUserName("alice")
                        .setCredential(kind == CredentialKind.PASSWORD ? CURRENT : "token.unknown")
                        .setCredentialKind(kind)
                        .setDeviceId("device-A"))
                .toCompletableFuture()
                .join();
    }
}

//////////////////////////////////////////////////////////////////////////////
