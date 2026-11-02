//////////////////////////////////////////////////////////////////////////////
// SurveyResponseUpdated.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.server.domain;

public
class SurveyResponseUpdated
    extends AbstractSurveyResponseEvent
{
    public
    SurveyResponseUpdated(SurveyResponse src)
    {
        super("SurveyResponseUpdated",src);
    }

    public
    SurveyResponseUpdated(String correlId,SurveyResponse src)
    {
        super("SurveyResponseUpdated",correlId,src);
    }
}

//////////////////////////////////////////////////////////////////////////////
