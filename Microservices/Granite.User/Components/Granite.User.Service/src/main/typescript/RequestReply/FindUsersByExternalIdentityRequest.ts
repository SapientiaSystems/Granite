import {AbstractServiceRequest} from "strata.foundation.core/Transfer";

export
interface FindUsersByExternalIdentityRequest
    extends AbstractServiceRequest
{
    provider: string;
    externalId: string;
}
