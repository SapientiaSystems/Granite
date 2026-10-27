import {AbstractServiceReply} from "strata.foundation.core/Transfer";
import {UserData} from "../Unused";

export
interface MergeUserValuesReply
    extends AbstractServiceReply
{
    mergedUser: UserData;
}
