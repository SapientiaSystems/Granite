import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";
import type { CreateFeedbackData } from "./CreateFeedbackData";

export
interface CreateFeedbackRequest
    extends AbstractServiceRequest
{
    feedback?: CreateFeedbackData;
}
