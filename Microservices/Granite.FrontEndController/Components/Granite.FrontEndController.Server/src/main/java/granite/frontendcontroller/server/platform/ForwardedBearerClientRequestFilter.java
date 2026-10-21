//////////////////////////////////////////////////////////////////////////////
// ForwardedBearerClientRequestFilter.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.platform;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

public
class ForwardedBearerClientRequestFilter
    implements ClientRequestFilter
{
    private static final String BEARER_PREFIX = "Bearer ";

    private final Optional<String> authorization;

    public
    ForwardedBearerClientRequestFilter(String authorization)
    {
        this.authorization =
            Optional
                .ofNullable(authorization)
                .filter(value -> value.regionMatches(true,0,BEARER_PREFIX,0,BEARER_PREFIX.length()));
    }

    @Override
    public void
    filter(ClientRequestContext context)
    {
        if (context.getHeaders().containsKey(HttpHeaders.AUTHORIZATION))
            return;

        authorization.ifPresent(value -> context.getHeaders().putSingle(HttpHeaders.AUTHORIZATION,value));
    }

    public boolean
    hasAuthorization() { return authorization.isPresent(); }

    public static ForwardedBearerClientRequestFilter
    fromCurrentRequest()
    {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        if (attributes instanceof ServletRequestAttributes servlet)
        {
            HttpServletRequest request = servlet.getRequest();

            return new ForwardedBearerClientRequestFilter(request.getHeader(HttpHeaders.AUTHORIZATION));
        }

        return new ForwardedBearerClientRequestFilter(null);
    }
}

//////////////////////////////////////////////////////////////////////////////
