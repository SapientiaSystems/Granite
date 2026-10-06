import {AbstractServiceReply} from "strata.foundation.core/Transfer";
import {UserData} from "../Unused";

export
interface DestroyUserReply
    extends AbstractServiceReply
{
    destroyedUser: UserData;
}