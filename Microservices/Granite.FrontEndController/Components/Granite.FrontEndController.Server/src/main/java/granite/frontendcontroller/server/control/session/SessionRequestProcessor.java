//////////////////////////////////////////////////////////////////////////////
// SessionRequestProcessor.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import strata.frontendcontroller.server.application.DefaultRequestProcessor;
import strata.frontendcontroller.server.application.IProcessAction;

import java.util.Map;

public
class SessionRequestProcessor
    extends DefaultRequestProcessor
{
    public
    SessionRequestProcessor(
        ISessionCommandServiceClient commandClient,
        ISessionQueryServiceClient   queryClient,
        IUserQueryServiceClient      userQueryClient,
        SessionRequestContext        context,
        SessionLoginSettings         settings)
    {
        super(
            Map.<String,IProcessAction>of(
                "Session.New->LoginView",new LoginMethodsView(settings),
                "Session.New->Login->LoginView",new LoginView(commandClient,context,settings),
                "Session.Current->SessionView",new SessionStatusView(queryClient,userQueryClient,context),
                "Session.Current->Logout->LoginView",new LogoutView(commandClient,context)));
    }
}

//////////////////////////////////////////////////////////////////////////////
