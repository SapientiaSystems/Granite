//////////////////////////////////////////////////////////////////////////////
// ChangeUserPasswordTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.user.server.domain.IUserRepository;
import granite.user.server.domain.User;
import granite.user.service.event.IUserEventSender;
import granite.user.service.requestreply.AuthenticateUserReply;
import granite.user.service.requestreply.AuthenticateUserRequest;
import granite.user.service.requestreply.ChangeUserPasswordReply;
import granite.user.service.requestreply.ChangeUserPasswordRequest;
import granite.user.service.requestreply.CredentialKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class ChangeUserPasswordTest
{
    private static final Long     USER_ID   = 101L;
    private static final String   USER_NAME = "alice@example.com";
    private static final String   PASSWORD  = "hunter2-old";
    private static final String   NEW       = "hunter2-new";
    private static final Instant  NOW       = Instant.parse("2026-11-16T12:00:00Z");
    private static final Duration LOCK      = Duration.ofMinutes(15);

    private IUserRepository    repository;
    private IUserEventSender   sender;
    private PasswordEncoder    encoder;
    private UserCommandService subject;
    private User               user;

    @BeforeEach
    public void
    setUp()
    {
        Clock clock = Mockito.mock(Clock.class);

        repository = Mockito.mock(IUserRepository.class);
        sender     = Mockito.mock(IUserEventSender.class);
        encoder    = Mockito.spy(new BCryptPasswordEncoder(4));
        subject    =
            new UserCommandService(
                repository,
                sender,
                new ModelMapper().registerModule(new MappingModule()),
                encoder,
                new LoginLockoutPolicy(3,LOCK,clock),
                new GraniteApplicationRolesManagerSupplier().get(),
                new PasswordPolicy(8,128));
        user       =
            new User()
                .setPrimaryId(USER_ID)
                .setUserName(USER_NAME)
                .setPassword(encoder.encode(PASSWORD));

        Mockito.when(clock.instant()).thenReturn(NOW);
        Mockito.when(repository.findById(USER_ID)).thenReturn(Optional.of(user));
        Mockito.when(repository.findByUserName(USER_NAME)).thenReturn(Optional.of(user));
        Mockito.when(repository.save(Mockito.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.clearInvocations(encoder);
    }

    @Test
    public void
    successStoresNewHashAndFiresUpdateEvent()
    {
        ChangeUserPasswordReply reply = change(USER_ID,PASSWORD,NEW);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertTrue(authenticate(NEW).getValid());
        assertFalse(authenticate(PASSWORD).getValid());
        assertFalse(user.getPassword().contains(NEW));
        Mockito.verify(sender,Mockito.times(1)).send(Mockito.any());
    }

    @Test
    public void
    wrongCurrentPasswordIsRefusedAndCounted()
    {
        ChangeUserPasswordReply reply = change(USER_ID,"wrong-password",NEW);

        assertFalse(reply.isSuccess());
        assertEquals("invalid credentials",reply.getFailureMessage());
        assertEquals(1,user.getFailedLoginAttempts());
        assertTrue(authenticate(PASSWORD).getValid());
    }

    @Test
    public void
    repeatedWrongCurrentPasswordLocksLoginToo()
    {
        for (int i = 0;i < 3;i++)
            assertFalse(change(USER_ID,"wrong-password",NEW).isSuccess());

        assertNotNull(user.getLockedUntil());

        AuthenticateUserReply login = authenticate(PASSWORD);

        assertFalse(login.getValid());
        assertEquals("account locked",login.getFailureMessage());
    }

    @Test
    public void
    lockedAccountRefusesEvenWithCorrectCurrentPassword()
    {
        for (int i = 0;i < 3;i++)
            change(USER_ID,"wrong-password",NEW);

        ChangeUserPasswordReply reply = change(USER_ID,PASSWORD,NEW);

        assertFalse(reply.isSuccess());
        assertEquals("invalid credentials",reply.getFailureMessage());
        assertFalse(authenticate(NEW).getValid());
    }

    @Test
    public void
    tooShortNewPasswordIsRefused()
    {
        ChangeUserPasswordReply reply = change(USER_ID,PASSWORD,"1234567");

        assertFalse(reply.isSuccess());
        assertEquals("password must be at least 8 characters",reply.getFailureMessage());
        assertTrue(authenticate(PASSWORD).getValid());
    }

    @Test
    public void
    tooLongNewPasswordIsRefused()
    {
        ChangeUserPasswordReply reply = change(USER_ID,PASSWORD,"x".repeat(129));

        assertFalse(reply.isSuccess());
        assertEquals("password must be at most 128 characters",reply.getFailureMessage());
    }

    @Test
    public void
    samePasswordIsRefused()
    {
        ChangeUserPasswordReply reply = change(USER_ID,PASSWORD,PASSWORD);

        assertFalse(reply.isSuccess());
        assertEquals("new password must differ from the current one",reply.getFailureMessage());
    }

    @Test
    public void
    unknownUserIsRefusedAfterOneComparison()
    {
        Mockito.when(repository.findById(999L)).thenReturn(Optional.empty());

        ChangeUserPasswordReply reply = change(999L,PASSWORD,NEW);

        assertFalse(reply.isSuccess());
        assertEquals("invalid credentials",reply.getFailureMessage());
        Mockito
            .verify(encoder,Mockito.times(1))
            .matches(Mockito.eq(PASSWORD),Mockito.argThat(hash -> hash != null && hash.startsWith("$2")));
    }

    @Test
    public void
    userWithoutPasswordIsRefused()
    {
        user.setPassword(null);

        ChangeUserPasswordReply reply = change(USER_ID,PASSWORD,NEW);

        assertFalse(reply.isSuccess());
        assertEquals("invalid credentials",reply.getFailureMessage());
        Mockito.verify(encoder,Mockito.times(1)).matches(Mockito.eq(PASSWORD),Mockito.anyString());
    }

    @Test
    public void
    requestToStringOmitsPasswords()
    {
        String text = new ChangeUserPasswordRequest()
            .setUserId(USER_ID)
            .setCurrentPassword(PASSWORD)
            .setNewPassword(NEW)
            .toString();

        assertFalse(text.contains(PASSWORD),text);
        assertFalse(text.contains(NEW),text);
    }

    private ChangeUserPasswordReply
    change(Long userId,String current,String next)
    {
        return
            subject
                .changeUserPassword(
                    new ChangeUserPasswordRequest()
                        .setUserId(userId)
                        .setCurrentPassword(current)
                        .setNewPassword(next))
                .toCompletableFuture()
                .join();
    }

    private AuthenticateUserReply
    authenticate(String credential)
    {
        return
            subject
                .authenticateUser(
                    new AuthenticateUserRequest()
                        .setUserName(USER_NAME)
                        .setCredential(credential)
                        .setCredentialKind(CredentialKind.PASSWORD))
                .toCompletableFuture()
                .join();
    }
}

//////////////////////////////////////////////////////////////////////////////
