//////////////////////////////////////////////////////////////////////////////
// GraniteClientServiceClients.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.application;

import granite.client.client.requestreply.IClientCommandServiceClient;
import granite.client.client.requestreply.IClientQueryServiceClient;

import java.util.Objects;
import java.util.function.Supplier;

public
class GraniteClientServiceClients
{
    private Supplier<IClientCommandServiceClient> clientCommandClient;
    private Supplier<IClientQueryServiceClient>   clientQueryClient;

    public
    GraniteClientServiceClients()
    {
        clientCommandClient = unavailable("client command");
        clientQueryClient   = unavailable("client query");
    }

    public GraniteClientServiceClients
    setClientCommandClient(Supplier<IClientCommandServiceClient> supplier)
    {
        clientCommandClient = memoize(Objects.requireNonNull(supplier,"supplier"));
        return this;
    }

    public GraniteClientServiceClients
    setClientQueryClient(Supplier<IClientQueryServiceClient> supplier)
    {
        clientQueryClient = memoize(Objects.requireNonNull(supplier,"supplier"));
        return this;
    }

    public IClientCommandServiceClient
    getClientCommandClient() { return clientCommandClient.get(); }

    public IClientQueryServiceClient
    getClientQueryClient() { return clientQueryClient.get(); }

    private static <T> Supplier<T>
    unavailable(String name)
    {
        return
            () ->
            {
                throw new IllegalStateException(name + " client is not configured");
            };
    }

    private static <T> Supplier<T>
    memoize(Supplier<T> supplier)
    {
        Object[] holder = new Object[1];

        return
            () ->
            {
                synchronized (holder)
                {
                    if (holder[0] == null)
                        holder[0] = Objects.requireNonNull(supplier.get(),"client");

                    @SuppressWarnings("unchecked")
                    T value = (T)holder[0];

                    return value;
                }
            };
    }
}

//////////////////////////////////////////////////////////////////////////////
