//////////////////////////////////////////////////////////////////////////////
// StartOktaLoginView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.frontendcontroller.server.platform.SessionCookies;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.service.requestreply.StartOktaLoginReply;
import granite.session.service.requestreply.StartOktaLoginRequest;
import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.service.requestreply.SessionInput;
import granite.frontendcontroller.service.requestreply.SessionOutput;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class StartOktaLoginView
    implements IInputAction<SessionInput,SessionOutput>
{
    private final ISessionCommandServiceClient commandClient;
    private final SessionRequestContext        context;
    private final SessionLoginSettings         settings;
    private final SessionCookies               cookies;

    public
    StartOktaLoginView(
        ISessionCommandServiceClient commandClient,
        SessionRequestContext        context,
        SessionLoginSettings         settings,
        SessionCookies               cookies)
    {
        this.commandClient = commandClient;
        this.context = context;
        this.settings = settings;
        this.cookies = cookies;
    }

    @Override
    public CompletionStage<SessionOutput>
    apply(SessionInput input)
    {
        Optional<String> deviceId = context.getCookie(cookies.getDeviceName()).filter(id -> !id.isBlank());

        if (!settings.isOktaEnabled() || deviceId.isEmpty())
            return CompletableFuture.completedFuture(refused(input));

        return
            commandClient
                .startOktaLogin(
                    new StartOktaLoginRequest()
                        .setDeviceId(deviceId.get())
                        .setReturnTo(input.getReturnTo())
                        .setApplication(SessionApplication.resolve(input.getApplication())))
                .thenApply(reply -> toOutput(input,reply))
                .exceptionally(e -> refused(input));
    }

    protected SessionOutput
    toOutput(SessionInput input,StartOktaLoginReply reply)
    {
        if (!reply.isSuccess() || isBlank(reply.getAuthorizeUrl()) || isBlank(reply.getTransactionId()))
            return refused(input);

        context.addSetCookie(cookies.issueLogin(reply.getTransactionId()));

        return describe(input).setRedirectUrl(reply.getAuthorizeUrl());
    }

    private SessionOutput
    refused(SessionInput input)
    {
        return describe(input).setStatus("UNAUTHENTICATED");
    }

    private SessionOutput
    describe(SessionInput input)
    {
        return
            new SessionOutput()
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
