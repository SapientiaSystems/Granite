//////////////////////////////////////////////////////////////////////////////
// UserCommandServiceClient.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.client.requestreply;

import granite.user.service.requestreply.*;
import jakarta.ws.rs.client.ClientBuilder;
import strata.client.resteasy.service.RestEasyRestClient;

import java.util.concurrent.CompletionStage;

public
class UserCommandServiceClient
    extends RestEasyRestClient
    implements IUserCommandServiceClient
{
    public
    UserCommandServiceClient(ClientBuilder builder, String baseUrl)
    {
        super(
            builder,
            preprocessBaseUrl(baseUrl),
            "user-service/");
    }

    @Override
    public CompletionStage<CreateUserReply>
    createUser(CreateUserRequest request)
    {
        return
            doPostAsync(
                "create-user",
                CreateUserReply.class,
                request);
    }

    @Override
    public CompletionStage<UpdateUserReply>
    updateUser(UpdateUserRequest request)
    {
        return
            doPostAsync(
                "update-user",
                UpdateUserReply.class,
                request);
    }

    @Override
    public CompletionStage<UpdateUsersReply>
    updateUsers(UpdateUsersRequest request)
    {
        return
            doPostAsync(
                "update-users",
                UpdateUsersReply.class,
                request);
    }

    @Override
    public CompletionStage<DestroyUserReply>
    destroyUser(DestroyUserRequest request)
    {
        return
            doPostAsync(
                "destroy-user",
                DestroyUserReply.class,
                request);
    }

    @Override
    public CompletionStage<CreateUsersReply>
    createUsers(CreateUsersRequest request)
    {
        return
            doPostAsync(
                "create-users",
                CreateUsersReply.class,
                request);
    }


    @Override
    public CompletionStage<DestroyUsersReply>
    destroyUsers(DestroyUsersRequest request)
    {
        return
            doPostAsync(
                "destroy-users",
                DestroyUsersReply.class,
                request);
    }

    @Override
    public CompletionStage<AuthenticateUserReply>
    authenticateUser(AuthenticateUserRequest request)
    {
        return
            doPostAsync(
                "authenticate-user",
                AuthenticateUserReply.class,
                request);
    }

    /*
    @Override
    public CompletionStage<FindUserReply>
    findUser(FindUserRequest request)
    {
        return
            doPostAsync(
                "find-user",
                FindUserReply.class,
                request);
    }
    */
    public UserCommandServiceClient
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
