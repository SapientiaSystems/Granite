//////////////////////////////////////////////////////////////////////////////
// ClientQueryServiceClient.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.client.requestreply;

import granite.client.service.requestreply.*;
import jakarta.ws.rs.client.ClientBuilder;
import strata.client.core.service.CountedClient;
import strata.client.core.service.StandardResponseProcessor;
import strata.client.resteasy.service.RestEasyRestClient;
import strata.foundation.core.utility.BasicRetryExecutor;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;

public
class ClientQueryServiceClient
    extends RestEasyRestClient
    implements IClientQueryServiceClient
{
    public
    ClientQueryServiceClient(ClientBuilder builder,String baseUrl)
    {
        super(
            builder,
            preprocessBaseUrl(baseUrl),
            "client-query-service/");
    }

    public
    ClientQueryServiceClient(CountedClient client,String baseUrl)
    {
        super(
            client,
            preprocessBaseUrl(baseUrl),
            "client-query-service/");
    }

    public
    ClientQueryServiceClient(CountedClient client,ExecutorService executor,String baseUrl)
    {
        super(
            client,
            executor,
            false,
            preprocessBaseUrl(baseUrl),
            "client-query-service/",
            new StandardResponseProcessor(),
            new BasicRetryExecutor());
    }

    @Override
    public CompletionStage<FindClientsReply>
    findAllClients(FindAllClientsRequest request)
    {
        return
            doPostAsync(
                "find-all-clients",
                FindClientsReply.class,
                request);
    }

    @Override
    public CompletionStage<FindClientsReply>
    findClientsByIds(FindClientsByIdsRequest request)
    {
        return
            doPostAsync(
                "find-clients-by-ids",
                FindClientsReply.class,
                request);
    }

    @Override
    public CompletionStage<FindClientsReply>
    findClientsByOwner(FindClientsByOwnerRequest request)
    {
        return
            doPostAsync(
                "find-clients-by-owner",
                FindClientsReply.class,
                request);
    }

    public ClientQueryServiceClient
    setAuthorizationHeader(String accessToken)
    {
        setHeader("Authorization","Bearer " + accessToken);
        return this;
    }

    private static String
    preprocessBaseUrl(String baseUrl)
    {
        return
            baseUrl.endsWith("/")
                ? baseUrl
                : baseUrl + '/';
    }
}

//////////////////////////////////////////////////////////////////////////////
