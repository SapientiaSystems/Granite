//////////////////////////////////////////////////////////////////////////////
// UserServiceClientTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.client.requestreply;

import granite.user.service.requestreply.IUserCommandService;
import granite.user.service.requestreply.UserServiceTest;
import jakarta.ws.rs.client.ClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationStage")
public
class UserServiceClientTest
    extends UserServiceTest
{
    private IUserCommandServiceClient subject;

    @BeforeEach
    public void
    setUp() throws Exception
    {
        subject = new UserCommandServiceClient(ClientBuilder.newBuilder(), "https://localhost:8081");
    }

    @AfterEach
    public void
    tearDown()
    {
        subject.close();
        subject = null;
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
