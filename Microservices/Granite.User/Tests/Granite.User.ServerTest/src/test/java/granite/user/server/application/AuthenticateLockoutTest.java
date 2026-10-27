//////////////////////////////////////////////////////////////////////////////
// AuthenticateLockoutTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.user.server.domain.IUserRepository;
import granite.user.server.domain.User;
import granite.user.service.event.IUserEventSender;
import granite.user.service.requestreply.AuthenticateUserReply;
import granite.user.service.requestreply.AuthenticateUserRequest;
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
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class AuthenticateLockoutTest
{
    private static final String   USER_NAME = "alice@example.com";
    private static final String   PASSWORD  = "hunter2";
    private static final Instant  NOW       = Instant.parse("2026-10-20T12:00:00Z");
    private static final Duration LOCK      = Duration.ofMinutes(15);

    private IUserRepository          repository;
    private IUserEventSender         sender;
    private PasswordEncoder          encoder;
    private AtomicReference<Instant> now;
    private UserCommandService       subject;
    private User                     user;

    @BeforeEach
    public void
    setUp()
    {
        Clock clock = Mockito.mock(Clock.class);

        repository = Mockito.mock(IUserRepository.class);
        sender     = Mockito.mock(IUserEventSender.class);
        encoder    = Mockito.spy(new BCryptPasswordEncoder(4));
        now        = new AtomicReference<>(NOW);
        subject    =
            new UserCommandService(
                repository,
                sender,
                new ModelMapper().registerModule(new MappingModule()),
                encoder,
                new LoginLockoutPolicy(5,LOCK,clock),
                new GraniteApplicationRolesManagerSupplier().get());
        user       =
            new User()
                .setPrimaryId(101L)
                .setUserName(USER_NAME)
                .setPassword(encoder.encode(PASSWORD));

        Mockito.when(clock.instant()).thenAnswer(invocation -> now.get());
        Mockito.when(repository.findByUserName(USER_NAME)).thenReturn(Optional.of(user));
        Mockito.when(repository.save(Mockito.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.clearInvocations(encoder);
    }

    @Test
    public void
    unknownUserStillPaysForPasswordComparison()
    {
        Mockito.when(repository.findByUserName("missing")).thenReturn(Optional.empty());

        assertFalse(authenticate("missing",PASSWORD).getValid());
        Mockito
            .verify(encoder,Mockito.times(1))
            .matches(Mockito.eq(PASSWORD),Mockito.argThat(hash -> hash != null && hash.startsWith("$2")));
    }

    @Test
    public void
    userWithoutPasswordStillPaysForPasswordComparison()
    {
        user.setPassword(null);

        assertFalse(authenticate(USER_NAME,PASSWORD).getValid());
        Mockito.verify(encoder,Mockito.times(1)).matches(Mockito.eq(PASSWORD),Mockito.anyString());
    }

    @Test
    public void
    failedAttemptIsCountedAndPersistedWithoutEvents()
    {
        assertFalse(authenticate(USER_NAME,"wrong").getValid());

        assertEquals(1,user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
        Mockito.verify(repository).save(user);
        Mockito.verifyNoInteractions(sender);
    }

    @Test
    public void
    accountLocksAfterMaxAttemptsAndRejectsCorrectPassword()
    {
        for (int i = 0;i < 5;i++)
            assertFalse(authenticate(USER_NAME,"wrong").getValid());

        assertEquals(NOW.plus(LOCK),user.getLockedUntil());

        AuthenticateUserReply reply = authenticate(USER_NAME,PASSWORD);

        assertFalse(reply.getValid());
        assertEquals("account locked",reply.getFailureMessage());
        assertEquals(NOW.plus(LOCK),user.getLockedUntil(),"a locked attempt must not extend the lock");
    }

    @Test
    public void
    lockExpiresAndSuccessResetsCounters()
    {
        for (int i = 0;i < 5;i++)
            authenticate(USER_NAME,"wrong");

        now.set(NOW.plus(LOCK).plusSeconds(1));

        assertTrue(authenticate(USER_NAME,PASSWORD).getValid());
        assertEquals(0,user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
    }

    @Test
    public void
    successBeforeLimitResetsCounter()
    {
        authenticate(USER_NAME,"wrong");
        authenticate(USER_NAME,"wrong");

        assertTrue(authenticate(USER_NAME,PASSWORD).getValid());
        assertEquals(0,user.getFailedLoginAttempts());
    }

    @Test
    public void
    successWithCleanCounterDoesNotWrite()
    {
        assertTrue(authenticate(USER_NAME,PASSWORD).getValid());
        Mockito.verify(repository,Mockito.never()).save(Mockito.any(User.class));
    }

    private AuthenticateUserReply
    authenticate(String userName,String credential)
    {
        return
            subject
                .authenticateUser(
                    new AuthenticateUserRequest()
                        .setUserName(userName)
                        .setCredential(credential)
                        .setCredentialKind(CredentialKind.PASSWORD))
                .toCompletableFuture()
                .join();
    }
}

//////////////////////////////////////////////////////////////////////////////
