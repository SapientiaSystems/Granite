//////////////////////////////////////////////////////////////////////////////
// UserServiceClientTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

@Tag("IntegrationStage")
public abstract
class UserServiceTest
{
    private IUserCommandService subject;

    @Test
    public void
    testCreateUser()
    {
        CreateUserReply reply =
            getSubject()
                .createUser(
                    new CreateUserRequest()
                        .setUserToCreate(
                            new CreateUserData()
                                .setFoo("XXX-YYYYY-ZZZZZZZ")))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(reply.isSuccess(),reply.getFailureMessage());
        Assertions.assertNotNull(reply.getCreatedUser());
        Assertions.assertNotNull(reply.getCreatedUser().getUserId());
        Assertions.assertEquals("XXX-YYYYY-ZZZZZZZ",reply.getCreatedUser().getFoo());
    }

    @Test
    public void
    testUpdateUser()
    {
        CreateUserReply createReply =
            getSubject()
                .createUser(
                    new CreateUserRequest()
                        .setUserToCreate(
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

        UpdateUserReply updateReply =
            getSubject()
                .updateUser(
                    new UpdateUserRequest()
                        .setUserToUpdate(
                            new UpdateUserData()
                                .setUserId(id)
                                .setFoo("AAA-BBBBB-CCCCCCC")))
                .toCompletableFuture()
                .join();

        Assertions.assertTrue(updateReply.isSuccess(),updateReply.getFailureMessage());
        entity = updateReply.getUpdatedUser();
        Assertions.assertNotNull(entity);
        Assertions.assertEquals(id,entity.getUserId());
        Assertions.assertEquals("AAA-BBBBB-CCCCCCC",entity.getFoo());
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
                                .setFoo("XXX-YYYYY-ZZZZZZZ")))
                .toCompletableFuture()
                .join();

        CreateUserReply createReply2 =
            getSubject()
                .createUser(
                    new CreateUserRequest()
                        .setUserToCreate(
                            new CreateUserData()
                                .setFoo("AAA-BBBBB-CCCCCCC")))
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
                                    .setFoo("UPDATED-ONE"),
                                new UpdateUserData()
                                    .setUserId(id2)
                                    .setFoo("UPDATED-TWO"))))
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
}

//////////////////////////////////////////////////////////////////////////////
