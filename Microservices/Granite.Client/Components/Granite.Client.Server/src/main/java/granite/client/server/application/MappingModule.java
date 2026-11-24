//////////////////////////////////////////////////////////////////////////////
// MappingModule.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.application;

import granite.client.server.domain.Client;
import granite.client.service.requestreply.ClientData;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.Module;

import java.util.TreeSet;

public
class MappingModule
    implements Module
{
    @Override
    public void
    setupModule(ModelMapper mapper)
    {
        Converter<Client,ClientData> toData =
            context ->
            {
                Client source = context.getSource();

                return
                    source == null
                        ? null
                        : new ClientData()
                            .setClientId(source.getClientId())
                            .setName(source.getName())
                            .setDescription(source.getDescription())
                            .setAuthorities(new TreeSet<>(source.getAuthorities()))
                            .setOwners(new TreeSet<>(source.getOwners()))
                            .setEnabled(source.isEnabled())
                            .setSecretExpiresAt(source.getSecretExpiresAt())
                            .setLastTokenIssuedAt(source.getLastTokenIssuedAt())
                            .setCreatedAt(source.getCreated())
                            .setUpdatedAt(source.getLastModified())
                            .setVersion(
                                source.getVersion() == null
                                    ? null
                                    : source.getVersion().longValue());
            };

        mapper
            .emptyTypeMap(Client.class,ClientData.class)
            .setConverter(toData);
    }
}

//////////////////////////////////////////////////////////////////////////////
