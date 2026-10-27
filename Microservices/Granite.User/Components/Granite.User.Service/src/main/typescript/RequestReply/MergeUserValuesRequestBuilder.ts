import {AbstractServiceRequestBuilder} from "strata.foundation.core/Transfer";
import {MergeUserValuesRequest} from "./MergeUserValuesRequest";

export
class MergeUserValuesRequestBuilder
    extends AbstractServiceRequestBuilder<MergeUserValuesRequest>
{
    private userId: number;
    private values: Record<string,unknown>;

    constructor()
    {
        super();
        this.userId = null;
        this.values = null;
    }

    setUserId(userId: number): MergeUserValuesRequestBuilder
    {
        this.userId = userId;
        return this;
    }

    getUserId(): number
    {
        return this.userId;
    }

    setValues(values: Record<string,unknown>): MergeUserValuesRequestBuilder
    {
        this.values = values;
        return this;
    }

    getValues(): Record<string,unknown>
    {
        return this.values;
    }

    build(): MergeUserValuesRequest
    {
        const request: MergeUserValuesRequest =
            {
                requestId: this.getRequestId(),
                timestamp: this.getTimestamp().toString(),
                userId: this.getUserId(),
                values: this.getValues()
            };

        return request;
    }
}
