//////////////////////////////////////////////////////////////////////////////
// ContentServiceServerTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.application;

import granite.content.service.requestreply.ContentServiceTest;
import granite.content.service.requestreply.IContentCommandService;
import granite.content.service.requestreply.IContentQueryService;
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
class ContentServiceServerTest
    extends ContentServiceTest
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
    protected IContentCommandService
    getCommandSubject()
    {
        return context.getBean(IContentCommandService.class);
    }

    @Override
    protected IContentQueryService
    getQuerySubject()
    {
        return context.getBean(IContentQueryService.class);
    }
}

//////////////////////////////////////////////////////////////////////////////
