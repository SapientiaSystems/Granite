//////////////////////////////////////////////////////////////////////////////
// AgentServiceClientTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.client.requestreply;

import granite.agent.service.requestreply.AgentServiceTest;
import granite.agent.service.requestreply.IAgentCommandService;
import granite.agent.service.requestreply.IAgentQueryService;
import jakarta.ws.rs.client.ClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

@Tag("IntegrationStage")
public
class AgentServiceClientTest
    extends AgentServiceTest
{
    private IAgentCommandServiceClient commandSubject;
    private IAgentQueryServiceClient   querySubject;

    @BeforeEach
    public void
    setUp()
    {
        commandSubject = new AgentCommandServiceClient(ClientBuilder.newBuilder(),"https://localhost:8095");
        querySubject   = new AgentQueryServiceClient(ClientBuilder.newBuilder(),"https://localhost:8096");
    }

    @AfterEach
    public void
    tearDown()
    {
        commandSubject.close();
        querySubject.close();
    }

    @Override
    protected IAgentCommandService
    getCommandSubject() { return commandSubject; }

    @Override
    protected IAgentQueryService
    getQuerySubject() { return querySubject; }
}

//////////////////////////////////////////////////////////////////////////////
