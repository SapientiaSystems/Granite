//////////////////////////////////////////////////////////////////////////////
// UserPasswordHashingTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.foundation.core.security.GraniteApplicationRoles;
import granite.user.server.domain.IUserRepository;
import granite.user.server.domain.User;
import granite.user.service.event.IUserEventSender;
import granite.user.service.requestreply.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("CommitStage")
public
class UserPasswordHashingTest
{
    private static final Long USER_ID = 42L;

    private IUserRepository repository;
    private PasswordEncoder encoder;
    private UserCommandService subject;
    private User stored;

    @BeforeEach
    public void
    setUp()
    {
        repository = Mockito.mock(IUserRepository.class);
        encoder    = new BCryptPasswordEncoder(4);
        stored     = new User().setPrimaryId(USER_ID);

        Mockito
            .when(repository.save(Mockito.any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito
            .when(repository.findById(USER_ID))
            .thenReturn(Optional.of(stored));

        subject =
            new UserCommandService(
                repository,
                Mockito.mock(IUserEventSender.class),
                new ModelMapper().registerModule(new MappingModule()),
                encoder,
                LoginLockoutPolicy.defaults(),
                GraniteApplicationRoles.create());
    }

    @Test
    public void
    createUserHashesPasswordBeforePersist()
    {
        CreateUserReply reply =
            subject
                .createUser(
                    new CreateUserRequest()
                        .setUserToCreate(
                            new CreateUserData()
                                .setUserName("alice")
                                .setPassword("hunter2")))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());

        User persisted = savedUser();

        assertNotEquals("hunter2",persisted.getPassword());
        assertTrue(encoder.matches("hunter2",persisted.getPassword()));
        assertEquals("alice",persisted.getUserName());
    }

    @Test
    public void
    createUserWithNullPasswordPersistsNullPassword()
    {
        CreateUserReply reply =
            subject
                .createUser(
                    new CreateUserRequest()
                        .setUserToCreate(new CreateUserData().setUserName("bob")))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertNull(savedUser().getPassword());
    }

    @Test
    public void
    createUserCopiesExternalIdentity()
    {
        subject
            .createUser(
                new CreateUserRequest()
                    .setUserToCreate(
                        new CreateUserData().setExternalIdentity("ext-1")))
            .toCompletableFuture()
            .join();

        assertEquals("ext-1",savedUser().getExternalIdentity());
    }

    @Test
    public void
    updateUserHashesPasswordWhenProvided()
    {
        UpdateUserReply reply =
            update(new UpdateUserData().setUserId(USER_ID).setPassword("s3cret"));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertNotEquals("s3cret",stored.getPassword());
        assertTrue(encoder.matches("s3cret",stored.getPassword()));
    }

    @Test
    public void
    updateUserPreservesPasswordWhenNull()
    {
        String existing = encoder.encode("original");
        stored.setPassword(existing);

        UpdateUserReply reply =
            update(new UpdateUserData().setUserId(USER_ID).setUserName("changed@example.com"));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(existing,stored.getPassword());
        assertEquals("changed@example.com",stored.getUserName());
    }

    @Test
    public void
    updateUserPreservesPasswordWhenBlank()
    {
        String existing = encoder.encode("original");
        stored.setPassword(existing);

        UpdateUserReply reply =
            update(new UpdateUserData().setUserId(USER_ID).setPassword("   "));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(existing,stored.getPassword());
    }

    @Test
    public void
    updateUserCopiesUserNameWhenProvided()
    {
        UpdateUserReply reply =
            update(new UpdateUserData().setUserId(USER_ID).setUserName("carol"));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals("carol",stored.getUserName());
    }

    @Test
    public void
    updateUserCopiesExternalIdentityWhenProvided()
    {
        UpdateUserReply reply =
            update(new UpdateUserData().setUserId(USER_ID).setExternalIdentity("ext-2"));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals("ext-2",stored.getExternalIdentity());
    }

    @Test
    public void
    createUserWithBlankPasswordPersistsNull()
    {
        CreateUserReply reply =
            subject
                .createUser(
                    new CreateUserRequest()
                        .setUserToCreate(
                            new CreateUserData()
                                .setUserName("dave")
                                .setPassword("")))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertNull(savedUser().getPassword());
    }

    @Test
    public void
    updateUserWithBlankUserNamePreservesExistingUserName()
    {
        stored.setUserName("original-name");

        UpdateUserReply reply =
            update(new UpdateUserData().setUserId(USER_ID).setUserName("  "));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals("original-name",stored.getUserName());

        reply = update(new UpdateUserData().setUserId(USER_ID).setUserName(""));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals("original-name",stored.getUserName());
    }

    @Test
    public void
    updateUserWithBlankExternalIdentityPreservesExistingExternalIdentity()
    {
        stored.setExternalIdentity("original-ext");

        UpdateUserReply reply =
            update(new UpdateUserData().setUserId(USER_ID).setExternalIdentity(""));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals("original-ext",stored.getExternalIdentity());
    }

    @Test
    public void
    createUsersHashesEachPassword()
    {
        CreateUsersReply reply =
            subject
                .createUsers(
                    new CreateUsersRequest()
                        .setUsersToCreate(
                            List.of(
                                new CreateUserData().setUserName("u1").setPassword("pw-one"),
                                new CreateUserData().setUserName("u2").setPassword("pw-two"))))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        Mockito.verify(repository,Mockito.times(2)).save(captor.capture());

        List<User> saved = captor.getAllValues();

        assertEquals("u1",saved.get(0).getUserName());
        assertNotEquals("pw-one",saved.get(0).getPassword());
        assertTrue(encoder.matches("pw-one",saved.get(0).getPassword()));
        assertEquals("u2",saved.get(1).getUserName());
        assertNotEquals("pw-two",saved.get(1).getPassword());
        assertTrue(encoder.matches("pw-two",saved.get(1).getPassword()));
    }

    @Test
    public void
    updateUsersHashesEachProvidedPassword()
    {
        User other = new User().setPrimaryId(43L);

        stubFindAll(stored,other);

        UpdateUsersReply reply =
            updateAll(
                new UpdateUserData().setUserId(USER_ID).setPassword("pw-one"),
                new UpdateUserData().setUserId(43L).setPassword("pw-two"));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertNotEquals("pw-one",stored.getPassword());
        assertTrue(encoder.matches("pw-one",stored.getPassword()));
        assertNotEquals("pw-two",other.getPassword());
        assertTrue(encoder.matches("pw-two",other.getPassword()));
    }

    @Test
    public void
    updateUsersPreservesPasswordWhenAbsent()
    {
        User other = new User().setPrimaryId(43L);
        String existingOne = encoder.encode("original-one");
        String existingTwo = encoder.encode("original-two");

        stored.setPassword(existingOne);
        other.setPassword(existingTwo);
        stubFindAll(stored,other);

        UpdateUsersReply reply =
            updateAll(
                new UpdateUserData().setUserId(USER_ID).setUserName("changed@example.com"),
                new UpdateUserData().setUserId(43L).setPassword(""));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(existingOne,stored.getPassword());
        assertEquals(existingTwo,other.getPassword());
    }

    private void
    stubFindAll(User first,User second)
    {
        Map<Long,Optional<User>> found = new HashMap<>();

        found.put(first.getPrimaryId(),Optional.of(first));
        found.put(second.getPrimaryId(),Optional.of(second));

        Mockito
            .when(repository.findAllByIdIn(Mockito.anySet()))
            .thenReturn(found);
    }

    private UpdateUsersReply
    updateAll(UpdateUserData first,UpdateUserData second)
    {
        return
            subject
                .updateUsers(
                    new UpdateUsersRequest().setUsersToUpdate(List.of(first,second)))
                .toCompletableFuture()
                .join();
    }

    private UpdateUserReply
    update(UpdateUserData data)
    {
        return
            subject
                .updateUser(new UpdateUserRequest().setUserToUpdate(data))
                .toCompletableFuture()
                .join();
    }

    private User
    savedUser()
    {
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        Mockito.verify(repository).save(captor.capture());
        return captor.getValue();
    }
}

//////////////////////////////////////////////////////////////////////////////
