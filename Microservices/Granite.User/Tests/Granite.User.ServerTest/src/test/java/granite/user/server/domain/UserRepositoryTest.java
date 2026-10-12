//////////////////////////////////////////////////////////////////////////////
// EntityManagerProviderTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.domain;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import strata.server.core.unitofwork.IUnitOfWorkSynchronizationManager;
import strata.server.spring.inject.RequestAttributeMap;
import strata.server.spring.unitofwork.ISpringUnitOfWorkManager;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@Tag("CommitStage")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public
class UserRepositoryTest
{
    private RequestAttributeMap requestAttributes;

    @Autowired
    private IUserRepository target;

    @Autowired
    private ISpringUnitOfWorkManager unitOfWorkManager;

    @Autowired
    private IUnitOfWorkSynchronizationManager synchronizer;

    private TransactionTemplate transaction;

    @BeforeAll
    public static void
    init() { System.setProperty("log4j.configuration", "log4j.properties"); }

    @BeforeEach
    public void
    setUp() throws Exception
    {
        RequestContextHolder.setRequestAttributes(new RequestAttributeMap());
        transaction = new TransactionTemplate(unitOfWorkManager);
    }

    @AfterEach
    public void
    tearDown()
    {
        target = null;
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void
    testSave() throws Exception
    {
        final AtomicReference<User> expected = new AtomicReference<>();
        final AtomicReference<User> actual = new AtomicReference<>();

        transaction.executeWithoutResult(
            status ->
            {
                expected.set(
                    new User()
                        .setUserName("user-save-1@example.com"));
                actual.set(target.save(expected.get()));
                synchronizer
                    .executeAfterCommit(() -> System.out.println("After Commit 1"))
                    .executeAfterCommit(() -> System.out.println("After Commit 2"))
                    .executeAfterCommit(() -> System.out.println("After Commit 3"))
                    .executeAfterRollback(() -> System.out.println("After Rollback 1"));
            });
        assertNotNull(actual.get());
        assertNotNull(actual.get().getPrimaryId());
        assertEquals(expected.get().getUserName(),actual.get().getUserName());
    }

    @Test
    public void
    testDelete() throws Exception
    {
        final AtomicReference<User> expected = new AtomicReference<>();
        final AtomicReference<User> actual = new AtomicReference<>();

        transaction.executeWithoutResult(
            status ->
            {
                expected.set(
                    new User()
                        .setUserName("user-delete-5@example.com"));
                actual.set(target.save(expected.get()));
            });
        assertNotNull(actual.get());
        assertNotNull(actual.get().getPrimaryId());
        assertEquals(expected.get().getUserName(),actual.get().getUserName());

        transaction.executeWithoutResult(
            status ->
            {
                target.delete(actual.get());
            });

        assertFalse(
            (boolean)
                transaction
                    .execute(
                        status ->
                            target.existsById(actual.get().getPrimaryId())));

    }
}

//////////////////////////////////////////////////////////////////////////////
