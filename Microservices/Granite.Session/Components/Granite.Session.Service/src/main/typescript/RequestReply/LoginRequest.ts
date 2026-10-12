import {AbstractServiceRequest} from "strata.foundation.core/Transfer";
import {CredentialKind} from "granite.user.service/RequestReply";

export
interface LoginRequest
    extends AbstractServiceRequest
{
    userName?: string;
    credential?: string;
    credentialKind?: CredentialKind;
    deviceId?: string;
}
