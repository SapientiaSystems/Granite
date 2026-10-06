import {AbstractServiceRequestBuilder} from "strata.foundation.core/Transfer";
import {DestroyUserRequest} from "./DestroyUserRequest";

export
class DestroyUserRequestBuilder
    extends AbstractServiceRequestBuilder<DestroyUserRequest>
{
    private userId: number;

    constructor()
    {
        super();
        this.userId = null;
    }

    setUserId(id: number): DestroyUserRequestBuilder
    {
        this.userId = id;
        return this;
    }

    getUserId(): number
    {
        return this.userId;
    }

    build(): DestroyUserRequest
    {
        const request: DestroyUserRequest =
            {
                requestId: this.getRequestId(),
                timestamp: this.getTimestamp().toString(),
                userId: this.getUserId()
            };

        return request;
    }
}