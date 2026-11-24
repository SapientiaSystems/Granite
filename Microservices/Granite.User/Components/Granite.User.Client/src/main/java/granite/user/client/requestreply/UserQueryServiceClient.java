//////////////////////////////////////////////////////////////////////////////
// UserQueryServiceClient.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.client.requestreply;

import granite.user.service.requestreply.*;
import jakarta.ws.rs.client.ClientBuilder;
import strata.client.core.service.CountedClient;
import strata.client.core.service.StandardResponseProcessor;
import strata.client.resteasy.service.RestEasyRestClient;
import strata.foundation.core.utility.BasicRetryExecutor;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;

public
class UserQueryServiceClient
    extends RestEasyRestClient
    implements IUserQueryServiceClient
{
    public
    UserQueryServiceClient(ClientBuilder builder, String baseUrl)
    {
        super(
            builder,
            preprocessBaseUrl(baseUrl),
            "user-query-service/");
    }

    public
    UserQueryServiceClient(CountedClient client, String baseUrl)
    {
        super(
            client,
            preprocessBaseUrl(baseUrl),
            "user-query-service/");
    }

    public
    UserQueryServiceClient(CountedClient client, ExecutorService executor, String baseUrl)
    {
        super(
            client,
            executor,
            false,
            preprocessBaseUrl(baseUrl),
            "user-query-service/",
            new StandardResponseProcessor(),
            new BasicRetryExecutor());
    }

    @Override
    public CompletionStage<FindUsersByKeyReply>
    findUsersByKey(FindUsersByKeyRequest request)
    {
        return
            doPostAsync(
                "find-users-by-key",
                FindUsersByKeyReply.class,
                request);
    }

    @Override
    public CompletionStage<FindUsersByKeySetReply>
    findUsersByKeySet(FindUsersByKeySetRequest request)
    {
        return
            doPostAsync(
                "find-users-by-keyset",
                FindUsersByKeySetReply.class,
                request);
    }

    @Override
    public CompletionStage<FindUsersByExternalIdentityReply>
    findUsersByExternalIdentity(FindUsersByExternalIdentityRequest request)
    {
        return
            doPostAsync(
                "find-users-by-external-identity",
                FindUsersByExternalIdentityReply.class,
                request);
    }

    @Override
    public CompletionStage<FindAllUsersReply>
    findAllUsers(FindAllUsersRequest request)
    {
        return
            doPostAsync(
                "find-all-users",
                FindAllUsersReply.class,
                request);
    }

    public UserQueryServiceClient
    setAuthorizationHeader(String accessToken)
    {
        setHeader("Authorization", "Bearer " + accessToken);
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
