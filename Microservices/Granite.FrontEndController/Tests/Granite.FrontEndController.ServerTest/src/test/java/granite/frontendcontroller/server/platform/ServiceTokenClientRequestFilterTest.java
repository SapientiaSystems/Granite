//////////////////////////////////////////////////////////////////////////////
// ServiceTokenClientRequestFilterTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.platform;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("CommitStage")
public
class ServiceTokenClientRequestFilterTest
{
    @Test
    public void
    setsServiceTokenReplacingAnyOtherBearer()
    {
        MultivaluedMap<String,Object> headers = new MultivaluedHashMap<>();
        ClientRequestContext          context = Mockito.mock(ClientRequestContext.class);

        headers.putSingle("Authorization","Bearer caller.jwt.token");
        Mockito.when(context.getHeaders()).thenReturn(headers);

        new ServiceTokenClientRequestFilter(() -> "service.jwt.token").filter(context);

        assertEquals(List.of("Bearer service.jwt.token"),headers.get("Authorization"));
    }
}

//////////////////////////////////////////////////////////////////////////////
