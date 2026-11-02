//////////////////////////////////////////////////////////////////////////////
// OutboxEventSurveyEventSender.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.server.platform;

import granite.survey.service.event.ISurveyEventSender;
import granite.survey.service.event.SurveyEvent;
import strata.outbox.core.repository.IOutboxEventRepository;
import strata.outbox.core.sender.OutboxEventServiceEventSender;

public
class OutboxEventSurveyEventSender
    extends OutboxEventServiceEventSender<SurveyEvent,ISurveyEventSender>
    implements ISurveyEventSender
{
    public
    OutboxEventSurveyEventSender(IOutboxEventRepository repository)
    {
        super(new SurveyEventToOutboxEventMapper(),repository);
    }

    @Override
    protected ISurveyEventSender
    getSelf()
    {
        return this;
    }
}

//////////////////////////////////////////////////////////////////////////////
