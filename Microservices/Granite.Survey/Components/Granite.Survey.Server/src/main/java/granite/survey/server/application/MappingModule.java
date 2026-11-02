//////////////////////////////////////////////////////////////////////////////
// MappingModule.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.server.application;

import granite.survey.server.domain.SurveyResponse;
import granite.survey.service.requestreply.SurveyResponseData;
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
        Converter<SurveyResponse,SurveyResponseData> toData =
            context ->
            {
                SurveyResponse source = context.getSource();

                return
                    source == null
                        ? null
                        : new SurveyResponseData()
                            .setResponseId(source.getPrimaryId())
                            .setUserId(source.getUserId())
                            .setAnswers(new LinkedHashMap<>(source.getAnswers()))
                            .setComment(source.getComment())
                            .setStatus(source.getStatus())
                            .setNotes(source.getNotes())
                            .setCreated(source.getSubmitted())
                            .setUpdated(source.getChanged())
                            .setSent(source.getSent())
                            .setReceived(source.getReceived());
            };

        mapper
            .emptyTypeMap(SurveyResponse.class,SurveyResponseData.class)
            .setConverter(toData);
    }
}

//////////////////////////////////////////////////////////////////////////////
