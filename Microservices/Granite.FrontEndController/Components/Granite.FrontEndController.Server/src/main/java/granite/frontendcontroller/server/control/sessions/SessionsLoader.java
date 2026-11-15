//////////////////////////////////////////////////////////////////////////////
// SessionsLoader.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.sessions;

import granite.frontendcontroller.service.requestreply.SessionRowData;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.session.service.requestreply.FindSessionsReply;
import granite.session.service.requestreply.FindSessionsRequest;
import granite.session.service.requestreply.SessionData;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindUsersByKeySetReply;
import granite.user.service.requestreply.FindUsersByKeySetRequest;
import granite.user.service.requestreply.UserData;
import granite.user.service.requestreply.UserKeySet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class SessionsLoader
{
    public static final String SOURCE_OPEN = "Session.Open";
    public static final String SOURCE_ALL  = "Session.All";

    private final ISessionQueryServiceClient sessionQueryClient;
    private final IUserQueryServiceClient    userQueryClient;

    public
    SessionsLoader(
        ISessionQueryServiceClient sessionQueryClient,
        IUserQueryServiceClient    userQueryClient)
    {
        this.sessionQueryClient = sessionQueryClient;
        this.userQueryClient    = userQueryClient;
    }

    public CompletionStage<List<SessionRowData>>
    load(String source,List<String> failures)
    {
        return
            sessionQueryClient
                .findSessions(new FindSessionsRequest().setIncludeClosed(SOURCE_ALL.equals(source)))
                .thenCompose(reply -> toRows(reply,failures));
    }

    protected CompletionStage<List<SessionRowData>>
    toRows(FindSessionsReply reply,List<String> failures)
    {
        if (reply == null || !reply.isSuccess() || reply.getSessions() == null)
        {
            failures.add(refreshFailure(reply == null ? null : reply.getFailureMessage()));
            return CompletableFuture.completedFuture(List.of());
        }

        List<SessionData> sessions = reply.getSessions();
        Set<Long>         userIds  = new LinkedHashSet<>();

        for (SessionData session : sessions)
        {
            if (session.getUserId() != null)
                userIds.add(session.getUserId());
        }

        if (userIds.isEmpty())
            return CompletableFuture.completedFuture(toRows(sessions,Map.of()));

        return
            userQueryClient
                .findUsersByKeySet(
                    new FindUsersByKeySetRequest()
                        .setKeys(UserKeySet.ofIds(userIds))
                        .setReturnIfFound(true))
                .thenApply(users -> toRows(sessions,userNames(users,failures)));
    }

    protected Map<Long,String>
    userNames(FindUsersByKeySetReply reply,List<String> failures)
    {
        Map<Long,String> names = new HashMap<>();

        if (reply == null || !reply.isSuccess())
        {
            failures.add(
                "Unable to resolve user names: " +
                reason(reply == null ? null : reply.getFailureMessage()));
            return names;
        }

        for (UserData user : reply.getFoundUsers() == null ? List.<UserData>of() : reply.getFoundUsers())
            names.put(user.getUserId(),user.getUserName());

        return names;
    }

    protected List<SessionRowData>
    toRows(List<SessionData> sessions,Map<Long,String> userNames)
    {
        List<SessionRowData> rows = new ArrayList<>();

        for (SessionData session : sessions)
            rows.add(toRow(session,userNames.get(session.getUserId())));

        return rows;
    }

    protected SessionRowData
    toRow(SessionData session,String userName)
    {
        return
            new SessionRowData()
                .setSessionId(session.getSessionId())
                .setUserId(session.getUserId())
                .setUserName(userName)
                .setAuthorities(session.getAuthorities() == null ? new ArrayList<>() : new ArrayList<>(new TreeSet<>(session.getAuthorities())))
                .setDeviceId(session.getDeviceId())
                .setLoginMethod(session.getLoginMethod() == null ? null : session.getLoginMethod().name())
                .setCreated(session.getCreated() == null ? null : session.getCreated().toString())
                .setLastActivity(session.getLastActivity() == null ? null : session.getLastActivity().toString())
                .setExpires(session.getExpires() == null ? null : session.getExpires().toString())
                .setClosedReason(session.getClosedReason() == null ? null : session.getClosedReason().name())
                .setContinuityId(session.getContinuityId() == null ? null : session.getContinuityId().toString())
                .setApplication(session.getApplication())
                .setReplacedBy(session.getReplacedBy())
                .setCopiedFrom(session.getCopiedFrom())
                .setValues(session.getValues() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(session.getValues()));
    }

    private String
    refreshFailure(String message)
    {
        return "Unable to refresh session list: " + reason(message);
    }

    private String
    reason(String message)
    {
        return message == null || message.isBlank() ? "unknown error" : message;
    }
}

//////////////////////////////////////////////////////////////////////////////
