//////////////////////////////////////////////////////////////////////////////
// MappingModule.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.application;

import granite.content.server.domain.ContentDocument;
import granite.content.service.requestreply.ContentDocumentData;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.Module;

import java.util.ArrayList;
import java.util.TreeSet;

public
class MappingModule
    implements Module
{
    @Override
    public void
    setupModule(ModelMapper mapper)
    {
        Converter<ContentDocument,ContentDocumentData> toData =
            context ->
            {
                ContentDocument source = context.getSource();

                return
                    source == null
                        ? null
                        : new ContentDocumentData()
                            .setDocumentId(source.getPrimaryId())
                            .setKey(source.getDocumentKey())
                            .setTitle(source.getTitle())
                            .setKind(source.getKind())
                            .setFormat(source.getFormat())
                            .setBody(source.getBody())
                            .setKeywords(new ArrayList<>(source.getKeywords()))
                            .setRequiredAuthorities(new TreeSet<>(source.getRequiredAuthorities()))
                            .setSource(source.getSource())
                            .setVersion(source.getContentVersion())
                            .setUpdated(source.getChanged());
            };

        mapper
            .emptyTypeMap(ContentDocument.class,ContentDocumentData.class)
            .setConverter(toData);
    }
}

//////////////////////////////////////////////////////////////////////////////
