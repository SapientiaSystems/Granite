//////////////////////////////////////////////////////////////////////////////
// UserQueryServiceClient.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.client.requestreply;

import granite.user.service.requestreply.*;
import jakarta.ws.rs.client.ClientBuilder;
import strata.client.resteasy.service.RestEasyRestClient;

import java.util.concurrent.CompletionStage;

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

    public UserQueryServiceClient
    setAuthorizationHeader(String accessToken)
    {
        setHeader("Authorization", "Bearer " + accessToken);
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
