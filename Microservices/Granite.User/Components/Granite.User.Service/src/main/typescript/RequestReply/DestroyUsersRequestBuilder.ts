import {AbstractServiceRequestBuilder} from "strata.foundation.core/Transfer";
import {DestroyUsersRequest} from "./DestroyUsersRequest";

export
class DestroyUsersRequestBuilder
    extends AbstractServiceRequestBuilder<DestroyUsersRequest>
{
    private userIds: Array<number>;

    constructor()
    {
        super();
        this.userIds = null;
    }

    setUserIds(ids: Array<number>): DestroyUsersRequestBuilder
    {
        this.userIds = ids;
        return this;
    }

    getUserIds(): Array<number>
    {
        return this.userIds;
    }

    build(): DestroyUsersRequest
    {
        const request: DestroyUsersRequest =
            {
                requestId: this.getRequestId(),
                timestamp: this.getTimestamp().toString(),
                userIds: this.getUserIds()
            };

        return request;
    }
}

