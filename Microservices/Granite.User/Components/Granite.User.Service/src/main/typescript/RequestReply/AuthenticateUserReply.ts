import {AbstractServiceReply} from "strata.foundation.core/Transfer";

export
interface AuthenticateUserReply
    extends AbstractServiceReply
{
    valid: boolean;
    userId?: number;
}
