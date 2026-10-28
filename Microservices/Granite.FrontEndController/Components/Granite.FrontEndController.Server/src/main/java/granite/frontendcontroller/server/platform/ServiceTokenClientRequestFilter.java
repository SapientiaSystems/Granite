//////////////////////////////////////////////////////////////////////////////
// ServiceTokenClientRequestFilter.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.platform;

import granite.frontendcontroller.server.application.IServiceTokenSource;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;

import java.util.Objects;

public
class ServiceTokenClientRequestFilter
    implements ClientRequestFilter
{
    private final IServiceTokenSource source;

    public
    ServiceTokenClientRequestFilter(IServiceTokenSource source)
    {
        this.source = Objects.requireNonNull(source,"source");
    }

    @Override
    public void
    filter(ClientRequestContext context)
    {
        context
            .getHeaders()
            .putSingle(HttpHeaders.AUTHORIZATION,"Bearer " + source.getToken());
    }
}

//////////////////////////////////////////////////////////////////////////////
