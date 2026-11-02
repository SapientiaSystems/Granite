import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";
import type { UpdateFeedbackData } from "./UpdateFeedbackData";

export
interface UpdateFeedbacksRequest
    extends AbstractServiceRequest
{
    feedback?: Array<UpdateFeedbackData>;
}
