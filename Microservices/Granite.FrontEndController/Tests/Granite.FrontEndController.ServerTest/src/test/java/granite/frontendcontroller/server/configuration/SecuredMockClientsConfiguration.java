//////////////////////////////////////////////////////////////////////////////
// SecuredMockClientsConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import org.mockito.Mockito;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

@Configuration
public
class SecuredMockClientsConfiguration
    extends GraniteMockClientsConfiguration
{
    @Override
    protected RestOperations
    createJwkSetRestOperations(Environment environment)
    {
        RestOperations operations = Mockito.mock(RestOperations.class);

        Mockito
            .when(operations.exchange(Mockito.any(RequestEntity.class),Mockito.eq(String.class)))
            .thenReturn(ResponseEntity.ok(TestJwks.jwkSetJson()));

        return operations;
    }
}

//////////////////////////////////////////////////////////////////////////////
