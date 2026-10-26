//////////////////////////////////////////////////////////////////////////////
// LoginView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.frontendcontroller.server.platform.SessionCookies;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.service.requestreply.LoginReply;
import granite.session.service.requestreply.LoginRequest;
import granite.session.service.requestreply.SessionData;
import granite.user.service.requestreply.CredentialKind;
import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.service.requestreply.SessionInput;
import granite.frontendcontroller.service.requestreply.SessionOutput;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class LoginView
    implements IInputAction<SessionInput,SessionOutput>
{
    private final ISessionCommandServiceClient commandClient;
    private final SessionRequestContext        context;
    private final SessionLoginSettings         settings;

    public
    LoginView(
        ISessionCommandServiceClient commandClient,
        SessionRequestContext        context,
        SessionLoginSettings         settings)
    {
        this.commandClient = commandClient;
        this.context = context;
        this.settings = settings;
    }

    @Override
    public CompletionStage<SessionOutput>
    apply(SessionInput input)
    {
        Optional<String> deviceId = context.getCookie(SessionCookies.DEVICE_COOKIE).filter(id -> !id.isBlank());

        if (!settings.isPasswordEnabled()
            || isBlank(input.getUserName())
            || isBlank(input.getPassword())
            || deviceId.isEmpty())
            return CompletableFuture.completedFuture(refused(input));

        return
            commandClient
                .login(
                    new LoginRequest()
                        .setUserName(input.getUserName())
                        .setCredential(input.getPassword())
                        .setCredentialKind(CredentialKind.PASSWORD)
                        .setDeviceId(deviceId.get()))
                .thenApply(reply -> toOutput(input,reply))
                .exceptionally(e -> refused(input));
    }

    protected SessionOutput
    toOutput(SessionInput input,LoginReply reply)
    {
        SessionData data = reply.getSessionData();
        String      handle = reply.getSessionHandle();

        if (!reply.isSuccess() || data == null || handle == null || handle.isBlank())
            return refused(input);

        context.addSetCookie(SessionCookies.issue(handle,data.getExpires(),Instant.now()));

        return
            describe(input)
                .setUserName(input.getUserName())
                .setAuthorities(data.getAuthorities() == null ? Set.of() : data.getAuthorities())
                .setStatus("AUTHENTICATED");
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
