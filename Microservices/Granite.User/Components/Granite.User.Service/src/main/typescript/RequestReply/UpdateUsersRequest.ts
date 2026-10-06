import {AbstractServiceRequest} from "strata.foundation.core/Transfer/";
import {UpdateUserData} from "./UpdateUserData";

export
interface UpdateUsersRequest
    extends AbstractServiceRequest
{
    usersToUpdate: Array<UpdateUserData>;
}

