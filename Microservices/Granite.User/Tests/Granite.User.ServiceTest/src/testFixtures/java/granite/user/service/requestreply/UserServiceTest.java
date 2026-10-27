//////////////////////////////////////////////////////////////////////////////
// UserServiceClientTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Tag("IntegrationStage")
public abstract
class UserServiceTest
{
    private IUserCommandService subject;

    @Test
    public void
    testCreateUser()
    {
        String      userName    = uniqueUserName();
        Set<String> authorities = Set.of("ROLE_USER");
        CreateUserReply reply =
            getSubject()
                .createUser(
                    new CreateUserRequest()
                        .setUserToCreate(
                            new CreateUserData()
                                .setUserName(userName)
                                .setAuthorities(authorities)))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(reply.isSuccess(),reply.getFailureMessage());
        Assertions.assertNotNull(reply.getCreatedUser());
        Assertions.assertNotNull(reply.getCreatedUser().getUserId());
        Assertions.assertEquals(userName,reply.getCreatedUser().getUserName());
        Assertions.assertEquals(authorities,reply.getCreatedUser().getAuthorities());
    }

    @Test
    public void
    testUpdateUser()
    {
        String      userName           = uniqueUserName();
        Set<String> initialAuthorities = Set.of("ROLE_USER");
        Set<String> updatedAuthorities = Set.of("ROLE_USER","ROLE_ADMIN");
        CreateUserReply createReply =
            getSubject()
                .createUser(
                    new CreateUserRequest()
                        .setUserToCreate(
                            new CreateUserData()
                                .setUserName(userName)
                                .setAuthorities(initialAuthorities)))
                .toCompletableFuture()
                .join();

        UserData entity = null;
        Long id = null;

        Assertions.assertTrue(createReply.isSuccess(),createReply.getFailureMessage());
        entity = createReply.getCreatedUser();
        id = entity.getUserId();
        Assertions.assertNotNull(entity);
        Assertions.assertNotNull(id);
        Assertions.assertEquals(initialAuthorities,entity.getAuthorities());

        UpdateUserReply updateReply =
            getSubject()
                .updateUser(
                    new UpdateUserRequest()
                        .setUserToUpdate(
                            new UpdateUserData()
                                .setUserId(id)
                                .setAuthorities(updatedAuthorities)))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(updateReply.isSuccess(),updateReply.getFailureMessage());
        entity = updateReply.getUpdatedUser();
        Assertions.assertNotNull(entity);
        Assertions.assertEquals(id,entity.getUserId());
        Assertions.assertEquals(updatedAuthorities,entity.getAuthorities());
    }

    @Test
    public void
    testUpdateUsers()
    {
        CreateUserReply createReply1 =
            getSubject()
                .createUser(
                    new CreateUserRequest()
                        .setUserToCreate(
                            new CreateUserData()
                                .setUserName(uniqueUserName())
                                .setAuthorities(Set.of("ROLE_USER"))))
                .toCompletableFuture()
                .join();

        CreateUserReply createReply2 =
            getSubject()
                .createUser(
                    new CreateUserRequest()
                        .setUserToCreate(
                            new CreateUserData()
                                .setUserName(uniqueUserName())
                                .setAuthorities(Set.of("ROLE_USER"))))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(createReply1.isSuccess(),createReply1.getFailureMessage());
        Assertions.assertTrue(createReply2.isSuccess(),createReply2.getFailureMessage());

        Long id1 = createReply1.getCreatedUser().getUserId();
        Long id2 = createReply2.getCreatedUser().getUserId();

        UpdateUsersReply updateReply =
            getSubject()
                .updateUsers(
                    new UpdateUsersRequest()
                        .setUsersToUpdate(
                            List.of(
                                new UpdateUserData()
                                    .setUserId(id1)
                                    .setAuthorities(Set.of("ROLE_USER","ROLE_ADMIN")),
                                new UpdateUserData()
                                    .setUserId(id2)
                                    .setAuthorities(Set.of("ROLE_USER","ROLE_AUDITOR")))))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(updateReply.isSuccess(),updateReply.getFailureMessage());
        Assertions.assertNotNull(updateReply.getUpdatedUsers());
        Assertions.assertEquals(2,updateReply.getUpdatedUsers().size());
        Assertions.assertTrue(updateReply.getFailedUsers() == null || updateReply.getFailedUsers().isEmpty());
    }

    @Test
    public void
    testDestroyUser()
    {
        CreateUserReply createReply =
            getSubject()
                .createUser(
                    new CreateUserRequest()
                        .setUserToCreate(
                            new CreateUserData()
                                .setUserName(uniqueUserName())
                                .setAuthorities(Set.of("ROLE_USER"))))
                .toCompletableFuture()
                .join();

        UserData entity = null;
        Long id = null;

        Assertions.assertTrue(createReply.isSuccess(),createReply.getFailureMessage());
        entity = createReply.getCreatedUser();
        id = entity.getUserId();
        Assertions.assertNotNull(entity);
        Assertions.assertNotNull(id);

        DestroyUserReply destroyReply =
            getSubject()
                .destroyUser(
                    new DestroyUserRequest()
                        .setUserId(id))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(destroyReply.isSuccess(),destroyReply.getFailureMessage());
        entity = destroyReply.getDestroyedUser();
        Assertions.assertNotNull(entity);
        Assertions.assertEquals(id,entity.getUserId());
    }

    /*
    @Test
    public void
    testFindUser()
    {
        CreateUserReply createReply =
            getSubject()
                .createUser(
                    new CreateUserRequest()
                        .setUser(
                            new CreateUserData()
                                .setFoo("XXX-YYYYY-ZZZZZZZ")))
                .toCompletableFuture()
                .join();

        UserData entity = null;
        Long id = null;

        Assertions.assertTrue(createReply.isSuccess(),createReply.getFailureMessage());
        entity = createReply.getCreatedUser();
        id = entity.getUserId();
        Assertions.assertNotNull(entity);
        Assertions.assertNotNull(id);
        Assertions.assertEquals("XXX-YYYYY-ZZZZZZZ",entity.getFoo());

        FindUserReply findReply =
            getSubject()
                .findUser(
                    new FindUserRequest()
                        .setUserId(id))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(findReply.isSuccess(),findReply.getFailureMessage());
        entity = findReply.getFoundUser();
        Assertions.assertNotNull(entity);
        Assertions.assertEquals(id,entity.getUserId());
        Assertions.assertEquals("XXX-YYYYY-ZZZZZZZ",entity.getFoo());
    }
    */
    protected abstract IUserCommandService
    getSubject();

    private static String
    uniqueUserName() { return "user-" + UUID.randomUUID() + "@example.com"; }
}

//////////////////////////////////////////////////////////////////////////////
