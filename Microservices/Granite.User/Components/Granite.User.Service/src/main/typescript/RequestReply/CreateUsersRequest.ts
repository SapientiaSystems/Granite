import {AbstractServiceRequest} from "strata.foundation.core/Transfer/";
import {CreateUserData} from "./CreateUserData";

export
interface CreateUsersRequest
    extends AbstractServiceRequest
{
    usersToCreate: Array<CreateUserData>;
}

