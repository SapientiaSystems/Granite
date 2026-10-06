import {AbstractServiceRequestBuilder} from "strata.foundation.core/Transfer";
import {CreateUsersRequest} from "./CreateUsersRequest";
import {CreateUserData} from "./CreateUserData";

export
class CreateUsersRequestBuilder
    extends AbstractServiceRequestBuilder<CreateUsersRequest>
{
    private usersToCreate: Array<CreateUserData>;

    constructor()
    {
        super();
        this.usersToCreate = null;
    }

    setUsersToCreate(users: Array<CreateUserData>): CreateUsersRequestBuilder
    {
        this.usersToCreate = users;
        return this;
    }

    getUsersToCreate(): Array<CreateUserData>
    {
        return this.usersToCreate;
    }

    build(): CreateUsersRequest
    {
        const request: CreateUsersRequest =
            {
                requestId: this.getRequestId(),
                timestamp: this.getTimestamp().toString(),
                usersToCreate: this.getUsersToCreate()
            };

        return request;
    }
}

