//////////////////////////////////////////////////////////////////////////////
// ContentServiceClientTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.client.requestreply;

import granite.content.service.requestreply.ContentServiceTest;
import granite.content.service.requestreply.IContentCommandService;
import granite.content.service.requestreply.IContentQueryService;
import jakarta.ws.rs.client.ClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

@Tag("IntegrationStage")
public
class ContentServiceClientTest
    extends ContentServiceTest
{
    private IContentCommandServiceClient commandSubject;
    private IContentQueryServiceClient   querySubject;

    @BeforeEach
    public void
    setUp()
    {
        commandSubject = new ContentCommandServiceClient(ClientBuilder.newBuilder(),"https://localhost:8093");
        querySubject   = new ContentQueryServiceClient(ClientBuilder.newBuilder(),"https://localhost:8094");
    }

    @AfterEach
    public void
    tearDown()
    {
        commandSubject.close();
        querySubject.close();
    }

    @Override
    protected IContentCommandService
    getCommandSubject() { return commandSubject; }

    @Override
    protected IContentQueryService
    getQuerySubject() { return querySubject; }
}

//////////////////////////////////////////////////////////////////////////////
