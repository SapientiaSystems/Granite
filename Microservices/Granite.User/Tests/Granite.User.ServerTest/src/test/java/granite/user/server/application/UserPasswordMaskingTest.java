//////////////////////////////////////////////////////////////////////////////
// UserPasswordMaskingTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("CommitStage")
public
class UserPasswordMaskingTest
{
    private static final Long   USER_ID   = 42L;
    private static final String USER_NAME = "alice@example.com";
    private static final String PLAINTEXT = "hunter2";
    private static final String MASK      = "********";

    private IUserRepository    repository;
    private PasswordEncoder    encoder;
    private ModelMapper        mapper;
    private UserCommandService commandService;
    private UserQueryService   queryService;
    private ObjectMapper       json;
    private User               stored;

    @BeforeEach
    public void
    setUp()
    {
        repository = Mockito.mock(IUserRepository.class);
        encoder    = new BCryptPasswordEncoder(4);
        mapper     = new ModelMapper().registerModule(new MappingModule());
        json       = new ObjectMapper().findAndRegisterModules();
        stored     =
            new User()
                .setPrimaryId(USER_ID)
                .setUserName(USER_NAME)
                .setPassword(encoder.encode(PLAINTEXT));

        Mockito
            .when(repository.save(Mockito.any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito
            .when(repository.findById(USER_ID))
            .thenReturn(Optional.of(stored));
        Mockito
            .when(repository.findByUserName(USER_NAME))
            .thenReturn(Optional.of(stored));

        commandService =
            new UserCommandService(
                repository,
                Mockito.mock(IUserEventSender.class),
                mapper,
                encoder);
        queryService = new UserQueryService(repository,mapper);
    }

    @Test
    public void
    mapsNonBlankPasswordToMask()
    {
        UserData data = mapper.map(stored,UserData.class);

        assertEquals(MASK,data.getPassword());
    }

    @Test
    public void
    mapsNullOrBlankPasswordToNull()
    {
        assertNull(mapper.map(new User().setPrimaryId(1L),UserData.class).getPassword());
        assertNull(
            mapper
                .map(new User().setPrimaryId(2L).setPassword("   "),UserData.class)
                .getPassword());
        assertNull(
            mapper
                .map(new User().setPrimaryId(3L).setPassword(""),UserData.class)
                .getPassword());
    }

    @Test
    public void
    createUserReplyIsMasked()
        throws Exception
    {
        CreateUserReply reply =
            commandService
                .createUser(
                    new CreateUserRequest()
                        .setUserToCreate(
                            new CreateUserData()
                                .setUserName("bob@example.com")
                                .setPassword(PLAINTEXT)))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(MASK,reply.getCreatedUser().getPassword());
        assertNoHash(reply);
    }

    @Test
    public void
    updateUserReplyIsMasked()
        throws Exception
    {
        UpdateUserReply reply =
            commandService
                .updateUser(
                    new UpdateUserRequest()
                        .setUserToUpdate(
                            new UpdateUserData()
                                .setUserId(USER_ID)
                                .setPassword("s3cret")))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(MASK,reply.getUpdatedUser().getPassword());
        assertNoHash(reply);
    }

    @Test
    public void
    findAllUsersIsMasked()
        throws Exception
    {
        Mockito.when(repository.findAllUsers()).thenReturn(List.of(stored));

        FindAllUsersReply reply =
            queryService
                .findAllUsers(new FindAllUsersRequest())
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(MASK,reply.getUsers().get(0).getPassword());
        assertNoHash(reply);
    }

    @Test
    public void
    authenticateUserReplyIsMasked()
        throws Exception
    {
        AuthenticateUserReply reply =
            commandService
                .authenticateUser(
                    new AuthenticateUserRequest()
                        .setUserName(USER_NAME)
                        .setCredential(PLAINTEXT)
                        .setCredentialKind(CredentialKind.PASSWORD))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertTrue(reply.getValid());
        assertEquals(MASK,reply.getUser().getPassword());
        assertNoHash(reply);
    }

    private void
    assertNoHash(Object reply)
        throws Exception
    {
        String serialized = json.writeValueAsString(reply);

        assertFalse(serialized.contains("$2"),serialized);
        assertFalse(serialized.contains(stored.getPassword()),serialized);
    }
}

//////////////////////////////////////////////////////////////////////////////
