import {AbstractServiceRequest} from "strata.foundation.core/Transfer/";

export
interface MergeUserValuesRequest
    extends AbstractServiceRequest
{
    userId: number;
    values: Record<string,unknown>;
}
