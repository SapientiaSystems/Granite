//////////////////////////////////////////////////////////////////////////////
// ClientsOutput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import granite.client.service.requestreply.ClientData;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;

import java.util.ArrayList;
import java.util.List;

public
class ClientsOutput
    extends FrontEndOutput
{
    private List<ClientData>      clients;
    private List<RoleData>        roles;
    private List<ClientOwnerData> users;
    private String                revealedClientId;
    private String                revealedClientSecret;
    private List<String>          failureMessages;

    public
    ClientsOutput()
    {
        super();
        clients              = new ArrayList<>();
        roles                = new ArrayList<>();
        users                = new ArrayList<>();
        revealedClientId     = null;
        revealedClientSecret = null;
        failureMessages      = new ArrayList<>();
    }

    @Override
    public ClientsOutput
    setUserId(Long id)
    {
        return (ClientsOutput)super.setUserId(id);
    }

    @Override
    public ClientsOutput
    setSource(String src)
    {
        return (ClientsOutput)super.setSource(src);
    }

    @Override
    public ClientsOutput
    setDestination(String dest)
    {
        return (ClientsOutput)super.setDestination(dest);
    }

    @Override
    public ClientsOutput
    setCommand(String cmd)
    {
        return (ClientsOutput)super.setCommand(cmd);
    }

    public ClientsOutput
    setClients(List<ClientData> clients)
    {
        this.clients = clients;
        return this;
    }

    public ClientsOutput
    setRoles(List<RoleData> roles)
    {
        this.roles = roles;
        return this;
    }

    public ClientsOutput
    setUsers(List<ClientOwnerData> users)
    {
        this.users = users;
        return this;
    }

    public ClientsOutput
    setRevealedClientId(String id)
    {
        revealedClientId = id;
        return this;
    }

    public ClientsOutput
    setRevealedClientSecret(String secret)
    {
        revealedClientSecret = secret;
        return this;
    }

    public ClientsOutput
    setFailureMessages(List<String> messages)
    {
        failureMessages = messages;
        return this;
    }

    public List<ClientData>
    getClients() { return clients; }

    public List<RoleData>
    getRoles() { return roles; }

    public List<ClientOwnerData>
    getUsers() { return users; }

    public String
    getRevealedClientId() { return revealedClientId; }

    public String
    getRevealedClientSecret() { return revealedClientSecret; }

    public List<String>
    getFailureMessages() { return failureMessages; }

    @Override
    public String
    toString()
    {
        return
            "ClientsOutput[source=" + getSource()
                + ", command=" + getCommand()
                + ", clients=" + (clients == null ? 0 : clients.size())
                + ", revealedClientId=" + revealedClientId + "]";
    }
}

//////////////////////////////////////////////////////////////////////////////
