//////////////////////////////////////////////////////////////////////////////
// KafkaAvroSurveyEventSender.java
//////////////////////////////////////////////////////////////////////////////

package granite.outbox.server.survey;

import granite.survey.service.event.ISurveyEventSender;
import granite.survey.service.event.SurveyEvent;
import strata.foundation.kafka.event.AbstractKafkaAvroEventSender;
import strata.foundation.kafka.event.IKafkaConfigurationProvider;

public
class KafkaAvroSurveyEventSender
    extends AbstractKafkaAvroEventSender<SurveyEvent>
    implements ISurveyEventSender
{
    public
    KafkaAvroSurveyEventSender(IKafkaConfigurationProvider provider,String topic)
    {
        super(
            provider.get(),
            e -> e.getSourceId().toString(),
            SurveyEvent.class,
            topic);
    }

}

//////////////////////////////////////////////////////////////////////////////
