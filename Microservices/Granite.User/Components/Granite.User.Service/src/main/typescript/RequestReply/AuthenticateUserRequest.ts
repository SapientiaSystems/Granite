import {AbstractServiceRequest} from "strata.foundation.core/Transfer/";
import {CredentialKind} from "./CredentialKind";

export
interface AuthenticateUserRequest
    extends AbstractServiceRequest
{
    userName: string;
    credential: string;
    credentialKind: CredentialKind;
}
