//////////////////////////////////////////////////////////////////////////////
// IUserQueryService.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import java.util.concurrent.CompletionStage;

public
interface IUserQueryService 
{
    CompletionStage<FindUsersByKeyReply>
    findUsersByKey(FindUsersByKeyRequest request);

    CompletionStage<FindUsersByKeySetReply>
    findUsersByKeySet(FindUsersByKeySetRequest request);

    CompletionStage<FindUsersByExternalIdentityReply>
    findUsersByExternalIdentity(FindUsersByExternalIdentityRequest request);

    CompletionStage<FindAllUsersReply>
    findAllUsers(FindAllUsersRequest request);
}

//////////////////////////////////////////////////////////////////////////////