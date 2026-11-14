import type { AbstractServiceReply } from "strata.foundation.core/Transfer";
import type { ClientData } from "./ClientData";

export
interface UpdateClientsReply
    extends AbstractServiceReply
{
    clients?: Array<ClientData>;
}
