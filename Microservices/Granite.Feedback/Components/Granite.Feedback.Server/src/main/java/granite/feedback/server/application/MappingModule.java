//////////////////////////////////////////////////////////////////////////////
// MappingModule.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.server.application;

import granite.feedback.server.domain.Feedback;
import granite.feedback.service.requestreply.FeedbackData;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.Module;

import java.util.LinkedHashMap;

public
class MappingModule
    implements Module
{
    @Override
    public void
    setupModule(ModelMapper mapper)
    {
        Converter<Feedback,FeedbackData> toData =
            context ->
            {
                Feedback source = context.getSource();

                return
                    source == null
                        ? null
                        : new FeedbackData()
                            .setFeedbackId(source.getPrimaryId())
                            .setUserId(source.getUserId())
                            .setType(source.getType())
                            .setSummary(source.getSummary())
                            .setDetails(new LinkedHashMap<>(source.getDetails()))
                            .setStatus(source.getStatus())
                            .setNotes(source.getNotes())
                            .setCreated(source.getSubmitted())
                            .setUpdated(source.getChanged())
                            .setSent(source.getSent())
                            .setReceived(source.getReceived());
            };

        mapper
            .emptyTypeMap(Feedback.class,FeedbackData.class)
            .setConverter(toData);
    }
}

//////////////////////////////////////////////////////////////////////////////
