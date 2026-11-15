import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";

export
interface RotateClientSecretRequest
    extends AbstractServiceRequest
{
    clientId?: string;
    secretExpiresAt?: string;
}
