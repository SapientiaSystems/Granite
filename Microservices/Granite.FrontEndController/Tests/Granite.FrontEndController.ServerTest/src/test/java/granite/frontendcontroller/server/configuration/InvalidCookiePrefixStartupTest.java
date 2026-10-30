//////////////////////////////////////////////////////////////////////////////
// InvalidCookiePrefixStartupTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("CommitStage")
public
class InvalidCookiePrefixStartupTest
{
    @Test
    public void
    invalidCookiePrefixFailsWhenTheContextStarts()
    {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();

        context.setServletContext(new MockServletContext());
        context
            .getEnvironment()
            .getPropertySources()
            .addFirst(new MapPropertySource("test",Map.of("granite.frontendcontroller.service-path","/x","granite.security.jwt.jwk-set-uri","https://session.invalid/.well-known/jwks.json")));
        context.register(LowercasePrefixTestSupport.class,GraniteMockClientsConfiguration.class);

        try
        {
            assertThrows(Exception.class,context::refresh);
        }
        finally
        {
            context.close();
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
