import {AbstractServiceRequest} from "strata.foundation.core/Transfer";

export
interface StartOktaLoginRequest
    extends AbstractServiceRequest
{
    deviceId?: string;
    returnTo?: string;
}
