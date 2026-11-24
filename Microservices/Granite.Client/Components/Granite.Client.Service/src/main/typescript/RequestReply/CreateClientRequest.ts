import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";
import type { CreateClientData } from "./CreateClientData";

export
interface CreateClientRequest
    extends AbstractServiceRequest
{
    client?: CreateClientData;
}
