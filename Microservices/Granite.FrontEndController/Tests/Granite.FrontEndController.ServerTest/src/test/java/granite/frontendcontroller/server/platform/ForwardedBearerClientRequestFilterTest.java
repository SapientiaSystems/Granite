//////////////////////////////////////////////////////////////////////////////
// ForwardedBearerClientRequestFilterTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.platform;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class ForwardedBearerClientRequestFilterTest
{
    @AfterEach
    public void
    tearDown()
    {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void
    forwardsInboundBearerToken()
    {
        bindInboundRequest("Bearer abc.def.ghi");

        MultivaluedMap<String,Object> headers = apply(ForwardedBearerClientRequestFilter.fromCurrentRequest(),new MultivaluedHashMap<>());

        assertEquals(List.of("Bearer abc.def.ghi"),headers.get("Authorization"));
    }

    @Test
    public void
    capturesTokenWhenCreatedNotWhenSent()
    {
        bindInboundRequest("Bearer first");

        ForwardedBearerClientRequestFilter filter = ForwardedBearerClientRequestFilter.fromCurrentRequest();

        RequestContextHolder.resetRequestAttributes();

        assertEquals(List.of("Bearer first"),apply(filter,new MultivaluedHashMap<>()).get("Authorization"));
    }

    @Test
    public void
    sendsNothingWithoutInboundRequestOrBearer()
    {
        assertFalse(ForwardedBearerClientRequestFilter.fromCurrentRequest().hasAuthorization());

        bindInboundRequest("Basic dXNlcjpwYXNz");

        ForwardedBearerClientRequestFilter filter = ForwardedBearerClientRequestFilter.fromCurrentRequest();

        assertFalse(filter.hasAuthorization());
        assertFalse(apply(filter,new MultivaluedHashMap<>()).containsKey("Authorization"));
    }

    @Test
    public void
    neverOverridesExplicitAuthorizationHeader()
    {
        bindInboundRequest("Bearer inbound");

        MultivaluedMap<String,Object> headers = new MultivaluedHashMap<>();

        headers.putSingle("Authorization","Bearer explicit");
        apply(ForwardedBearerClientRequestFilter.fromCurrentRequest(),headers);

        assertEquals(List.of("Bearer explicit"),headers.get("Authorization"));
        assertTrue(new ForwardedBearerClientRequestFilter("bearer lower-case").hasAuthorization());
    }

    private static void
    bindInboundRequest(String authorization)
    {
        MockHttpServletRequest request = new MockHttpServletRequest("POST","/frontendcontroller-user-service/handle-input");

        request.addHeader("Authorization",authorization);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private static MultivaluedMap<String,Object>
    apply(ForwardedBearerClientRequestFilter filter,MultivaluedMap<String,Object> headers)
    {
        ClientRequestContext context = Mockito.mock(ClientRequestContext.class);

        Mockito.when(context.getHeaders()).thenReturn(headers);
        filter.filter(context);
        return headers;
    }
}

//////////////////////////////////////////////////////////////////////////////
