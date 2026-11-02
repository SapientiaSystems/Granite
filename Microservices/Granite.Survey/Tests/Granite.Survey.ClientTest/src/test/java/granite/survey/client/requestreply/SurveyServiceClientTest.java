//////////////////////////////////////////////////////////////////////////////
// SurveyServiceClientTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.client.requestreply;

import granite.survey.service.requestreply.SurveyServiceTest;
import granite.survey.service.requestreply.ISurveyCommandService;
import granite.survey.service.requestreply.ISurveyQueryService;
import jakarta.ws.rs.client.ClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

@Tag("IntegrationStage")
public
class SurveyServiceClientTest
    extends SurveyServiceTest
{
    private ISurveyCommandServiceClient commandSubject;
    private ISurveyQueryServiceClient   querySubject;

    @BeforeEach
    public void
    setUp()
    {
        commandSubject = new SurveyCommandServiceClient(ClientBuilder.newBuilder(),"https://localhost:8091");
        querySubject   = new SurveyQueryServiceClient(ClientBuilder.newBuilder(),"https://localhost:8092");
    }

    @AfterEach
    public void
    tearDown()
    {
        commandSubject.close();
        querySubject.close();
    }

    @Override
    protected ISurveyCommandService
    getCommandSubject() { return commandSubject; }

    @Override
    protected ISurveyQueryService
    getQuerySubject() { return querySubject; }
}

//////////////////////////////////////////////////////////////////////////////
