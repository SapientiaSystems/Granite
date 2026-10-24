//////////////////////////////////////////////////////////////////////////////
// SessionCommandServiceClientTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.client.requestreply;

import granite.session.service.requestreply.ISessionCommandService;
import granite.session.service.requestreply.SessionCommandServiceTest;
import jakarta.ws.rs.client.ClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationStage")
public
class SessionCommandServiceClientTest
    extends SessionCommandServiceTest
{
    private SessionCommandServiceClient subject;

    @BeforeEach
    public void
    setUp() throws Exception
    {
        subject = new SessionCommandServiceClient(ClientBuilder.newBuilder(),"https://localhost:8088");
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
    testLogin()
    {
        super.testLogin();
    }

    @Override
    @Test
    public void
    testLoginWithOauth2()
    {
        super.testLoginWithOauth2();
    }

    @Override
    @Test
    public void
    testLogout()
    {
        super.testLogout();
    }

    @Override
    @Test
    public void
    testLogoutAllForUser()
    {
        super.testLogoutAllForUser();
    }

    @Override
    @Test
    public void
    testRefresh()
    {
        super.testRefresh();
    }

    @Override
    @Test
    public void
    testResolveSession()
    {
        super.testResolveSession();
    }

    @Override
    @Test
    public void
    testResolveSessionWithUnknownHandleFails()
    {
        super.testResolveSessionWithUnknownHandleFails();
    }

    @Override
    protected ISessionCommandService
    getSubject()
    {
        return subject;
    }

}

//////////////////////////////////////////////////////////////////////////////
