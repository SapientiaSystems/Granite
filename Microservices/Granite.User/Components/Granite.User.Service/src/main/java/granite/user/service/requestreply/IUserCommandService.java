//////////////////////////////////////////////////////////////////////////////
// IUserCommandService.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import java.util.concurrent.CompletionStage;

public
interface IUserCommandService
{
    CompletionStage<CreateUserReply>
    createUser(CreateUserRequest request);

    CompletionStage<CreateUsersReply>
    createUsers(CreateUsersRequest request);

    CompletionStage<UpdateUserReply>
    updateUser(UpdateUserRequest request);

    CompletionStage<UpdateUsersReply>
    updateUsers(UpdateUsersRequest request);

    CompletionStage<DestroyUserReply>
    destroyUser(DestroyUserRequest request);

    CompletionStage<DestroyUsersReply>
    destroyUsers(DestroyUsersRequest request);

    CompletionStage<AuthenticateUserReply>
    authenticateUser(AuthenticateUserRequest request);

}

//////////////////////////////////////////////////////////////////////////////