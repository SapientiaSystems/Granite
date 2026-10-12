//////////////////////////////////////////////////////////////////////////////
// SessionQueryServiceServerTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.service.requestreply.ISessionCommandService;
import granite.session.service.requestreply.ISessionQueryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.request.RequestContextHolder;
import strata.server.spring.inject.RequestAttributeMap;

@Tag("CommitStage")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
@ActiveProfiles("dev")
public
class SessionQueryServiceServerTest
    extends granite.session.service.requestreply.SessionQueryServiceTest
{
    @Autowired
    private ISessionQueryService subject;

    @Autowired
    private ISessionCommandService commandSubject;

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
    protected ISessionQueryService
    getSubject() { return subject; }

    @Override
    protected ISessionCommandService
    getCommandSubject() { return commandSubject; }
}

//////////////////////////////////////////////////////////////////////////////
