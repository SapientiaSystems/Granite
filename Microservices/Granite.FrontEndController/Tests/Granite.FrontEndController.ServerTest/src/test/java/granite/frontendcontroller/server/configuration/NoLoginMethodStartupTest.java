//////////////////////////////////////////////////////////////////////////////
// NoLoginMethodStartupTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import granite.frontendcontroller.server.control.session.SessionLoginSettings;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("CommitStage")
public
class NoLoginMethodStartupTest
{
    @Test
    public void
    noEnabledLoginMethodFailsWhenTheContextStarts()
    {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();

        context.setServletContext(new MockServletContext());
        context
            .getEnvironment()
            .getPropertySources()
            .addFirst(new MapPropertySource("test",Map.of("granite.frontendcontroller.service-path","/x","granite.security.jwt.jwk-set-uri","https://session.invalid/.well-known/jwks.json")));
        context.register(NoLoginMethodTestSupport.class,GraniteMockClientsConfiguration.class);

        try
        {
            Exception failure = assertThrows(Exception.class,context::refresh);

            assertEquals(SessionLoginSettings.NONE_ENABLED,NestedExceptionUtils.getMostSpecificCause(failure).getMessage());
        }
        finally
        {
            context.close();
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
