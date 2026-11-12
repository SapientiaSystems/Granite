//////////////////////////////////////////////////////////////////////////////
// ContentCommandServiceClient.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.client.requestreply;

import granite.content.service.requestreply.*;
import jakarta.ws.rs.client.ClientBuilder;
import strata.client.core.service.CountedClient;
import strata.client.resteasy.service.RestEasyRestClient;

import java.util.concurrent.CompletionStage;

public
class ContentCommandServiceClient
    extends RestEasyRestClient
    implements IContentCommandServiceClient
{
    public
    ContentCommandServiceClient(ClientBuilder builder,String baseUrl)
    {
        super(
            builder,
            preprocessBaseUrl(baseUrl),
            "content-command-service/");
    }

    public
    ContentCommandServiceClient(CountedClient client,String baseUrl)
    {
        super(
            client,
            preprocessBaseUrl(baseUrl),
            "content-command-service/");
    }

    @Override
    public CompletionStage<UpsertContentDocumentsReply>
    upsertContentDocuments(UpsertContentDocumentsRequest request)
    {
        return
            doPostAsync(
                "upsert-content-documents",
                UpsertContentDocumentsReply.class,
                request);
    }

    @Override
    public CompletionStage<DestroyContentDocumentsReply>
    destroyContentDocuments(DestroyContentDocumentsRequest request)
    {
        return
            doPostAsync(
                "destroy-content-documents",
                DestroyContentDocumentsReply.class,
                request);
    }

    public ContentCommandServiceClient
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
