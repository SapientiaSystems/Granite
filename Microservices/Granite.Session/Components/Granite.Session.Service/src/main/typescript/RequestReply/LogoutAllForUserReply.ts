import {AbstractServiceReply} from "strata.foundation.core/Transfer";

export
interface LogoutAllForUserReply
    extends AbstractServiceReply
{
    destroyed?: Array<number>;
}
