import {AbstractServiceRequest} from "strata.foundation.core/Transfer";

export
interface CompleteOktaLoginRequest
    extends AbstractServiceRequest
{
    code?: string;
    state?: string;
    transactionId?: string;
    deviceId?: string;
}
