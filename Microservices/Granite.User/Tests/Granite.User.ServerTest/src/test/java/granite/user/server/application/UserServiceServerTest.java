//////////////////////////////////////////////////////////////////////////////
// UserServiceClientTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.service.requestreply.IUserCommandService;
import granite.user.service.requestreply.UserServiceTest;
import org.junit.jupiter.api.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import strata.server.spring.inject.RequestAttributeMap;

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

    @Override
    protected IUserCommandService
    getSubject()
    {
        return subject;
    }

}

//////////////////////////////////////////////////////////////////////////////
