//////////////////////////////////////////////////////////////////////////////
// SurveyResponseCreated.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.server.domain;

public
class SurveyResponseCreated
    extends AbstractSurveyResponseEvent
{
    public
    SurveyResponseCreated(SurveyResponse src)
    {
        super("SurveyResponseCreated",src);
    }

    public
    SurveyResponseCreated(String correlId,SurveyResponse src)
    {
        super("SurveyResponseCreated",correlId,src);
    }
}

//////////////////////////////////////////////////////////////////////////////
