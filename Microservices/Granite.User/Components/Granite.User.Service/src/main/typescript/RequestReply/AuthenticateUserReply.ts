import {AbstractServiceReply} from "strata.foundation.core/Transfer";
import {UserData}             from "./UserData";

export
interface AuthenticateUserReply
    extends AbstractServiceReply
{
    valid: boolean;
    user?: UserData;
}
