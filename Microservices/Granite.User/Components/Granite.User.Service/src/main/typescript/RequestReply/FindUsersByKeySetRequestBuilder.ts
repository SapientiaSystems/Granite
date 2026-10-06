import {AbstractServiceRequestBuilder} from "strata.foundation.core/Transfer";
import {FindUsersByKeySetRequest} from "./FindUsersByKeySetRequest";
import {UserKeySet} from "./UserKeySet";

export
class FindUsersByKeySetRequestBuilder
    extends AbstractServiceRequestBuilder<FindUsersByKeySetRequest>
{
    private keys: UserKeySet;
    private returnIfFound: boolean;

    constructor()
    {
        super();
        this.keys = null;
        this.returnIfFound = false;
    }

    setKeys(keys: UserKeySet): FindUsersByKeySetRequestBuilder
    {
        this.keys = keys;
        return this;
    }

    getKeys(): UserKeySet
    {
        return this.keys;
    }

    setReturnIfFound(value: boolean): FindUsersByKeySetRequestBuilder
    {
        this.returnIfFound = value;
        return this;
    }

    getReturnIfFound(): boolean
    {
        return this.returnIfFound;
    }

    build(): FindUsersByKeySetRequest
    {
        const request: FindUsersByKeySetRequest =
            {
                requestId: this.getRequestId(),
                timestamp: this.getTimestamp().toString(),
                keys: this.getKeys(),
                returnIfFound: this.getReturnIfFound()
            };

        return request;
    }
}

