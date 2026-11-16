import {AbstractServiceRequest} from "strata.foundation.core/Transfer";

export
interface ChangePasswordRequest
    extends AbstractServiceRequest
{
    sessionId: number;
    currentPassword: string;
    newPassword: string;
}
