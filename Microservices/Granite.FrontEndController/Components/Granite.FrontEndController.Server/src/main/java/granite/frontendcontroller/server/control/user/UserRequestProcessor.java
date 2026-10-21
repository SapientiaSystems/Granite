//////////////////////////////////////////////////////////////////////////////
// UserRequestProcessor.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.user;

import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import strata.frontendcontroller.server.application.DefaultRequestProcessor;
import strata.frontendcontroller.server.application.IProcessAction;

import java.util.Map;

public
class UserRequestProcessor
    extends DefaultRequestProcessor
{
    public
    UserRequestProcessor(
        IUserQueryServiceClient   queryClient,
        IUserCommandServiceClient commandClient)
    {
        super(
            Map.<String,IProcessAction>of(
                "User.All->UsersEditableView",new InitializeUsersView(queryClient),
                "User.All->Save->UsersEditableView",new UpdateUsersView(queryClient,commandClient),
                "User.All->Delete->UsersEditableView",new DeleteUsersView(queryClient,commandClient)));
    }
}

//////////////////////////////////////////////////////////////////////////////
