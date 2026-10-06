import {AbstractServiceReply} from "strata.foundation.core/Transfer";
import {UserData} from "../Unused";

export
interface CreateUserReply
    extends AbstractServiceReply
{
    createdUser: UserData;
}