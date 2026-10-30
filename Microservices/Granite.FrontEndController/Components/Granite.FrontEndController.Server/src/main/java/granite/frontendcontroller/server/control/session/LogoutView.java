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
    private final SessionCookies               cookies;

    public
    LogoutView(ISessionCommandServiceClient commandClient,SessionRequestContext context,SessionCookies cookies)
    {
        this.commandClient = commandClient;
        this.context = context;
        this.cookies = cookies;
    }

    @Override
    public CompletionStage<SessionOutput>
    apply(SessionInput input)
    {
        Optional<Long> sessionId = context.getSessionId();

        if (sessionId.isEmpty())
            return CompletableFuture.completedFuture(clearAndDescribe(input));

        return
            commandClient
                .logout(new LogoutRequest().setSessionId(sessionId.get()))
                .thenApply(reply -> toOutput(input,reply))
                .exceptionally(e -> describe(input,false));
    }

    protected SessionOutput
    toOutput(SessionInput input,LogoutReply reply)
    {
        return reply.isSuccess() ? clearAndDescribe(input) : describe(input,false);
    }

    private SessionOutput
    clearAndDescribe(SessionInput input)
    {
        context.addSetCookie(cookies.clear());
        return describe(input,true);
    }

    private SessionOutput
    describe(SessionInput input,boolean loggedOut)
    {
        return
            new SessionOutput()
                .setLoggedOut(loggedOut)
                .setUserId(input.getUserId())
                .setSource(input.getSource())
                .setCommand(input.getCommand())
                .setDestination(input.getDestination().orElse(null));
    }
}

//////////////////////////////////////////////////////////////////////////////
