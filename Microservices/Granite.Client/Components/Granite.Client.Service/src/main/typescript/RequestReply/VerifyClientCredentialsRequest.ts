import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";

export
interface VerifyClientCredentialsRequest
    extends AbstractServiceRequest
{
    clientId?: string;
    clientSecret?: string;
}
