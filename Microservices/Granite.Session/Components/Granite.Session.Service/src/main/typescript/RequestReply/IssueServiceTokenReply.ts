import {AbstractServiceReply} from "strata.foundation.core/Transfer";

export
interface IssueServiceTokenReply
    extends AbstractServiceReply
{
    token?: string;
    expiresAt?: string;
}
