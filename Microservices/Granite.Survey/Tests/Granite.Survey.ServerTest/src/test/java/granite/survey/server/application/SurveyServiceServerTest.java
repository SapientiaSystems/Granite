//////////////////////////////////////////////////////////////////////////////
// SurveyServiceServerTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.server.application;

import granite.survey.service.requestreply.SurveyServiceTest;
import granite.survey.service.requestreply.ISurveyCommandService;
import granite.survey.service.requestreply.ISurveyQueryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import strata.server.spring.inject.RequestAttributeMap;

@Tag("CommitStage")
public
class SurveyServiceServerTest
    extends SurveyServiceTest
{
    private static ApplicationContext context;

    @BeforeAll
    public static void
    initialize()
    {
        context = new AnnotationConfigApplicationContext(TestConfiguration.class);
    }

    @BeforeEach
    public void
    setUp()
    {
        RequestContextHolder.setRequestAttributes(new RequestAttributeMap());
    }

    @AfterEach
    public void
    tearDown()
    {
        RequestContextHolder.resetRequestAttributes();
    }

    @Override
    protected ISurveyCommandService
    getCommandSubject()
    {
        return context.getBean(ISurveyCommandService.class);
    }

    @Override
    protected ISurveyQueryService
    getQuerySubject()
    {
        return context.getBean(ISurveyQueryService.class);
    }
}

//////////////////////////////////////////////////////////////////////////////
