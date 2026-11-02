//////////////////////////////////////////////////////////////////////////////
// GraniteSurveyQuestions.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.service.requestreply;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public
class GraniteSurveyQuestions
{
    public static final int    MIN_ANSWER         = 1;
    public static final int    MAX_ANSWER         = 5;
    public static final int    MAX_COMMENT_LENGTH = 2000;
    public static final String SATISFIED          = "satisfied";
    public static final String NEEDS              = "needs";
    public static final String RECOMMEND          = "recommend";

    private
    GraniteSurveyQuestions() {}

    public static List<SurveyQuestionData>
    questions()
    {
        return
            List.of(
                question(
                    SATISFIED,
                    "Overall, how satisfied are you with Granite?",
                    "Very dissatisfied","Dissatisfied","Neutral","Satisfied","Very satisfied"),
                question(
                    NEEDS,
                    "How well does Granite meet your day-to-day needs?",
                    "Not at all","Slightly","Moderately","Very well","Extremely well"),
                question(
                    RECOMMEND,
                    "How likely are you to recommend Granite to a colleague?",
                    "Not at all likely","Unlikely","Neutral","Likely","Extremely likely"));
    }

    public static List<String>
    keys()
    {
        return List.of(SATISFIED,NEEDS,RECOMMEND);
    }

    private static SurveyQuestionData
    question(String key,String label,String... scale)
    {
        Map<Integer,String> labels = new LinkedHashMap<>();

        for (int i = 0; i < scale.length; i++)
            labels.put(MIN_ANSWER + i,scale[i]);

        return new SurveyQuestionData().setKey(key).setLabel(label).setLabels(labels);
    }
}

//////////////////////////////////////////////////////////////////////////////
