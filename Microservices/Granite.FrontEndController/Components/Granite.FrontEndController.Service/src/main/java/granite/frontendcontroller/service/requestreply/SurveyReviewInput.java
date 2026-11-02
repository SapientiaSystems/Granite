//////////////////////////////////////////////////////////////////////////////
// SurveyReviewInput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import granite.survey.service.requestreply.UpdateSurveyResponseData;
import strata.frontendcontroller.service.requestreply.FrontEndInput;
import java.util.ArrayList;
import java.util.List;

public
class SurveyReviewInput
    extends FrontEndInput
{
    private List<UpdateSurveyResponseData> changes;

    public
    SurveyReviewInput()
    {
        super();
        changes = new ArrayList<>();
    }

    @Override
    public SurveyReviewInput
    setUserId(Long id)
    {
        return (SurveyReviewInput)super.setUserId(id);
    }

    @Override
    public SurveyReviewInput
    setSource(String src)
    {
        return (SurveyReviewInput)super.setSource(src);
    }

    @Override
    public SurveyReviewInput
    setDestination(String dest)
    {
        return (SurveyReviewInput)super.setDestination(dest);
    }

    @Override
    public SurveyReviewInput
    setCommand(String cmd)
    {
        return (SurveyReviewInput)super.setCommand(cmd);
    }

    public SurveyReviewInput
    setChanges(List<UpdateSurveyResponseData> value)
    {
        changes = value;
        return this;
    }

    public List<UpdateSurveyResponseData>
    getChanges() { return changes; }
}

//////////////////////////////////////////////////////////////////////////////
