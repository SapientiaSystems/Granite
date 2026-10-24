import {AbstractServiceReply} from "strata.foundation.core/Transfer";

export
interface StartOktaLoginReply
    extends AbstractServiceReply
{
    authorizeUrl?: string;
    transactionId?: string;
}
