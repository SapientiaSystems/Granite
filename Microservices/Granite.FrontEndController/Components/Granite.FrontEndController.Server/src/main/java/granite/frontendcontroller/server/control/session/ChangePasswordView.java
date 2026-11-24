//////////////////////////////////////////////////////////////////////////////
// ChangePasswordView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.SessionInput;
import granite.frontendcontroller.service.requestreply.SessionOutput;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.service.requestreply.ChangePasswordReply;
import granite.session.service.requestreply.ChangePasswordRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class ChangePasswordView
    implements IInputAction<SessionInput,SessionOutput>
{
    public static final String NOT_AUTHENTICATED = "not authenticated";
    public static final String NOT_AVAILABLE     = "password change is not available";
    public static final String NOT_CHANGED       = "Unable to change password";

    private final ISessionCommandServiceClient commandClient;
    private final SessionRequestContext        context;
    private final SessionLoginSettings         settings;

    public
    ChangePasswordView(
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
        Optional<Long> sessionId = context.getSessionId();

        if (sessionId.isEmpty())
            return CompletableFuture.completedFuture(failed(input,NOT_AUTHENTICATED));

        if (!settings.isPasswordEnabled())
            return CompletableFuture.completedFuture(failed(input,NOT_AVAILABLE));

        return
            commandClient
                .changePassword(
                    new ChangePasswordRequest()
                        .setSessionId(sessionId.get())
                        .setCurrentPassword(input.getCurrentPassword())
                        .setNewPassword(input.getNewPassword()))
                .thenApply(reply -> toOutput(input,reply))
                .exceptionally(e -> failed(input,NOT_CHANGED));
    }

    protected SessionOutput
    toOutput(SessionInput input,ChangePasswordReply reply)
    {
        if (reply.isSuccess())
            return describe(input).setStatus("CHANGED");

        String message = reply.getFailureMessage();

        return failed(input,message == null || message.isBlank() ? NOT_CHANGED : message);
    }

    private SessionOutput
    failed(SessionInput input,String message)
    {
        return describe(input).setStatus("FAILED").setFailureMessages(new ArrayList<>(List.of(message)));
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
