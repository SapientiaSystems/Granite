import {AbstractServiceRequest} from "strata.foundation.core/Transfer";

export
interface IssueServiceTokenRequest
    extends AbstractServiceRequest
{
    clientId?: string;
    clientSecret?: string;
}
