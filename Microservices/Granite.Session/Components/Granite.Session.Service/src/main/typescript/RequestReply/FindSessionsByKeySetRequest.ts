import {AbstractServiceRequest} from "strata.foundation.core/Transfer";
import {SessionKeySet} from "./SessionKeySet";

export
interface FindSessionsByKeySetRequest
    extends AbstractServiceRequest
{
    keySet?: SessionKeySet;
}
