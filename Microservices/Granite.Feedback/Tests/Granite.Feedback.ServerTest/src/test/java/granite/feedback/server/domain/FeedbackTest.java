//////////////////////////////////////////////////////////////////////////////
// FeedbackTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.server.domain;

import granite.feedback.service.requestreply.FeedbackStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class FeedbackTest
{
    private static final Instant T1 = Instant.parse("2026-11-01T10:00:00Z");
    private static final Instant T2 = Instant.parse("2026-11-01T11:00:00Z");

    @Test
    public void
    newFeedbackCanOnlyBeMarkedSent()
    {
        Feedback feedback = new Feedback();

        assertEquals(FeedbackStatus.NEW,feedback.getStatus());
        assertTrue(feedback.canMarkSent());
        assertFalse(feedback.canMarkReceived());
    }

    @Test
    public void
    sentThenReceivedStampsEachOnce()
    {
        Feedback feedback = new Feedback().changeStatus(FeedbackStatus.SENT,T1).changeStatus(FeedbackStatus.RECEIVED,T2);

        assertEquals(FeedbackStatus.RECEIVED,feedback.getStatus());
        assertEquals(T1,feedback.getSent());
        assertEquals(T2,feedback.getReceived());
    }

    @Test
    public void
    receivedDirectlyAlsoStampsSent()
    {
        Feedback feedback = new Feedback().changeStatus(FeedbackStatus.RECEIVED,T1);

        assertEquals(T1,feedback.getSent());
        assertEquals(T1,feedback.getReceived());
    }

    @Test
    public void
    nullOrSameStatusIsNoChange()
    {
        Feedback feedback = new Feedback().changeStatus(null,T1).changeStatus(FeedbackStatus.NEW,T1);

        assertEquals(FeedbackStatus.NEW,feedback.getStatus());
        assertNull(feedback.getSent());
    }
}

//////////////////////////////////////////////////////////////////////////////
