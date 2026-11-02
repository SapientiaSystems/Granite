//////////////////////////////////////////////////////////////////////////////
// AbstractSurveyResponseEvent.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.server.domain;

import strata.server.core.domainevent.AbstractDomainEvent;

public abstract
class AbstractSurveyResponseEvent
    extends AbstractDomainEvent<SurveyResponse>
    implements ISurveyResponseEvent
{
    protected
    AbstractSurveyResponseEvent(String nm,SurveyResponse src)
    {
        super(nm,src);
    }

    protected
    AbstractSurveyResponseEvent(String nm,String correlId,SurveyResponse src)
    {
        super(nm,correlId,src);
    }
}

//////////////////////////////////////////////////////////////////////////////
