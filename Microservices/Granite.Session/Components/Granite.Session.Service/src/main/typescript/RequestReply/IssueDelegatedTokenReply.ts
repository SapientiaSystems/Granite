import {AbstractServiceReply} from "strata.foundation.core/Transfer";

export
interface IssueDelegatedTokenReply
    extends AbstractServiceReply
{
    token?: string;
    expiresAt?: string;
}
