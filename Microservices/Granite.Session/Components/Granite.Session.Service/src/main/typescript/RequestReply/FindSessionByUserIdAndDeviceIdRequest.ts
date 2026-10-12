import {AbstractServiceRequest} from "strata.foundation.core/Transfer";

export
interface FindSessionByUserIdAndDeviceIdRequest
    extends AbstractServiceRequest
{
    userId?: number;
    deviceId?: string;
}
