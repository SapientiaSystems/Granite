import {AbstractServiceRequest} from "strata.foundation.core/Transfer";
import {SessionKey} from "./SessionKey";

export
interface FindSessionsByKeyRequest
    extends AbstractServiceRequest
{
    key?: SessionKey;
    returnIfFound?: boolean;
}
