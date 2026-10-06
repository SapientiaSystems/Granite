import {AbstractServiceRequest} from "strata.foundation.core/Transfer/";
import {UpdateUserData} from "./UpdateUserData";

export
interface UpdateUserRequest
    extends AbstractServiceRequest
{
    user: UpdateUserData;
}