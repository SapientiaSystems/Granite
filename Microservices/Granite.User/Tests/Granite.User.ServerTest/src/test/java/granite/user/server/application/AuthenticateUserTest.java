//////////////////////////////////////////////////////////////////////////////
// AuthenticateUserTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.domain.IUserRepository;
import granite.user.server.domain.User;
import granite.user.service.event.IUserEventSender;
import granite.user.service.requestreply.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("CommitStage")
public
class AuthenticateUserTest
{
    private static final Long   USER_ID    = 101L;
    private static final String USER_NAME  = "alice@example.com";
    private static final String PLAINTEXT  = "hunter2";

    private IUserRepository    repository;
    private PasswordEncoder    encoder;
    private UserCommandService subject;

    @BeforeEach
    public void
    setUp()
    {
        repository = Mockito.mock(IUserRepository.class);
        encoder    = new BCryptPasswordEncoder(4);
        subject =
            new UserCommandService(
                repository,
                Mockito.mock(IUserEventSender.class),
                new ModelMapper().registerModule(new MappingModule()),
                encoder);
    }

    @Test
    public void
    returnsValidTrueWithUserIdForCorrectPassword()
    {
        stubUser(
            new User()
                .setPrimaryId(USER_ID)
                .setUserName(USER_NAME)
                .setPassword(encoder.encode(PLAINTEXT)));

        AuthenticateUserReply reply =
            subject
                .authenticateUser(
                    new AuthenticateUserRequest()
                        .setUserName(USER_NAME)
                        .setCredential(PLAINTEXT)
                        .setCredentialKind(CredentialKind.PASSWORD))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertTrue(reply.getValid());
        assertEquals(USER_ID,reply.getUserId());
    }

    @Test
    public void
    returnsValidFalseForWrongPassword()
    {
        stubUser(
            new User()
                .setPrimaryId(USER_ID)
                .setUserName(USER_NAME)
                .setPassword(encoder.encode(PLAINTEXT)));

        AuthenticateUserReply reply =
            subject
                .authenticateUser(
                    new AuthenticateUserRequest()
                        .setUserName(USER_NAME)
                        .setCredential("wrong-password")
                        .setCredentialKind(CredentialKind.PASSWORD))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertFalse(reply.getValid());
        assertNull(reply.getUserId());
    }

    @Test
    public void
    returnsValidFalseForAbsentUser()
    {
        Mockito
            .when(repository.findByUserName("missing"))
            .thenReturn(Optional.empty());

        AuthenticateUserReply reply =
            subject
                .authenticateUser(
                    new AuthenticateUserRequest()
                        .setUserName("missing")
                        .setCredential(PLAINTEXT)
                        .setCredentialKind(CredentialKind.PASSWORD))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertFalse(reply.getValid());
        assertNull(reply.getUserId());
    }

    @Test
    public void
    returnsValidFalseWithUnsupportedKindMessageForAccessToken()
    {
        stubUser(
            new User()
                .setPrimaryId(USER_ID)
                .setUserName(USER_NAME)
                .setPassword(encoder.encode(PLAINTEXT)));

        AuthenticateUserReply reply =
            subject
                .authenticateUser(
                    new AuthenticateUserRequest()
                        .setUserName(USER_NAME)
                        .setCredential("some-token")
                        .setCredentialKind(CredentialKind.ACCESS_TOKEN))
                .toCompletableFuture()
                .join();

        assertFalse(reply.getValid());
        assertNull(reply.getUserId());
        assertEquals(
            "CredentialKind ACCESS_TOKEN is not supported by Granite.User.authenticateUser",
            reply.getFailureMessage());
    }

    @Test
    public void
    returnsValidFalseWithUnsupportedKindMessageForSecurityCode()
    {
        stubUser(
            new User()
                .setPrimaryId(USER_ID)
                .setUserName(USER_NAME)
                .setPassword(encoder.encode(PLAINTEXT)));

        AuthenticateUserReply reply =
            subject
                .authenticateUser(
                    new AuthenticateUserRequest()
                        .setUserName(USER_NAME)
                        .setCredential("123456")
                        .setCredentialKind(CredentialKind.SECURITY_CODE))
                .toCompletableFuture()
                .join();

        assertFalse(reply.getValid());
        assertNull(reply.getUserId());
        assertEquals(
            "CredentialKind SECURITY_CODE is not supported by Granite.User.authenticateUser",
            reply.getFailureMessage());
    }

    @Test
    public void
    returnsValidFalseForUserWithNullPassword()
    {
        stubUser(
            new User()
                .setPrimaryId(USER_ID)
                .setUserName(USER_NAME)
                .setPassword(null));

        AuthenticateUserReply reply =
            subject
                .authenticateUser(
                    new AuthenticateUserRequest()
                        .setUserName(USER_NAME)
                        .setCredential(PLAINTEXT)
                        .setCredentialKind(CredentialKind.PASSWORD))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertFalse(reply.getValid());
        assertNull(reply.getUserId());
    }

    private void
    stubUser(User user)
    {
        Mockito
            .when(repository.findByUserName(user.getUserName()))
            .thenReturn(Optional.of(user));
    }
}

//////////////////////////////////////////////////////////////////////////////
