//////////////////////////////////////////////////////////////////////////////
// SurveyEventOutboxEventReceiver.java
//////////////////////////////////////////////////////////////////////////////

package granite.outbox.server.survey;

import granite.survey.service.event.ISurveyEventSender;
import granite.survey.service.event.SurveyEvent;
import strata.outbox.core.receiver.ServiceEventOutboxEventReceiver;
import strata.outbox.core.sender.SendException;

public
class SurveyEventOutboxEventReceiver
    extends ServiceEventOutboxEventReceiver<SurveyEvent,ISurveyEventSender>
{
    public
    SurveyEventOutboxEventReceiver(ISurveyEventSender sender)
        throws SendException
    {
        super(SurveyEvent.class,sender);
    }
}

//////////////////////////////////////////////////////////////////////////////
