//////////////////////////////////////////////////////////////////////////////
// SessionQueryServiceClientTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.client.requestreply;

import granite.session.service.requestreply.ISessionCommandService;
import granite.session.service.requestreply.ISessionQueryService;
import granite.session.service.requestreply.SessionQueryServiceTest;
import jakarta.ws.rs.client.ClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationStage")
public
class SessionQueryServiceClientTest
    extends SessionQueryServiceTest
{
    private SessionQueryServiceClient   subject;
    private SessionCommandServiceClient commandSubject;

    @BeforeEach
    public void
    setUp() throws Exception
    {
        subject = new SessionQueryServiceClient(ClientBuilder.newBuilder(),"https://localhost:8089");
        commandSubject = new SessionCommandServiceClient(ClientBuilder.newBuilder(),"https://localhost:8088");
    }

    @AfterEach
    public void
    tearDown()
    {
        subject.close();
        commandSubject.close();
        subject = null;
        commandSubject = null;
    }

    @Override
    @Test
    public void
    testFindSessionsByKey()
    {
        super.testFindSessionsByKey();
    }

    @Override
    @Test
    public void
    testFindSessionsByKeySet()
    {
        super.testFindSessionsByKeySet();
    }

    @Override
    @Test
    public void
    testFindSessionByUserIdAndDeviceId()
    {
        super.testFindSessionByUserIdAndDeviceId();
    }

    @Override
    protected ISessionQueryService
    getSubject()
    {
        return subject;
    }

    @Override
    protected ISessionCommandService
    getCommandSubject()
    {
        return commandSubject;
    }

}

//////////////////////////////////////////////////////////////////////////////
