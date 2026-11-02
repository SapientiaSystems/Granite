//////////////////////////////////////////////////////////////////////////////
// SurveyOutput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import granite.survey.service.requestreply.GraniteSurveyQuestions;
import granite.survey.service.requestreply.SurveyQuestionData;
import granite.survey.service.requestreply.SurveyResponseData;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;
import java.util.ArrayList;
import java.util.List;

public
class SurveyOutput
    extends FrontEndOutput
{
    private List<SurveyQuestionData> questions;
    private SurveyResponseData       submitted;
    private List<String>             failureMessages;

    public
    SurveyOutput()
    {
        super();
        questions       = new ArrayList<>(GraniteSurveyQuestions.questions());
        submitted       = null;
        failureMessages = new ArrayList<>();
    }

    @Override
    public SurveyOutput
    setUserId(Long id)
    {
        return (SurveyOutput)super.setUserId(id);
    }

    @Override
    public SurveyOutput
    setSource(String src)
    {
        return (SurveyOutput)super.setSource(src);
    }

    @Override
    public SurveyOutput
    setDestination(String dest)
    {
        return (SurveyOutput)super.setDestination(dest);
    }

    @Override
    public SurveyOutput
    setCommand(String cmd)
    {
        return (SurveyOutput)super.setCommand(cmd);
    }

    public SurveyOutput
    setQuestions(List<SurveyQuestionData> value)
    {
        questions = value;
        return this;
    }

    public SurveyOutput
    setSubmitted(SurveyResponseData value)
    {
        submitted = value;
        return this;
    }

    public SurveyOutput
    setFailureMessages(List<String> value)
    {
        failureMessages = value;
        return this;
    }

    public List<SurveyQuestionData>
    getQuestions() { return questions; }

    public SurveyResponseData
    getSubmitted() { return submitted; }

    public List<String>
    getFailureMessages() { return failureMessages; }
}

//////////////////////////////////////////////////////////////////////////////
