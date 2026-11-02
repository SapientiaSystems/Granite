//////////////////////////////////////////////////////////////////////////////
// FeedbackServiceClientTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.client.requestreply;

import granite.feedback.service.requestreply.FeedbackServiceTest;
import granite.feedback.service.requestreply.IFeedbackCommandService;
import granite.feedback.service.requestreply.IFeedbackQueryService;
import jakarta.ws.rs.client.ClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

@Tag("IntegrationStage")
public
class FeedbackServiceClientTest
    extends FeedbackServiceTest
{
    private IFeedbackCommandServiceClient commandSubject;
    private IFeedbackQueryServiceClient   querySubject;

    @BeforeEach
    public void
    setUp()
    {
        commandSubject = new FeedbackCommandServiceClient(ClientBuilder.newBuilder(),"https://localhost:8086");
        querySubject   = new FeedbackQueryServiceClient(ClientBuilder.newBuilder(),"https://localhost:8087");
    }

    @AfterEach
    public void
    tearDown()
    {
        commandSubject.close();
        querySubject.close();
    }

    @Override
    protected IFeedbackCommandService
    getCommandSubject() { return commandSubject; }

    @Override
    protected IFeedbackQueryService
    getQuerySubject() { return querySubject; }
}

//////////////////////////////////////////////////////////////////////////////
