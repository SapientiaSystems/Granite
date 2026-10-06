import {AbstractServiceRequestBuilder} from "strata.foundation.core/Transfer";
import {FindUsersByKeyRequest} from "./FindUsersByKeyRequest";
import {UserKey} from "./UserKey";

export
class FindUsersByKeyRequestBuilder
    extends AbstractServiceRequestBuilder<FindUsersByKeyRequest>
{
    private key: UserKey;
    private returnIfFound: boolean;

    constructor()
    {
        super();
        this.key = null;
        this.returnIfFound = false;
    }

    setKey(key: UserKey): FindUsersByKeyRequestBuilder
    {
        this.key = key;
        return this;
    }

    getKey(): UserKey
    {
        return this.key;
    }

    setReturnIfFound(value: boolean): FindUsersByKeyRequestBuilder
    {
        this.returnIfFound = value;
        return this;
    }

    getReturnIfFound(): boolean
    {
        return this.returnIfFound;
    }

    build(): FindUsersByKeyRequest
    {
        const request: FindUsersByKeyRequest =
            {
                requestId: this.getRequestId(),
                timestamp: this.getTimestamp().toString(),
                key: this.getKey(),
                returnIfFound: this.getReturnIfFound()
            };

        return request;
    }
}

