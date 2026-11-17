//////////////////////////////////////////////////////////////////////////////
// SessionStatusView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.session;

import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.server.control.IOutputAction;
import granite.frontendcontroller.service.requestreply.GetSessionOutputRequest;
import granite.frontendcontroller.service.requestreply.SessionOutput;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.session.service.requestreply.FindSessionsByKeyReply;
import granite.session.service.requestreply.FindSessionsByKeyRequest;
import granite.session.service.requestreply.SessionData;
import granite.session.service.requestreply.SessionKey;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindUsersByKeyReply;
import granite.user.service.requestreply.FindUsersByKeyRequest;
import granite.user.service.requestreply.UserKey;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class SessionStatusView
    implements IOutputAction<GetSessionOutputRequest,SessionOutput>
{
    private final ISessionQueryServiceClient sessionClient;
    private final IUserQueryServiceClient    userClient;
    private final SessionRequestContext      context;
    private final SessionLoginSettings       settings;

    public
    SessionStatusView(
        ISessionQueryServiceClient sessionClient,
        IUserQueryServiceClient    userClient,
        SessionRequestContext      context)
    {
        this(sessionClient,userClient,context,new SessionLoginSettings(true,false));
    }

    public
    SessionStatusView(
        ISessionQueryServiceClient sessionClient,
        IUserQueryServiceClient    userClient,
        SessionRequestContext      context,
        SessionLoginSettings       settings)
    {
        this.sessionClient = sessionClient;
        this.userClient = userClient;
        this.context = context;
        this.settings = settings;
    }

    @Override
    public CompletionStage<SessionOutput>
    apply(GetSessionOutputRequest request)
    {
        Optional<Long> sessionId = context.getSessionId();

        if (sessionId.isEmpty())
            return CompletableFuture.completedFuture(unauthenticated(request));

        return
            sessionClient
                .findSessionsByKey(new FindSessionsByKeyRequest().setKey(SessionKey.ofSessionId(sessionId.get())))
                .thenCompose(reply -> describe(request,openSession(reply)))
                .exceptionally(e -> unauthenticated(request));
    }

    protected Optional<SessionData>
    openSession(FindSessionsByKeyReply reply)
    {
        if (!reply.isSuccess() || reply.getSessions() == null || reply.getSessions().isEmpty())
            return Optional.empty();

        SessionData session = reply.getSessions().get(0);
        Instant     expires = session.getExpires();

        if (session.getClosedReason() != null || (expires != null && !expires.isAfter(Instant.now())))
            return Optional.empty();

        if (context.getCallerUserId().isPresent() && !context.getCallerUserId().get().equals(session.getUserId()))
            return Optional.empty();

        return Optional.of(session);
    }

    protected CompletionStage<SessionOutput>
    describe(GetSessionOutputRequest request,Optional<SessionData> session)
    {
        if (session.isEmpty())
            return CompletableFuture.completedFuture(unauthenticated(request));

        return
            userClient
                .findUsersByKey(
                    new FindUsersByKeyRequest()
                        .setKey(UserKey.ofUserId(session.get().getUserId()))
                        .setReturnIfFound(true))
                .thenApply(reply -> toOutput(request,session.get(),reply));
    }

    protected SessionOutput
    toOutput(GetSessionOutputRequest request,SessionData session,FindUsersByKeyReply reply)
    {
        if (!reply.isSuccess() || reply.getFoundUsers() == null || reply.getFoundUsers().isEmpty())
            return unauthenticated(request);

        String password = reply.getFoundUsers().get(0).getPassword();

        return
            describe(request)
                .setUserName(reply.getFoundUsers().get(0).getUserName())
                .setPasswordSet(password != null && !password.isBlank())
                .setPasswordEnabled(settings.isPasswordEnabled())
                .setAuthorities(session.getAuthorities() == null ? Set.of() : session.getAuthorities())
                .setStatus("AUTHENTICATED");
    }

    private SessionOutput
    unauthenticated(GetSessionOutputRequest request)
    {
        return describe(request).setStatus("UNAUTHENTICATED");
    }

    private SessionOutput
    describe(GetSessionOutputRequest request)
    {
        return
            new SessionOutput()
                .setUserId(request.getUserId())
                .setSource(request.getSource())
                .setCommand(request.getCommand())
                .setDestination(request.getDestination());
    }
}

//////////////////////////////////////////////////////////////////////////////
