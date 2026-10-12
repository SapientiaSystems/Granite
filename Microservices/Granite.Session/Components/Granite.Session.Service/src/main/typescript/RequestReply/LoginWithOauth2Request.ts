import {AbstractServiceRequest} from "strata.foundation.core/Transfer";

export
interface LoginWithOauth2Request
    extends AbstractServiceRequest
{
    provider?: string;
    idToken?: string;
    deviceId?: string;
}
