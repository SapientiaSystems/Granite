//////////////////////////////////////////////////////////////////////////////
// PasswordPolicyEnforcementTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
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
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class PasswordPolicyEnforcementTest
{
    private static final Long   USER_ID  = 42L;
    private static final String SHORT    = "short";
    private static final String MESSAGE  = "password must be at least 8 characters";
    private static final String ORIGINAL = "original-password";

    private IUserRepository    repository;
    private PasswordEncoder    encoder;
    private UserCommandService subject;
    private User               stored;
    private String             originalHash;

    @BeforeEach
    public void
    setUp()
    {
        repository   = Mockito.mock(IUserRepository.class);
        encoder      = new BCryptPasswordEncoder(4);
        originalHash = encoder.encode(ORIGINAL);
        stored       =
            new User()
                .setPrimaryId(USER_ID)
                .setUserName("alice@example.com")
                .setPassword(originalHash);

        Mockito.when(repository.save(Mockito.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(repository.findById(USER_ID)).thenReturn(Optional.of(stored));
        Mockito.when(repository.findAllByIdIn(Mockito.anySet())).thenReturn(Map.of(USER_ID,Optional.of(stored)));

        subject =
            new UserCommandService(
                repository,
                Mockito.mock(IUserEventSender.class),
                new ModelMapper().registerModule(new MappingModule()),
                encoder,
                LoginLockoutPolicy.defaults(),
                new GraniteApplicationRolesManagerSupplier().get(),
                new PasswordPolicy(8,128));
    }

    @Test
    public void
    createWithShortPasswordIsRefused()
    {
        CreateUserReply reply =
            subject
                .createUser(
                    new CreateUserRequest()
                        .setUserToCreate(new CreateUserData().setUserName("bob@example.com").setPassword(SHORT)))
                .toCompletableFuture()
                .join();

        assertFalse(reply.isSuccess());
        assertEquals(MESSAGE,reply.getFailureMessage());
        Mockito.verify(repository,Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    public void
    createWithAcceptablePasswordSucceeds()
    {
        CreateUserReply reply =
            subject
                .createUser(
                    new CreateUserRequest()
                        .setUserToCreate(new CreateUserData().setUserName("bob@example.com").setPassword("long-enough")))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
    }

    @Test
    public void
    batchCreateWithShortPasswordIsRefused()
    {
        CreateUsersReply reply =
            subject
                .createUsers(
                    new CreateUsersRequest()
                        .setUsersToCreate(
                            List.of(
                                new CreateUserData().setUserName("bob@example.com").setPassword("long-enough"),
                                new CreateUserData().setUserName("carol@example.com").setPassword(SHORT))))
                .toCompletableFuture()
                .join();

        assertFalse(reply.isSuccess());
        assertEquals(MESSAGE,reply.getFailureMessage());
        Mockito.verify(repository,Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    public void
    updateWithShortPasswordIsRefused()
    {
        UpdateUserReply reply = update(new UpdateUserData().setUserId(USER_ID).setPassword(SHORT));

        assertFalse(reply.isSuccess());
        assertEquals(MESSAGE,reply.getFailureMessage());
        assertEquals(originalHash,stored.getPassword());
    }

    @Test
    public void
    updateWithMaskedOrNullPasswordLeavesHashUnchanged()
    {
        assertTrue(update(new UpdateUserData().setUserId(USER_ID).setPassword("********")).isSuccess());
        assertEquals(originalHash,stored.getPassword());

        assertTrue(update(new UpdateUserData().setUserId(USER_ID).setPassword(null)).isSuccess());
        assertEquals(originalHash,stored.getPassword());

        assertTrue(update(new UpdateUserData().setUserId(USER_ID).setPassword("  ")).isSuccess());
        assertEquals(originalHash,stored.getPassword());
    }

    @Test
    public void
    updateWithAcceptablePasswordStoresNewHash()
    {
        assertTrue(update(new UpdateUserData().setUserId(USER_ID).setPassword("brand-new-password")).isSuccess());
        assertTrue(encoder.matches("brand-new-password",stored.getPassword()));
    }

    @Test
    public void
    batchUpdateWithShortPasswordIsRefusedWithoutChanges()
    {
        UpdateUsersReply reply =
            subject
                .updateUsers(
                    new UpdateUsersRequest()
                        .setUsersToUpdate(List.of(new UpdateUserData().setUserId(USER_ID).setPassword(SHORT))))
                .toCompletableFuture()
                .join();

        assertFalse(reply.isSuccess());
        assertEquals(MESSAGE,reply.getFailureMessage());
        assertEquals(originalHash,stored.getPassword());
    }

    @Test
    public void
    batchUpdateWithMaskedPasswordLeavesHashUnchanged()
    {
        UpdateUsersReply reply =
            subject
                .updateUsers(
                    new UpdateUsersRequest()
                        .setUsersToUpdate(List.of(new UpdateUserData().setUserId(USER_ID).setPassword("********"))))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(originalHash,stored.getPassword());
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
}

//////////////////////////////////////////////////////////////////////////////
