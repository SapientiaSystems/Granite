//////////////////////////////////////////////////////////////////////////////
// SurveyResponseTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.server.domain;

import granite.survey.service.requestreply.SurveyResponseStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class SurveyResponseTest
{
    private static final Instant T1 = Instant.parse("2026-11-01T10:00:00Z");
    private static final Instant T2 = Instant.parse("2026-11-01T11:00:00Z");

    @Test
    public void
    newResponseCanOnlyBeMarkedSent()
    {
        SurveyResponse response = new SurveyResponse();

        assertEquals(SurveyResponseStatus.NEW,response.getStatus());
        assertTrue(response.canMarkSent());
        assertFalse(response.canMarkReceived());
    }

    @Test
    public void
    sentThenReceivedStampsEachOnce()
    {
        SurveyResponse response =
            new SurveyResponse()
                .changeStatus(SurveyResponseStatus.SENT,T1)
                .changeStatus(SurveyResponseStatus.RECEIVED,T2);

        assertEquals(SurveyResponseStatus.RECEIVED,response.getStatus());
        assertEquals(T1,response.getSent());
        assertEquals(T2,response.getReceived());
    }

    @Test
    public void
    nullOrSameStatusIsNoChange()
    {
        SurveyResponse response = new SurveyResponse().changeStatus(null,T1).changeStatus(SurveyResponseStatus.NEW,T1);

        assertEquals(SurveyResponseStatus.NEW,response.getStatus());
        assertNull(response.getSent());
    }

    @Test
    public void
    answersAreCopied()
    {
        Map<String,Integer> answers  = new HashMap<>(Map.of("satisfied",4));
        SurveyResponse      response = new SurveyResponse().setAnswers(answers);

        answers.put("satisfied",1);

        assertEquals(4,response.getAnswers().get("satisfied"));
    }
}

//////////////////////////////////////////////////////////////////////////////
