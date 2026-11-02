//////////////////////////////////////////////////////////////////////////////
// FeedbackServiceServerTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.server.application;

import granite.feedback.service.requestreply.FeedbackServiceTest;
import granite.feedback.service.requestreply.IFeedbackCommandService;
import granite.feedback.service.requestreply.IFeedbackQueryService;
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
class FeedbackServiceServerTest
    extends FeedbackServiceTest
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
    protected IFeedbackCommandService
    getCommandSubject()
    {
        return context.getBean(IFeedbackCommandService.class);
    }

    @Override
    protected IFeedbackQueryService
    getQuerySubject()
    {
        return context.getBean(IFeedbackQueryService.class);
    }
}

//////////////////////////////////////////////////////////////////////////////
