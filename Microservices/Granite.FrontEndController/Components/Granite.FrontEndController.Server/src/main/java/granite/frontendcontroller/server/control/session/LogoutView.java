//////////////////////////////////////////////////////////////////////////////
// LogoutView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.frontendcontroller.server.platform.SessionCookies;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.LogoutInput;
import granite.frontendcontroller.service.requestreply.LogoutOutput;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.service.requestreply.LogoutReply;
import granite.session.service.requestreply.LogoutRequest;
import strata.frontendcontroller.server.application.IHandleInputAction;
import strata.frontendcontroller.service.requestreply.FrontEndInputMapper;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class LogoutView
    implements IHandleInputAction
{
    private final ISessionCommandServiceClient commandClient;
    private final SessionRequestContext        context;
    private final FrontEndInputMapper          mapper;

    public
    LogoutView(ISessionCommandServiceClient commandClient,SessionRequestContext context)
    {
        this.commandClient = commandClient;
        this.context = context;
        this.mapper = FrontEndInputMapper.of();
    }

    @Override
    public CompletionStage<FrontEndOutput>
    apply(HandleInputRequest request)
    {
        return
            mapper
                .map(LogoutInput.class,request.getInput())
                .map(this::logout)
                .orElseThrow();
    }

    protected CompletionStage<FrontEndOutput>
    logout(LogoutInput input)
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

    protected FrontEndOutput
    toOutput(LogoutInput input,LogoutReply reply)
    {
        return reply.isSuccess() ? clearAndDescribe(input) : describe(input,false);
    }

    private FrontEndOutput
    clearAndDescribe(LogoutInput input)
    {
        context.addSetCookie(SessionCookies.clear());
        return describe(input,true);
    }

    private FrontEndOutput
    describe(LogoutInput input,boolean loggedOut)
    {
        return
            new LogoutOutput()
                .setLoggedOut(loggedOut)
                .setUserId(input.getUserId())
                .setSource(input.getSource())
                .setCommand(input.getCommand())
                .setDestination(input.getDestination().orElse(null));
    }
}

//////////////////////////////////////////////////////////////////////////////
