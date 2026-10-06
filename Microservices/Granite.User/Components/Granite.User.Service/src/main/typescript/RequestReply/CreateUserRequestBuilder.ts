import {AbstractServiceRequestBuilder} from "strata.foundation.core/Transfer";
import {CreateUserRequest} from "./CreateUserRequest";
import {CreateUserData} from "./CreateUserData";

export
class CreateUserRequestBuilder
    extends AbstractServiceRequestBuilder<CreateUserRequest>
{
    private user: CreateUserData;

    constructor()
    {
        super();
        this.user = null;
    }

    setUser(user:CreateUserData): CreateUserRequestBuilder
    {
        this.user = user;
        return this;
    }

    getUser(): CreateUserData
    {
        return this.user;
    }

    build(): CreateUserRequest
    {
        const request: CreateUserRequest =
            {
                requestId: this.getRequestId(),
                timestamp: this.getTimestamp().toString(),
                user: this.getUser()
            };

        return request;
    }
}