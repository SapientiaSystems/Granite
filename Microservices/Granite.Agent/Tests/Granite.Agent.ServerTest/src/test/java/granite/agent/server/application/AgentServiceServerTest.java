//////////////////////////////////////////////////////////////////////////////
// AgentServiceServerTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.application;

import granite.agent.service.requestreply.AgentServiceTest;
import granite.agent.service.requestreply.IAgentCommandService;
import granite.agent.service.requestreply.IAgentQueryService;
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
class AgentServiceServerTest
    extends AgentServiceTest
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
    protected IAgentCommandService
    getCommandSubject()
    {
        return context.getBean(IAgentCommandService.class);
    }

    @Override
    protected IAgentQueryService
    getQuerySubject()
    {
        return context.getBean(IAgentQueryService.class);
    }
}

//////////////////////////////////////////////////////////////////////////////
