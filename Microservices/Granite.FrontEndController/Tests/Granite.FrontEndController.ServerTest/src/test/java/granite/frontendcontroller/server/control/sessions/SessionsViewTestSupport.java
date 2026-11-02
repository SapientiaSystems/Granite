//////////////////////////////////////////////////////////////////////////////
// SessionsViewTestSupport.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.sessions;

import granite.session.service.requestreply.FindSessionsReply;
import granite.session.service.requestreply.SessionClosedReason;
import granite.session.service.requestreply.SessionData;
import granite.session.service.requestreply.SessionLoginMethod;
import granite.user.service.requestreply.FindUsersByKeySetReply;
import granite.user.service.requestreply.UserData;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

public
class SessionsViewTestSupport
{
    public static final Instant NOW = Instant.parse("2026-11-01T12:00:00Z");

    private
    SessionsViewTestSupport() {}

    public static SessionData
    session(Long sessionId,Long userId)
    {
        return
            new SessionData()
                .setSessionId(sessionId)
                .setUserId(userId)
                .setDeviceId("device-" + sessionId)
                .setCreated(NOW.minusSeconds(600))
                .setLastActivity(NOW.minusSeconds(60))
                .setExpires(NOW.plusSeconds(3600))
                .setLoginMethod(SessionLoginMethod.PASSWORD)
                .setAuthorities(Set.of("ROLE_USER","ROLE_ADMIN"))
                .setValues(Map.of("cart",3));
    }

    public static SessionData
    closed(Long sessionId,Long userId)
    {
        return session(sessionId,userId).setClosedReason(SessionClosedReason.ADMIN);
    }

    public static FindSessionsReply
    sessions(SessionData... sessions)
    {
        return new FindSessionsReply().setSuccess(true).setSessions(List.of(sessions));
    }

    public static FindUsersByKeySetReply
    users(UserData... users)
    {
        return new FindUsersByKeySetReply().setSuccess(true).setFoundUsers(List.of(users));
    }

    public static UserData
    user(Long userId,String userName)
    {
        return new UserData().setUserId(userId).setUserName(userName);
    }
}

//////////////////////////////////////////////////////////////////////////////
