//////////////////////////////////////////////////////////////////////////////
// ServiceTokenClientRequestFilter.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.platform;

import granite.session.server.application.IServiceTokenProvider;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;

import java.util.Objects;

public
class ServiceTokenClientRequestFilter
    implements ClientRequestFilter
{
    private final IServiceTokenProvider provider;

    public
    ServiceTokenClientRequestFilter(IServiceTokenProvider provider)
    {
        this.provider = Objects.requireNonNull(provider,"provider");
    }

    @Override
    public void
    filter(ClientRequestContext context)
    {
        context
            .getHeaders()
            .putSingle(HttpHeaders.AUTHORIZATION,"Bearer " + provider.getToken());
    }
}

//////////////////////////////////////////////////////////////////////////////
