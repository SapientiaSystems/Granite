//////////////////////////////////////////////////////////////////////////////
// ClientsInput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import granite.client.service.requestreply.CreateClientData;
import granite.client.service.requestreply.UpdateClientData;
import strata.frontendcontroller.service.requestreply.FrontEndInput;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public
class ClientsInput
    extends FrontEndInput
{
    private List<CreateClientData> clientsToCreate;
    private List<UpdateClientData> clientsToUpdate;
    private List<String>           clientIdsToDelete;
    private String                 clientIdToRotate;
    private Instant                secretExpiresAt;

    public
    ClientsInput()
    {
        super();
        clientsToCreate   = new ArrayList<>();
        clientsToUpdate   = new ArrayList<>();
        clientIdsToDelete = new ArrayList<>();
        clientIdToRotate  = null;
        secretExpiresAt   = null;
    }

    @Override
    public ClientsInput
    setUserId(Long id)
    {
        return (ClientsInput)super.setUserId(id);
    }

    @Override
    public ClientsInput
    setSource(String src)
    {
        return (ClientsInput)super.setSource(src);
    }

    @Override
    public ClientsInput
    setDestination(String dest)
    {
        return (ClientsInput)super.setDestination(dest);
    }

    @Override
    public ClientsInput
    setCommand(String cmd)
    {
        return (ClientsInput)super.setCommand(cmd);
    }

    public ClientsInput
    setClientsToCreate(List<CreateClientData> clients)
    {
        clientsToCreate = clients;
        return this;
    }

    public ClientsInput
    setClientsToUpdate(List<UpdateClientData> clients)
    {
        clientsToUpdate = clients;
        return this;
    }

    public ClientsInput
    setClientIdsToDelete(List<String> ids)
    {
        clientIdsToDelete = ids;
        return this;
    }

    public ClientsInput
    setClientIdToRotate(String id)
    {
        clientIdToRotate = id;
        return this;
    }

    public ClientsInput
    setSecretExpiresAt(Instant expiresAt)
    {
        secretExpiresAt = expiresAt;
        return this;
    }

    public List<CreateClientData>
    getClientsToCreate() { return clientsToCreate; }

    public List<UpdateClientData>
    getClientsToUpdate() { return clientsToUpdate; }

    public List<String>
    getClientIdsToDelete() { return clientIdsToDelete; }

    public String
    getClientIdToRotate() { return clientIdToRotate; }

    public Instant
    getSecretExpiresAt() { return secretExpiresAt; }

    @Override
    public String
    toString()
    {
        return
            "ClientsInput[source=" + getSource()
                + ", command=" + getCommand()
                + ", creates=" + (clientsToCreate == null ? 0 : clientsToCreate.size())
                + ", updates=" + (clientsToUpdate == null ? 0 : clientsToUpdate.size())
                + ", deletes=" + (clientIdsToDelete == null ? 0 : clientIdsToDelete.size()) + "]";
    }
}

//////////////////////////////////////////////////////////////////////////////
