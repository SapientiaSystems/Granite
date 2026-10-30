//////////////////////////////////////////////////////////////////////////////
// OktaRequestReplyTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class OktaRequestReplyTest
{
    private static final String CODE           = "okta-code-0123456789";
    private static final String STATE          = "okta-state-0123456789";
    private static final String TRANSACTION_ID = "transaction-id-0123456789";
    private static final String HANDLE         = "session-handle-0123456789";
    private static final String AUTHORIZE_URL  = "https://okta.example.com/authorize?state=" + STATE;

    @Test
    void
    startRequestIsFluent()
    {
        StartOktaLoginRequest target = new StartOktaLoginRequest();

        assertSame(target,target.setDeviceId("device-1"));
        assertSame(target,target.setReturnTo("/users"));
        assertSame(target,target.setRequestId(UUID.randomUUID()));
        assertEquals("device-1",target.getDeviceId());
        assertEquals("/users",target.getReturnTo());
    }

    @Test
    void
    startReplyIsFluent()
    {
        StartOktaLoginReply target =
            new StartOktaLoginReply()
                .setSuccess(true)
                .setAuthorizeUrl(AUTHORIZE_URL)
                .setTransactionId(TRANSACTION_ID);

        assertTrue(target.isSuccess());
        assertEquals(AUTHORIZE_URL,target.getAuthorizeUrl());
        assertEquals(TRANSACTION_ID,target.getTransactionId());
    }

    @Test
    void
    completeRequestIsFluent()
    {
        CompleteOktaLoginRequest target =
            new CompleteOktaLoginRequest()
                .setCode(CODE)
                .setState(STATE)
                .setTransactionId(TRANSACTION_ID)
                .setDeviceId("device-1");

        assertEquals(CODE,target.getCode());
        assertEquals(STATE,target.getState());
        assertEquals(TRANSACTION_ID,target.getTransactionId());
        assertEquals("device-1",target.getDeviceId());
    }

    @Test
    void
    completeReplyIsFluent()
    {
        SessionData             data   = new SessionData().setSessionId(5L);
        CompleteOktaLoginReply target =
            new CompleteOktaLoginReply()
                .setSuccess(true)
                .setSessionData(data)
                .setSessionHandle(HANDLE)
                .setUserName("alice")
                .setReturnTo("/users");

        assertSame(data,target.getSessionData());
        assertEquals(HANDLE,target.getSessionHandle());
        assertEquals("alice",target.getUserName());
        assertEquals("/users",target.getReturnTo());
    }

    @Test
    void
    logoutReplyHasNoEndSessionField()
    {
        assertFalse(new LogoutReply().toString().contains("endSessionUrl"));
    }

    @Test
    void
    toStringHidesSecrets()
    {
        String start    =
            new StartOktaLoginReply()
                .setAuthorizeUrl(AUTHORIZE_URL)
                .setTransactionId(TRANSACTION_ID)
                .toString();
        String complete =
            new CompleteOktaLoginRequest()
                .setCode(CODE)
                .setState(STATE)
                .setTransactionId(TRANSACTION_ID)
                .setDeviceId("device-1")
                .toString();
        String reply    =
            new CompleteOktaLoginReply()
                .setSessionHandle(HANDLE)
                .setSessionData(new SessionData().setSessionId(5L))
                .toString();
        String logout   = new LogoutReply().toString();

        for (String text : new String[] {start,complete,reply,logout})
        {
            assertFalse(text.contains(CODE),text);
            assertFalse(text.contains(STATE),text);
            assertFalse(text.contains(TRANSACTION_ID),text);
            assertFalse(text.contains(HANDLE),text);
            assertFalse(text.contains("id.token.value"),text);
        }

        assertTrue(start.startsWith("StartOktaLoginReply"));
        assertTrue(complete.startsWith("CompleteOktaLoginRequest"));
        assertTrue(reply.startsWith("CompleteOktaLoginReply"));
        assertTrue(logout.startsWith("LogoutReply"));
    }
}

//////////////////////////////////////////////////////////////////////////////
