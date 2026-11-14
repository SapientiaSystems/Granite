import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";

export
interface RotateOwnedClientSecretRequest
    extends AbstractServiceRequest
{
    clientId?: string;
    ownerUserId?: number;
}
