//////////////////////////////////////////////////////////////////////////////
// UnauthenticatedSurveyOutputs.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.survey;

import granite.frontendcontroller.service.requestreply.SurveyOutput;
import granite.frontendcontroller.service.requestreply.SurveyReviewOutput;

import java.util.ArrayList;
import java.util.List;

public
class UnauthenticatedSurveyOutputs
{
    public static final String MESSAGE = "not authenticated";

    private
    UnauthenticatedSurveyOutputs() {}

    public static SurveyOutput
    survey()
    {
        return new SurveyOutput().setFailureMessages(new ArrayList<>(List.of(MESSAGE)));
    }

    public static SurveyReviewOutput
    review()
    {
        return new SurveyReviewOutput().setFailureMessages(new ArrayList<>(List.of(MESSAGE)));
    }
}

//////////////////////////////////////////////////////////////////////////////
