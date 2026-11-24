//////////////////////////////////////////////////////////////////////////////
// SessionQueryServiceClient.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.client.requestreply;

import granite.session.service.requestreply.*;
import jakarta.ws.rs.client.ClientBuilder;
import strata.client.core.service.CountedClient;
import strata.client.core.service.StandardResponseProcessor;
import strata.client.resteasy.service.RestEasyRestClient;
import strata.foundation.core.utility.BasicRetryExecutor;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;

public
class SessionQueryServiceClient
    extends RestEasyRestClient
    implements ISessionQueryServiceClient
{
    public
    SessionQueryServiceClient(ClientBuilder builder, String baseUrl)
    {
        super(
            builder,
            preprocessBaseUrl(baseUrl),
            "session-query-service/");
    }

    public
    SessionQueryServiceClient(CountedClient client, String baseUrl)
    {
        super(
            client,
            preprocessBaseUrl(baseUrl),
            "session-query-service/");
    }

    public
    SessionQueryServiceClient(CountedClient client, ExecutorService executor, String baseUrl)
    {
        super(
            client,
            executor,
            false,
            preprocessBaseUrl(baseUrl),
            "session-query-service/",
            new StandardResponseProcessor(),
            new BasicRetryExecutor());
    }

    @Override
    public CompletionStage<FindSessionsByKeyReply>
    findSessionsByKey(FindSessionsByKeyRequest request)
    {
        return
            doPostAsync(
                "find-sessions-by-key",
                FindSessionsByKeyReply.class,
                request);
    }

    @Override
    public CompletionStage<FindSessionsByKeySetReply>
    findSessionsByKeySet(FindSessionsByKeySetRequest request)
    {
        return
            doPostAsync(
                "find-sessions-by-keyset",
                FindSessionsByKeySetReply.class,
                request);
    }

    @Override
    public CompletionStage<FindSessionByUserIdAndDeviceIdReply>
    findSessionByUserIdAndDeviceId(FindSessionByUserIdAndDeviceIdRequest request)
    {
        return
            doPostAsync(
                "find-session-by-user-id-and-device-id",
                FindSessionByUserIdAndDeviceIdReply.class,
                request);
    }

    @Override
    public CompletionStage<FindSessionsReply>
    findSessions(FindSessionsRequest request)
    {
        return
            doPostAsync(
                "find-sessions",
                FindSessionsReply.class,
                request);
    }

    @Override
    public SessionQueryServiceClient
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
