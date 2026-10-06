import {AbstractServiceRequestBuilder} from "strata.foundation.core/Transfer";
import {UpdateUserRequest} from "./UpdateUserRequest";
import {UpdateUserData} from "./UpdateUserData";

export
class UpdateUserRequestBuilder
    extends AbstractServiceRequestBuilder<UpdateUserRequest>
{
    private user: UpdateUserData;

    constructor()
    {
        super();
        this.user = null;
    }

    setUser(user: UpdateUserData): UpdateUserRequestBuilder
    {
        this.user = user;
        return this;
    }

    getUser(): UpdateUserData
    {
        return this.user;
    }

    build(): UpdateUserRequest
    {
        const request: UpdateUserRequest =
            {
                requestId: this.getRequestId(),
                timestamp: this.getTimestamp().toString(),
                user: this.getUser()
            };

        return request;
    }
}