import {AbstractServiceReply} from "strata.foundation.core/Transfer";

export
interface LogoutReply
    extends AbstractServiceReply
{
    endSessionUrl?: string;
}
