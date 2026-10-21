//////////////////////////////////////////////////////////////////////////////
// PlatformContextLoadsTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.platform;

import granite.session.server.application.IJwtTokenService;
import granite.session.server.application.IOauth2IdTokenValidator;
import granite.session.server.application.ITransactionalSessionCommandService;
import granite.session.server.application.ITransactionalSessionQueryService;
import granite.session.service.event.ISessionEventSender;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("CommitStage")
@SpringJUnitConfig(PlatformContextTestConfiguration.class)
@ActiveProfiles("local")
public
class PlatformContextLoadsTest
{
    @Autowired
    private ApplicationContext context;

    @Test
    public void
    contextLoadsWithAllBeansWired()
    {
        assertNotNull(context.getBean(IJwtTokenService.class));
        assertNotNull(context.getBean(IOauth2IdTokenValidator.class));
        assertNotNull(context.getBean(Clock.class));
        assertTrue(
            context.getBeanNamesForType(ISessionEventSender.class).length > 0);
        assertTrue(
            context.getBeanNamesForType(
                ITransactionalSessionCommandService.class).length > 0);
        assertTrue(
            context.getBeanNamesForType(
                ITransactionalSessionQueryService.class).length > 0);
        assertTrue(
            context.getBeanNamesForType(
                SessionCommandServiceController.class).length > 0);
    }
}

//////////////////////////////////////////////////////////////////////////////
