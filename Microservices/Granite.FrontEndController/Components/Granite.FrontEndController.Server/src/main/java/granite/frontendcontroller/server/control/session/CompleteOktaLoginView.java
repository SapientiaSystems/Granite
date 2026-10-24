//////////////////////////////////////////////////////////////////////////////
// CompleteOktaLoginView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.frontendcontroller.server.platform.SessionCookies;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.CompleteOktaLoginInput;
import granite.frontendcontroller.service.requestreply.SessionOutput;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.service.requestreply.CompleteOktaLoginReply;
import granite.session.service.requestreply.CompleteOktaLoginRequest;
import granite.session.service.requestreply.SessionData;
import strata.frontendcontroller.server.application.IHandleInputAction;
import strata.frontendcontroller.service.requestreply.FrontEndInputMapper;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class CompleteOktaLoginView
    implements IHandleInputAction
{
    private final ISessionCommandServiceClient commandClient;
    private final SessionRequestContext        context;
    private final SessionLoginSettings         settings;
    private final FrontEndInputMapper          mapper;

    public
    CompleteOktaLoginView(
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
                .map(CompleteOktaLoginInput.class,request.getInput())
                .map(this::complete)
                .orElseThrow();
    }

    protected CompletionStage<FrontEndOutput>
    complete(CompleteOktaLoginInput input)
    {
        Optional<String> deviceId      = context.getCookie(SessionCookies.DEVICE_COOKIE).filter(id -> !id.isBlank());
        Optional<String> transactionId = context.getCookie(SessionCookies.LOGIN_COOKIE).filter(id -> !id.isBlank());

        context.addSetCookie(SessionCookies.clearLogin());

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

    protected FrontEndOutput
    toOutput(CompleteOktaLoginInput input,CompleteOktaLoginReply reply)
    {
        SessionData data   = reply.getSessionData();
        String      handle = reply.getSessionHandle();

        if (!reply.isSuccess() || data == null || handle == null || handle.isBlank())
            return refused(input);

        context.addSetCookie(SessionCookies.issue(handle,data.getExpires(),Instant.now()));

        return
            describe(input)
                .setUserName(reply.getUserName())
                .setAuthorities(data.getAuthorities() == null ? Set.of() : data.getAuthorities())
                .setStatus("AUTHENTICATED")
                .setReturnTo(reply.getReturnTo());
    }

    private FrontEndOutput
    refused(CompleteOktaLoginInput input)
    {
        return describe(input).setStatus("UNAUTHENTICATED");
    }

    private SessionOutput
    describe(CompleteOktaLoginInput input)
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
