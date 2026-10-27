//////////////////////////////////////////////////////////////////////////////
// UserValuesCommandTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.user.server.domain.IUserRepository;
import granite.user.server.domain.User;
import granite.user.service.event.IUserEventSender;
import granite.user.service.event.UserEvent;
import granite.user.service.requestreply.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Tag("CommitStage")
public
class UserValuesCommandTest
{
    private static final Long USER_ID = 42L;

    private UserCommandService commandService;
    private User               stored;
    private IUserRepository    repository;
    private IUserEventSender   sender;

    @BeforeEach
    public void
    setUp()
    {
        repository = Mockito.mock(IUserRepository.class);
        sender     = Mockito.mock(IUserEventSender.class);

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
                sender,
                new ModelMapper().registerModule(new MappingModule()),
                new BCryptPasswordEncoder(4),
                LoginLockoutPolicy.defaults(),
                new GraniteApplicationRolesManagerSupplier().get());
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
    updatePreservesReservedKeys()
    {
        stored.setValue("preferences.granite.theme","dark").setValue("granite.rolesAssigned","true");

        Map<String,Object> incoming = new HashMap<>();

        incoming.put("preferences.granite.theme","light");
        incoming.put("granite.other","x");
        incoming.put("b","3");

        UpdateUserReply reply = update(new UpdateUserData().setUserId(USER_ID).setValues(incoming));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(
            Map.of("preferences.granite.theme","dark","granite.rolesAssigned","true","b","3"),
            stored.getValues());

        update(new UpdateUserData().setUserId(USER_ID).setValues(Map.of()));

        assertEquals(Map.of("preferences.granite.theme","dark","granite.rolesAssigned","true"),stored.getValues());
    }

    @Test
    public void
    updateStillReplacesOtherKeys()
    {
        stored.setValue("preferences.x","1");

        update(new UpdateUserData().setUserId(USER_ID).setValues(Map.of("c","4")));

        assertEquals(Map.of("preferences.x","1","c","4"),stored.getValues());
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

    @Test
    public void
    mergeAddsAndReplacesKeys()
    {
        Map<String,Object> values = new LinkedHashMap<>();

        values.put("b","3");
        values.put("c","4");

        MergeUserValuesReply reply = merge(USER_ID,values);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(Map.of("a","1","b","3","c","4"),reply.getMergedUser().getValues());
        assertEquals(Map.of("a","1","b","3","c","4"),stored.getValues());
    }

    @Test
    public void
    mergeFiresUserUpdated()
    {
        MergeUserValuesReply reply = merge(USER_ID,Map.of("c","4"));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        Mockito.verify(sender,Mockito.atLeastOnce()).send(Mockito.any(UserEvent.class));
    }

    @Test
    public void
    mergeRemovesNullKeys()
    {
        Map<String,Object> values = new LinkedHashMap<>();

        values.put("a",null);

        MergeUserValuesReply reply = merge(USER_ID,values);

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(Map.of("b","2"),reply.getMergedUser().getValues());
        assertEquals(Map.of("b","2"),stored.getValues());
    }

    @Test
    public void
    mergeKeepsOtherKeys()
    {
        MergeUserValuesReply reply = merge(USER_ID,Map.of("c","4"));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals("1",stored.getValues().get("a"));
        assertEquals("2",stored.getValues().get("b"));
        assertEquals("4",stored.getValues().get("c"));
    }

    @Test
    public void
    mergeLeavesAuthoritiesUnchanged()
    {
        stored.setAuthorities(new HashSet<>(Set.of("ROLE_ADMIN")));

        MergeUserValuesReply reply = merge(USER_ID,Map.of("c","4"));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(Set.of("ROLE_ADMIN"),stored.getAuthorities());
    }

    @Test
    public void
    mergeUnknownUserFails()
    {
        MergeUserValuesReply reply = merge(999L,Map.of("c","4"));

        assertFalse(reply.isSuccess());
        assertNotNull(reply.getFailureMessage());
        assertTrue(reply.getFailureMessage().contains("999"),reply.getFailureMessage());
        assertEquals(Map.of("a","1","b","2"),stored.getValues());
        Mockito.verify(repository,Mockito.never()).save(Mockito.any(User.class));
    }

    private MergeUserValuesReply
    merge(Long userId,Map<String,Object> values)
    {
        return
            commandService
                .mergeUserValues(new MergeUserValuesRequest().setUserId(userId).setValues(values))
                .toCompletableFuture()
                .join();
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
