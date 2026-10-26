//////////////////////////////////////////////////////////////////////////////
// LogoutView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.platform.SessionCookies;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.SessionInput;
import granite.frontendcontroller.service.requestreply.SessionOutput;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.service.requestreply.LogoutReply;
import granite.session.service.requestreply.LogoutRequest;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class LogoutView
    implements IInputAction<SessionInput,SessionOutput>
{
    private final ISessionCommandServiceClient commandClient;
    private final SessionRequestContext        context;

    public
    LogoutView(ISessionCommandServiceClient commandClient,SessionRequestContext context)
    {
        this.commandClient = commandClient;
        this.context = context;
    }

    @Override
    public CompletionStage<SessionOutput>
    apply(SessionInput input)
    {
        Optional<Long> sessionId = context.getSessionId();

        if (sessionId.isEmpty())
            return CompletableFuture.completedFuture(clearAndDescribe(input,null));

        return
            commandClient
                .logout(new LogoutRequest().setSessionId(sessionId.get()))
                .thenApply(reply -> toOutput(input,reply))
                .exceptionally(e -> describe(input,false,null));
    }

    protected SessionOutput
    toOutput(SessionInput input,LogoutReply reply)
    {
        return reply.isSuccess() ? clearAndDescribe(input,reply.getEndSessionUrl()) : describe(input,false,null);
    }

    private SessionOutput
    clearAndDescribe(SessionInput input,String endSessionUrl)
    {
        context.addSetCookie(SessionCookies.clear());
        return describe(input,true,endSessionUrl);
    }

    private SessionOutput
    describe(SessionInput input,boolean loggedOut,String endSessionUrl)
    {
        return
            new SessionOutput()
                .setLoggedOut(loggedOut)
                .setEndSessionUrl(endSessionUrl)
                .setUserId(input.getUserId())
                .setSource(input.getSource())
                .setCommand(input.getCommand())
                .setDestination(input.getDestination().orElse(null));
    }
}

//////////////////////////////////////////////////////////////////////////////
