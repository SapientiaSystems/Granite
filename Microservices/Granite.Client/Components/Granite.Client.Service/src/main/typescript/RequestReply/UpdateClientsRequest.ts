import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";
import type { UpdateClientData } from "./UpdateClientData";

export
interface UpdateClientsRequest
    extends AbstractServiceRequest
{
    clients?: Array<UpdateClientData>;
}
