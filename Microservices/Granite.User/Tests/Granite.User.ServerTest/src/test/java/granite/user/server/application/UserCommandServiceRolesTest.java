//////////////////////////////////////////////////////////////////////////////
// UserCommandServiceRolesTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.foundation.core.security.ApplicationRoles;
import granite.user.server.configuration.PoplarUserCommandServerConfiguration;
import granite.user.server.domain.IUserRepository;
import granite.user.server.domain.User;
import granite.user.service.event.IUserEventSender;
import granite.user.service.requestreply.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Tag("CommitStage")
public
class UserCommandServiceRolesTest
{
    private static final Long   USER_ID  = 7L;
    private static final String REVIEWER = PoplarUserCommandServerConfiguration.REVIEWER;
    private static final String APPROVER = PoplarUserCommandServerConfiguration.APPROVER;

    private IUserRepository    repository;
    private User               stored;
    private UserCommandService commandService;

    @BeforeEach
    public void
    setUp()
    {
        repository = Mockito.mock(IUserRepository.class);
        stored     = new User().setPrimaryId(USER_ID).setUserName("alice@example.com");

        Mockito
            .when(repository.save(Mockito.any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito
            .when(repository.findById(USER_ID))
            .thenReturn(Optional.of(stored));
        Mockito
            .when(repository.findAllByIdIn(Mockito.anySet()))
            .thenReturn(Map.of(USER_ID,Optional.of(stored)));

        commandService =
            new UserCommandService(
                repository,
                Mockito.mock(IUserEventSender.class),
                new ModelMapper().registerModule(new MappingModule()),
                new BCryptPasswordEncoder(4),
                LoginLockoutPolicy.defaults(),
                PoplarUserCommandServerConfiguration.poplarRoles());
    }

    @Test
    public void
    createStoresExpandedSet()
    {
        CreateUserReply reply = create(Set.of(APPROVER));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(Set.of(APPROVER,REVIEWER,"ROLE_User"),reply.getCreatedUser().getAuthorities());
    }

    @Test
    public void
    createRejectsNonCatalogRole()
    {
        CreateUserReply reply = create(Set.of("ROLE_Bogus"));

        assertFalse(reply.isSuccess());
        assertEquals("unknown role: ROLE_Bogus",reply.getFailureMessage());
        Mockito.verify(repository,Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    public void
    createUsersRejectsNonCatalogRoleWithoutSaving()
    {
        CreateUsersReply reply =
            commandService
                .createUsers(
                    new CreateUsersRequest()
                        .setUsersToCreate(
                            List.of(
                                new CreateUserData()
                                    .setUserName("carol@example.com")
                                    .setPassword("pw")
                                    .setAuthorities(Set.of(REVIEWER)),
                                new CreateUserData()
                                    .setUserName("dave@example.com")
                                    .setPassword("pw")
                                    .setAuthorities(Set.of("ROLE_Bogus")))))
                .toCompletableFuture()
                .join();

        assertFalse(reply.isSuccess());
        assertEquals("unknown role: ROLE_Bogus",reply.getFailureMessage());
        Mockito.verify(repository,Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    public void
    updateExpandsAddedRole()
    {
        stored.setAuthorities(new LinkedHashSet<>(Set.of(REVIEWER)));

        UpdateUserReply reply = update(Set.of(REVIEWER,APPROVER));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(Set.of(APPROVER,REVIEWER,"ROLE_User"),stored.getAuthorities());
    }

    @Test
    public void
    updateKeepsExistingUnknownAuthorities()
    {
        stored.setAuthorities(new LinkedHashSet<>(Set.of("ROLE_Legacy")));

        UpdateUserReply reply = update(Set.of("ROLE_Legacy",REVIEWER));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(Set.of("ROLE_Legacy",REVIEWER,"ROLE_User"),stored.getAuthorities());
    }

    @Test
    public void
    updateRejectsAddedUnknown()
    {
        stored.setAuthorities(new LinkedHashSet<>());

        UpdateUserReply reply = update(Set.of("SCOPE_internal"));

        assertFalse(reply.isSuccess());
        assertEquals("unknown role: SCOPE_internal",reply.getFailureMessage());
        assertTrue(stored.getAuthorities().isEmpty());
        Mockito.verify(repository,Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    public void
    updateUsersRejectsAddedUnknownWithoutSaving()
    {
        stored.setAuthorities(new LinkedHashSet<>());

        UpdateUsersReply reply =
            commandService
                .updateUsers(
                    new UpdateUsersRequest()
                        .setUsersToUpdate(
                            List.of(new UpdateUserData().setUserId(USER_ID).setAuthorities(Set.of("ROLE_Bogus")))))
                .toCompletableFuture()
                .join();

        assertFalse(reply.isSuccess());
        assertEquals("unknown role: ROLE_Bogus",reply.getFailureMessage());
        assertTrue(stored.getAuthorities().isEmpty());
        Mockito.verify(repository,Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    public void
    configurationExposesOverridableCatalog()
    {
        try (AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(PoplarUserCommandServerConfiguration.class))
        {
            ApplicationRoles roles = context.getBean(ApplicationRoles.class);

            assertTrue(roles.isRole(APPROVER));
            assertTrue(roles.isRole("ROLE_Admin"));
        }
    }

    private CreateUserReply
    create(Set<String> authorities)
    {
        return
            commandService
                .createUser(
                    new CreateUserRequest()
                        .setUserToCreate(
                            new CreateUserData()
                                .setUserName("bob@example.com")
                                .setPassword("pw")
                                .setAuthorities(authorities)))
                .toCompletableFuture()
                .join();
    }

    private UpdateUserReply
    update(Set<String> authorities)
    {
        return
            commandService
                .updateUser(
                    new UpdateUserRequest()
                        .setUserToUpdate(new UpdateUserData().setUserId(USER_ID).setAuthorities(authorities)))
                .toCompletableFuture()
                .join();
    }
}

//////////////////////////////////////////////////////////////////////////////
