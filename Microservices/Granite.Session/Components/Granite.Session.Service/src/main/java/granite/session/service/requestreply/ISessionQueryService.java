//////////////////////////////////////////////////////////////////////////////
// ISessionQueryService.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import java.util.concurrent.CompletionStage;

public
interface ISessionQueryService
{
    CompletionStage<FindSessionsByKeyReply>
    findSessionsByKey(FindSessionsByKeyRequest request);

    CompletionStage<FindSessionsByKeySetReply>
    findSessionsByKeySet(FindSessionsByKeySetRequest request);

    CompletionStage<FindSessionByUserIdAndDeviceIdReply>
    findSessionByUserIdAndDeviceId(FindSessionByUserIdAndDeviceIdRequest request);

}

//////////////////////////////////////////////////////////////////////////////
