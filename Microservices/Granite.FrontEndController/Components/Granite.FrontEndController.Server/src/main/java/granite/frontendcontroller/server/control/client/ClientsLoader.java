//////////////////////////////////////////////////////////////////////////////
// ClientsLoader.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.client;

import granite.client.service.requestreply.FindAllClientsRequest;
import granite.client.service.requestreply.FindClientsByOwnerRequest;
import granite.client.service.requestreply.FindClientsReply;
import granite.foundation.core.security.ApplicationRolesManager;
import granite.frontendcontroller.server.application.GraniteClientServiceClients;
import granite.frontendcontroller.server.control.user.RoleCatalogData;
import granite.frontendcontroller.service.requestreply.ClientOwnerData;
import granite.frontendcontroller.service.requestreply.ClientsOutput;
import granite.frontendcontroller.service.requestreply.RoleData;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindAllUsersReply;
import granite.user.service.requestreply.FindAllUsersRequest;
import granite.user.service.requestreply.UserData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public
class ClientsLoader
{
    private final GraniteClientServiceClients clients;
    private final IUserQueryServiceClient     userQueryClient;
    private final ApplicationRolesManager     applicationRoles;

    public
    ClientsLoader(
        GraniteClientServiceClients clients,
        IUserQueryServiceClient     userQueryClient,
        ApplicationRolesManager     applicationRoles)
    {
        this.clients          = clients;
        this.userQueryClient  = userQueryClient;
        this.applicationRoles = applicationRoles;
    }

    public CompletionStage<ClientsOutput>
    loadAll(List<String> failures)
    {
        return
            clients
                .getClientQueryClient()
                .findAllClients(new FindAllClientsRequest())
                .thenCompose(
                    clientsReply ->
                        userQueryClient
                            .findAllUsers(new FindAllUsersRequest())
                            .thenApply(usersReply -> toOutput(clientsReply,usersReply,failures)));
    }

    public CompletionStage<ClientsOutput>
    loadOwned(Long ownerUserId,List<String> failures)
    {
        return
            clients
                .getClientQueryClient()
                .findClientsByOwner(new FindClientsByOwnerRequest().setUserId(ownerUserId))
                .thenApply(reply -> toOutput(reply,null,failures));
    }

    protected ClientsOutput
    toOutput(FindClientsReply clientsReply,FindAllUsersReply usersReply,List<String> failures)
    {
        ClientsOutput output = new ClientsOutput().setFailureMessages(failures).setRoles(roles());

        if (clientsReply.isSuccess() && clientsReply.getClients() != null)
            output.setClients(clientsReply.getClients());
        else
            failures.add("Unable to refresh client list: " + reason(clientsReply.getFailureMessage()));

        if (usersReply != null)
        {
            if (usersReply.isSuccess() && usersReply.getUsers() != null)
                output.setUsers(owners(usersReply.getUsers()));
            else
                failures.add("Unable to read users: " + reason(usersReply.getFailureMessage()));
        }

        return output;
    }

    protected List<RoleData>
    roles()
    {
        List<RoleData> roles = new ArrayList<>();

        for (RoleData role : RoleCatalogData.from(applicationRoles))
            if (!ApplicationRolesManager.ADMIN.equals(role.getName()))
                roles.add(role);

        return roles;
    }

    private List<ClientOwnerData>
    owners(List<UserData> users)
    {
        List<ClientOwnerData> owners = new ArrayList<>();

        for (UserData user : users)
            owners.add(new ClientOwnerData().setUserId(user.getUserId()).setUserName(user.getUserName()));

        return owners;
    }

    private String
    reason(String message)
    {
        return message == null || message.isBlank() ? "unknown error" : message;
    }
}

//////////////////////////////////////////////////////////////////////////////
