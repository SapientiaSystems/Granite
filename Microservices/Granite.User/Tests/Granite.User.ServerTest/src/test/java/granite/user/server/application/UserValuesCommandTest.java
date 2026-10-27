//////////////////////////////////////////////////////////////////////////////
// UserValuesCommandTest.java
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
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("CommitStage")
public
class UserValuesCommandTest
{
    private static final Long USER_ID = 42L;

    private UserCommandService commandService;
    private User               stored;

    @BeforeEach
    public void
    setUp()
    {
        IUserRepository repository = Mockito.mock(IUserRepository.class);

        stored =
            new User()
                .setPrimaryId(USER_ID)
                .setUserName("alice@example.com")
                .setValue("a","1")
                .setValue("b","2");

        Mockito
            .when(repository.save(Mockito.any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito
            .when(repository.findById(USER_ID))
            .thenReturn(Optional.of(stored));

        commandService =
            new UserCommandService(
                repository,
                Mockito.mock(IUserEventSender.class),
                new ModelMapper().registerModule(new MappingModule()),
                new BCryptPasswordEncoder(4),
                LoginLockoutPolicy.defaults(),
                GraniteApplicationRoles.create());
    }

    @Test
    public void
    createStoresValues()
    {
        Map<String,Object> values = new LinkedHashMap<>();

        values.put("theme","dark");
        values.put("pageSize","50");

        CreateUserReply reply =
            commandService
                .createUser(
                    new CreateUserRequest()
                        .setUserToCreate(
                            new CreateUserData()
                                .setUserName("bob@example.com")
                                .setPassword("pw")
                                .setValues(values)))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(values,reply.getCreatedUser().getValues());
    }

    @Test
    public void
    updateWithValuesReplacesWholeMap()
    {
        UpdateUserReply reply =
            update(new UpdateUserData().setUserId(USER_ID).setValues(Map.of("b","3","c","4")));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(Map.of("b","3","c","4"),reply.getUpdatedUser().getValues());
        assertEquals(Map.of("b","3","c","4"),stored.getValues());
    }

    @Test
    public void
    updateWithoutValuesLeavesMapUnchanged()
    {
        UpdateUserReply reply =
            update(new UpdateUserData().setUserId(USER_ID).setUserName("alice2@example.com"));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(Map.of("a","1","b","2"),reply.getUpdatedUser().getValues());
    }

    @Test
    public void
    updateWithEmptyValuesClearsMap()
    {
        UpdateUserReply reply =
            update(new UpdateUserData().setUserId(USER_ID).setValues(new HashMap<>()));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertTrue(reply.getUpdatedUser().getValues().isEmpty());
        assertTrue(stored.getValues().isEmpty());
    }

    @Test
    public void
    nullValueEntriesAreSkipped()
    {
        Map<String,Object> values = new LinkedHashMap<>();

        values.put("a",null);
        values.put("b","x");

        UpdateUserReply reply = update(new UpdateUserData().setUserId(USER_ID).setValues(values));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(Map.of("b","x"),reply.getUpdatedUser().getValues());
    }

    private UpdateUserReply
    update(UpdateUserData data)
    {
        return
            commandService
                .updateUser(new UpdateUserRequest().setUserToUpdate(data))
                .toCompletableFuture()
                .join();
    }
}

//////////////////////////////////////////////////////////////////////////////
