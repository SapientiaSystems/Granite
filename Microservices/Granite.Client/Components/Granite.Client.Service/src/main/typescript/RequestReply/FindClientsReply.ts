import type { AbstractServiceReply } from "strata.foundation.core/Transfer";
import type { ClientData } from "./ClientData";

export
interface FindClientsReply
    extends AbstractServiceReply
{
    clients?: Array<ClientData>;
}
