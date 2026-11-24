import type { AbstractServiceReply } from "strata.foundation.core/Transfer";

export
interface DestroyClientsReply
    extends AbstractServiceReply
{
    clientIds?: Array<string>;
}
