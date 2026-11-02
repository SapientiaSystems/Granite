import {AbstractServiceRequest} from "strata.foundation.core/Transfer";

export
interface IssueDelegatedTokenRequest
    extends AbstractServiceRequest
{
    clientId?: string;
    clientSecret?: string;
    sessionId?: number;
    userId?: number;
}
