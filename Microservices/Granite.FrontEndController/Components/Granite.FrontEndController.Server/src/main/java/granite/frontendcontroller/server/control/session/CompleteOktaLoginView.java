//////////////////////////////////////////////////////////////////////////////
// CompleteOktaLoginView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.frontendcontroller.server.platform.SessionCookies;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.service.requestreply.CompleteOktaLoginReply;
import granite.session.service.requestreply.CompleteOktaLoginRequest;
import granite.session.service.requestreply.SessionData;
import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.service.requestreply.SessionInput;
import granite.frontendcontroller.service.requestreply.SessionOutput;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class CompleteOktaLoginView
    implements IInputAction<SessionInput,SessionOutput>
{
    private final ISessionCommandServiceClient commandClient;
    private final SessionRequestContext        context;
    private final SessionLoginSettings         settings;
    private final SessionCookies               cookies;

    public
    CompleteOktaLoginView(
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
        Optional<String> deviceId      = context.getCookie(cookies.getDeviceName()).filter(id -> !id.isBlank());
        Optional<String> transactionId = context.getCookie(cookies.getLoginName()).filter(id -> !id.isBlank());

        context.addSetCookie(cookies.clearLogin());

        if (!settings.isOktaEnabled() || deviceId.isEmpty() || transactionId.isEmpty())
            return CompletableFuture.completedFuture(refused(input));

        return
            commandClient
                .completeOktaLogin(
                    new CompleteOktaLoginRequest()
                        .setCode(input.getCode())
                        .setState(input.getState())
                        .setTransactionId(transactionId.get())
                        .setDeviceId(deviceId.get()))
                .thenApply(reply -> toOutput(input,reply))
                .exceptionally(e -> refused(input));
    }

    protected SessionOutput
    toOutput(SessionInput input,CompleteOktaLoginReply reply)
    {
        SessionData data   = reply.getSessionData();
        String      handle = reply.getSessionHandle();

        if (!reply.isSuccess() || data == null || handle == null || handle.isBlank())
            return refused(input);

        context.addSetCookie(cookies.issue(handle,data.getExpires(),Instant.now()));

        return
            describe(input)
                .setUserName(reply.getUserName())
                .setAuthorities(data.getAuthorities() == null ? Set.of() : data.getAuthorities())
                .setStatus("AUTHENTICATED")
                .setReturnTo(reply.getReturnTo());
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
}

//////////////////////////////////////////////////////////////////////////////
