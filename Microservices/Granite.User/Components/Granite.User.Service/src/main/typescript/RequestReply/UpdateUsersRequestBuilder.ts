import {AbstractServiceRequestBuilder} from "strata.foundation.core/Transfer";
import {UpdateUsersRequest} from "./UpdateUsersRequest";
import {UpdateUserData} from "./UpdateUserData";

export
class UpdateUsersRequestBuilder
    extends AbstractServiceRequestBuilder<UpdateUsersRequest>
{
    private usersToUpdate: Array<UpdateUserData>;

    constructor()
    {
        super();
        this.usersToUpdate = null;
    }

    setUsersToUpdate(users: Array<UpdateUserData>): UpdateUsersRequestBuilder
    {
        this.usersToUpdate = users;
        return this;
    }

    getUsersToUpdate(): Array<UpdateUserData>
    {
        return this.usersToUpdate;
    }

    build(): UpdateUsersRequest
    {
        const request: UpdateUsersRequest =
            {
                requestId: this.getRequestId(),
                timestamp: this.getTimestamp().toString(),
                usersToUpdate: this.getUsersToUpdate()
            };

        return request;
    }
}

