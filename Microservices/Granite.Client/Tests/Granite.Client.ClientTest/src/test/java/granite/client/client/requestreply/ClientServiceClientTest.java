//////////////////////////////////////////////////////////////////////////////
// ClientServiceClientTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.client.requestreply;

import granite.client.service.requestreply.ClientServiceTest;
import granite.client.service.requestreply.IClientCommandService;
import granite.client.service.requestreply.IClientQueryService;
import jakarta.ws.rs.client.ClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

@Tag("IntegrationStage")
public
class ClientServiceClientTest
    extends ClientServiceTest
{
    private IClientCommandServiceClient commandSubject;
    private IClientQueryServiceClient   querySubject;

    @BeforeEach
    public void
    setUp()
    {
        commandSubject = new ClientCommandServiceClient(ClientBuilder.newBuilder(),"https://localhost:8097");
        querySubject   = new ClientQueryServiceClient(ClientBuilder.newBuilder(),"https://localhost:8098");
    }

    @AfterEach
    public void
    tearDown()
    {
        commandSubject.close();
        querySubject.close();
    }

    protected IClientCommandService
    getCommandSubject() { return commandSubject; }

    protected IClientQueryService
    getQuerySubject() { return querySubject; }
}

//////////////////////////////////////////////////////////////////////////////
