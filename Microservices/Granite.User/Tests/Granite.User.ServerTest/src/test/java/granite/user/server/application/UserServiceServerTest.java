//////////////////////////////////////////////////////////////////////////////
// UserServiceClientTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.service.requestreply.CreateUserData;
import granite.user.service.requestreply.CreateUserReply;
import granite.user.service.requestreply.CreateUserRequest;
import granite.user.service.requestreply.CreateUsersReply;
import granite.user.service.requestreply.CreateUsersRequest;
import granite.user.service.requestreply.IUserCommandService;
import granite.user.service.requestreply.UserData;
import granite.user.service.requestreply.UserServiceTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import strata.server.spring.inject.RequestAttributeMap;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class UserServiceServerTest
    extends UserServiceTest
{
    private static ApplicationContext context;
    private IUserCommandService subject;

    @BeforeAll
    public static void
    initialize()
    {
        context =
            new AnnotationConfigApplicationContext(
                TestConfiguration.class);
    }

    @BeforeEach
    public void
    setUp() throws Exception
    {
        RequestContextHolder.setRequestAttributes(new RequestAttributeMap());
        subject = context.getBean(IUserCommandService.class);
    }

    @AfterEach
    public void
    tearDown()
    {
        subject = null;
        RequestContextHolder.resetRequestAttributes();
    }

    @Override
    @Test
    public void
    testCreateUser()
    {
        super.testCreateUser();
    }

    @Override
    @Test
    public void
    testUpdateUser()
    {
        super.testUpdateUser();
    }

    @Override
    @Test
    public void
    testDestroyUser()
    {
        super.testDestroyUser();
    }

    /*
    @Override
    @Test
    public void
    testFindUser()
    {
        super.testFindUser();
    }
     */

    @Test
    public void
    testCreateUserWritesCreatedOutboxEvent()
    {
        CreateUserReply reply =
            subject
                .createUser(
                    new CreateUserRequest()
                        .setUserToCreate(newUserData()))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertCreatedOutboxEvent(reply.getCreatedUser().getUserId());
    }

    @Test
    public void
    testCreateUsersWritesCreatedOutboxEvents()
    {
        CreateUsersReply reply =
            subject
                .createUsers(
                    new CreateUsersRequest()
                        .setUsersToCreate(List.of(newUserData(),newUserData())))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(2,reply.getCreatedUsers().size());

        for (UserData created : reply.getCreatedUsers())
            assertCreatedOutboxEvent(created.getUserId());
    }

    @Override
    protected IUserCommandService
    getSubject()
    {
        return subject;
    }

    private CreateUserData
    newUserData()
    {
        return
            new CreateUserData()
                .setUserName("user-" + UUID.randomUUID() + "@example.com")
                .setAuthorities(Set.of("ROLE_USER"));
    }

    private void
    assertCreatedOutboxEvent(Long userId)
    {
        EntityManager manager =
            context
                .getBean(EntityManagerFactory.class)
                .createEntityManager();

        try
        {
            List<String> payloads =
                manager
                    .createQuery(
                        "select e.eventPayload from OutboxEvent e where e.sourceId = :id",
                        String.class)
                    .setParameter("id",userId.toString())
                    .getResultList();

            assertTrue(
                payloads.stream().anyMatch(payload -> payload.contains("CREATED")),
                "no CREATED outbox event for user " + userId + ": " + payloads);
        }
        finally
        {
            manager.close();
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
