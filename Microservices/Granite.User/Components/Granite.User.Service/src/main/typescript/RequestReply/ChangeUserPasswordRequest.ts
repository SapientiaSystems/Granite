import {AbstractServiceRequest} from "strata.foundation.core/Transfer/";

export
interface ChangeUserPasswordRequest
    extends AbstractServiceRequest
{
    userId: number;
    currentPassword: string;
    newPassword: string;
}
