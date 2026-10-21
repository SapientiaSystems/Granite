import {AbstractServiceRequestBuilder} from "strata.foundation.core/Transfer";
import {FindAllUsersRequest} from "./FindAllUsersRequest";

export
class FindAllUsersRequestBuilder
    extends AbstractServiceRequestBuilder<FindAllUsersRequest>
{
    constructor()
    {
        super();
    }

    build(): FindAllUsersRequest
    {
        const request: FindAllUsersRequest =
            {
                requestId: this.getRequestId(),
                timestamp: this.getTimestamp().toString()
            };

        return request;
    }
}
