import type { AbstractServiceReply } from "strata.foundation.core/Transfer";

export
interface RotateOwnClientSecretReply
    extends AbstractServiceReply
{
    clientId?: string;
    clientSecret?: string;
}
