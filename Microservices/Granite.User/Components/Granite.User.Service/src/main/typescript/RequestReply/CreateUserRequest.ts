import {AbstractServiceRequest} from "strata.foundation.core/Transfer/";
import {CreateUserData} from "./CreateUserData";

export
interface CreateUserRequest
    extends AbstractServiceRequest
{
    user: CreateUserData;
}