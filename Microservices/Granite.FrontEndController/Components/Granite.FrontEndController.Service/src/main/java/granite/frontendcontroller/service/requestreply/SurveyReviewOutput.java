//////////////////////////////////////////////////////////////////////////////
// SurveyReviewOutput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import granite.survey.service.requestreply.GraniteSurveyQuestions;
import granite.survey.service.requestreply.SurveyQuestionData;
import granite.survey.service.requestreply.SurveyResponseData;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public
class SurveyReviewOutput
    extends FrontEndOutput
{
    private List<SurveyResponseData> responses;
    private List<SurveyQuestionData> questions;
    private Map<String,Double>       averages;
    private Map<Long,String>         userNames;
    private List<String>             failureMessages;

    public
    SurveyReviewOutput()
    {
        super();
        responses       = new ArrayList<>();
        questions       = new ArrayList<>(GraniteSurveyQuestions.questions());
        averages        = new LinkedHashMap<>();
        userNames       = new LinkedHashMap<>();
        failureMessages = new ArrayList<>();
    }

    @Override
    public SurveyReviewOutput
    setUserId(Long id)
    {
        return (SurveyReviewOutput)super.setUserId(id);
    }

    @Override
    public SurveyReviewOutput
    setSource(String src)
    {
        return (SurveyReviewOutput)super.setSource(src);
    }

    @Override
    public SurveyReviewOutput
    setDestination(String dest)
    {
        return (SurveyReviewOutput)super.setDestination(dest);
    }

    @Override
    public SurveyReviewOutput
    setCommand(String cmd)
    {
        return (SurveyReviewOutput)super.setCommand(cmd);
    }

    public SurveyReviewOutput
    setResponses(List<SurveyResponseData> value)
    {
        responses = value;
        return this;
    }

    public SurveyReviewOutput
    setQuestions(List<SurveyQuestionData> value)
    {
        questions = value;
        return this;
    }

    public SurveyReviewOutput
    setAverages(Map<String,Double> value)
    {
        averages = value;
        return this;
    }

    public SurveyReviewOutput
    setUserNames(Map<Long,String> value)
    {
        userNames = value;
        return this;
    }

    public SurveyReviewOutput
    setFailureMessages(List<String> value)
    {
        failureMessages = value;
        return this;
    }

    public List<SurveyResponseData>
    getResponses() { return responses; }

    public List<SurveyQuestionData>
    getQuestions() { return questions; }

    public Map<String,Double>
    getAverages() { return averages; }

    public Map<Long,String>
    getUserNames() { return userNames; }

    public List<String>
    getFailureMessages() { return failureMessages; }
}

//////////////////////////////////////////////////////////////////////////////
