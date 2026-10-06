import {AbstractServiceReply} from "strata.foundation.core/Transfer";
import {UserData} from "../Unused";

export
interface UpdateUserReply
    extends AbstractServiceReply
{
    updatedUser: UserData;
}