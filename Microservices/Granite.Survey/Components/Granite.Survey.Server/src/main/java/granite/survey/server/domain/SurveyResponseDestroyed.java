//////////////////////////////////////////////////////////////////////////////
// SurveyResponseDestroyed.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.server.domain;

public
class SurveyResponseDestroyed
    extends AbstractSurveyResponseEvent
{
    public
    SurveyResponseDestroyed(SurveyResponse src)
    {
        super("SurveyResponseDestroyed",src);
    }

    public
    SurveyResponseDestroyed(String correlId,SurveyResponse src)
    {
        super("SurveyResponseDestroyed",correlId,src);
    }
}

//////////////////////////////////////////////////////////////////////////////
