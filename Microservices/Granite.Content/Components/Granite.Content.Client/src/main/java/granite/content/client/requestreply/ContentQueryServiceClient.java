//////////////////////////////////////////////////////////////////////////////
// ContentQueryServiceClient.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.client.requestreply;

import granite.content.service.requestreply.*;
import jakarta.ws.rs.client.ClientBuilder;
import strata.client.resteasy.service.RestEasyRestClient;

import java.util.concurrent.CompletionStage;

public
class ContentQueryServiceClient
    extends RestEasyRestClient
    implements IContentQueryServiceClient
{
    public
    ContentQueryServiceClient(ClientBuilder builder,String baseUrl)
    {
        super(
            builder,
            preprocessBaseUrl(baseUrl),
            "content-query-service/");
    }

    @Override
    public CompletionStage<SearchContentReply>
    searchContent(SearchContentRequest request)
    {
        return
            doPostAsync(
                "search-content",
                SearchContentReply.class,
                request);
    }

    @Override
    public CompletionStage<FindContentDocumentsByKeysReply>
    findContentDocumentsByKeys(FindContentDocumentsByKeysRequest request)
    {
        return
            doPostAsync(
                "find-content-documents-by-keys",
                FindContentDocumentsByKeysReply.class,
                request);
    }

    @Override
    public CompletionStage<FindAllContentDocumentsReply>
    findAllContentDocuments(FindAllContentDocumentsRequest request)
    {
        return
            doPostAsync(
                "find-all-content-documents",
                FindAllContentDocumentsReply.class,
                request);
    }

    public ContentQueryServiceClient
    setAuthorizationHeader(String accessToken)
    {
        setHeader("Authorization","Bearer " + accessToken);
        return this;
    }

    @Override
    public void
    close() {}

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
