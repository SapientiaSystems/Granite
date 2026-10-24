//////////////////////////////////////////////////////////////////////////////
// StartOktaLoginView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.frontendcontroller.server.platform.SessionCookies;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.RedirectOutput;
import granite.frontendcontroller.service.requestreply.SessionOutput;
import granite.frontendcontroller.service.requestreply.StartOktaLoginInput;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.service.requestreply.StartOktaLoginReply;
import granite.session.service.requestreply.StartOktaLoginRequest;
import strata.frontendcontroller.server.application.IHandleInputAction;
import strata.frontendcontroller.service.requestreply.FrontEndInputMapper;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class StartOktaLoginView
    implements IHandleInputAction
{
    private final ISessionCommandServiceClient commandClient;
    private final SessionRequestContext        context;
    private final SessionLoginSettings         settings;
    private final FrontEndInputMapper          mapper;

    public
    StartOktaLoginView(
        ISessionCommandServiceClient commandClient,
        SessionRequestContext        context,
        SessionLoginSettings         settings)
    {
        this.commandClient = commandClient;
        this.context = context;
        this.settings = settings;
        this.mapper = FrontEndInputMapper.of();
    }

    @Override
    public CompletionStage<FrontEndOutput>
    apply(HandleInputRequest request)
    {
        return
            mapper
                .map(StartOktaLoginInput.class,request.getInput())
                .map(this::start)
                .orElseThrow();
    }

    protected CompletionStage<FrontEndOutput>
    start(StartOktaLoginInput input)
    {
        Optional<String> deviceId = context.getCookie(SessionCookies.DEVICE_COOKIE).filter(id -> !id.isBlank());

        if (!settings.isOktaEnabled() || deviceId.isEmpty())
            return CompletableFuture.completedFuture(refused(input));

        return
            commandClient
                .startOktaLogin(
                    new StartOktaLoginRequest()
                        .setDeviceId(deviceId.get())
                        .setReturnTo(input.getReturnTo()))
                .thenApply(reply -> toOutput(input,reply))
                .exceptionally(e -> refused(input));
    }

    protected FrontEndOutput
    toOutput(StartOktaLoginInput input,StartOktaLoginReply reply)
    {
        if (!reply.isSuccess() || isBlank(reply.getAuthorizeUrl()) || isBlank(reply.getTransactionId()))
            return refused(input);

        context.addSetCookie(SessionCookies.issueLogin(reply.getTransactionId()));

        return
            new RedirectOutput()
                .setUrl(reply.getAuthorizeUrl())
                .setUserId(input.getUserId())
                .setSource(input.getSource())
                .setCommand(input.getCommand())
                .setDestination(input.getDestination().orElse(null));
    }

    private FrontEndOutput
    refused(StartOktaLoginInput input)
    {
        return
            new SessionOutput()
                .setStatus("UNAUTHENTICATED")
                .setUserId(input.getUserId())
                .setSource(input.getSource())
                .setCommand(input.getCommand())
                .setDestination(input.getDestination().orElse(null));
    }

    private boolean
    isBlank(String value)
    {
        return value == null || value.isBlank();
    }
}

//////////////////////////////////////////////////////////////////////////////
