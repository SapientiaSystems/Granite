//////////////////////////////////////////////////////////////////////////////
// ClientCommandServiceClient.java
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
class ClientCommandServiceClient
    extends RestEasyRestClient
    implements IClientCommandServiceClient
{
    public
    ClientCommandServiceClient(ClientBuilder builder,String baseUrl)
    {
        super(
            builder,
            preprocessBaseUrl(baseUrl),
            "client-command-service/");
    }

    public
    ClientCommandServiceClient(CountedClient client,String baseUrl)
    {
        super(
            client,
            preprocessBaseUrl(baseUrl),
            "client-command-service/");
    }

    public
    ClientCommandServiceClient(CountedClient client,ExecutorService executor,String baseUrl)
    {
        super(
            client,
            executor,
            false,
            preprocessBaseUrl(baseUrl),
            "client-command-service/",
            new StandardResponseProcessor(),
            new BasicRetryExecutor());
    }

    @Override
    public CompletionStage<CreateClientReply>
    createClient(CreateClientRequest request)
    {
        return
            doPostAsync(
                "create-client",
                CreateClientReply.class,
                request);
    }

    @Override
    public CompletionStage<UpdateClientsReply>
    updateClients(UpdateClientsRequest request)
    {
        return
            doPostAsync(
                "update-clients",
                UpdateClientsReply.class,
                request);
    }

    @Override
    public CompletionStage<RotateClientSecretReply>
    rotateClientSecret(RotateClientSecretRequest request)
    {
        return
            doPostAsync(
                "rotate-client-secret",
                RotateClientSecretReply.class,
                request);
    }

    @Override
    public CompletionStage<RotateClientSecretReply>
    rotateOwnedClientSecret(RotateOwnedClientSecretRequest request)
    {
        return
            doPostAsync(
                "rotate-owned-client-secret",
                RotateClientSecretReply.class,
                request);
    }

    @Override
    public CompletionStage<RotateOwnClientSecretReply>
    rotateOwnClientSecret(RotateOwnClientSecretRequest request)
    {
        return
            doPostAsync(
                "rotate-own-client-secret",
                RotateOwnClientSecretReply.class,
                request);
    }

    @Override
    public CompletionStage<DestroyClientsReply>
    destroyClients(DestroyClientsRequest request)
    {
        return
            doPostAsync(
                "destroy-clients",
                DestroyClientsReply.class,
                request);
    }

    @Override
    public CompletionStage<VerifyClientCredentialsReply>
    verifyClientCredentials(VerifyClientCredentialsRequest request)
    {
        return
            doPostAsync(
                "verify-client-credentials",
                VerifyClientCredentialsReply.class,
                request);
    }

    public ClientCommandServiceClient
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
