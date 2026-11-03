//////////////////////////////////////////////////////////////////////////////
// HomeMetricsCalculatorTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.home;

import granite.feedback.service.requestreply.FeedbackData;
import granite.feedback.service.requestreply.FeedbackStatus;
import granite.feedback.service.requestreply.FeedbackType;
import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.frontendcontroller.service.requestreply.HomeBreakdownData;
import granite.frontendcontroller.service.requestreply.HomeCountData;
import granite.frontendcontroller.service.requestreply.HomeFeedbackMetrics;
import granite.frontendcontroller.service.requestreply.HomeSessionMetrics;
import granite.frontendcontroller.service.requestreply.HomeSurveyMetrics;
import granite.frontendcontroller.service.requestreply.HomeUserMetrics;
import granite.session.service.requestreply.SessionClosedReason;
import granite.session.service.requestreply.SessionData;
import granite.session.service.requestreply.SessionLoginMethod;
import granite.survey.service.requestreply.SurveyResponseData;
import granite.user.service.requestreply.UserData;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("CommitStage")
public
class HomeMetricsCalculatorTest
{
    private static final Instant NOW = Instant.parse("2026-11-02T18:00:00Z");

    private final HomeMetricsCalculator calculator =
        new HomeMetricsCalculator(new GraniteApplicationRolesManagerSupplier().get(),Clock.fixed(NOW,ZoneOffset.UTC));

    @Test
    public void
    labelsTheCallersCatalogRolesInCatalogOrder()
    {
        assertEquals(List.of("Admin","User"),calculator.roleLabels(Set.of("ROLE_USER","ROLE_ADMIN","SCOPE_internal")));
        assertEquals(List.of(),calculator.roleLabels(null));
    }

    @Test
    public void
    countsUsersByRoleAndOktaLink()
    {
        HomeUserMetrics metrics =
            calculator.users(
                List.of(
                    new UserData().setUserId(1L).setAuthorities(Set.of("ROLE_ADMIN")).setExternalIdentity("okta|1"),
                    new UserData().setUserId(2L).setAuthorities(Set.of("ROLE_USER")).setExternalIdentity(" "),
                    new UserData().setUserId(3L).setAuthorities(Set.of("ROLE_AUDITOR","ROLE_USER"))));

        assertEquals(3,metrics.getTotal());
        assertEquals(1,metrics.getOktaLinked());
        assertEquals(Map.of("ROLE_ADMIN",1L,"ROLE_AUDITOR",1L,"ROLE_USER",2L),counts(metrics.getByRole()));
        assertEquals("Admin",metrics.getByRole().get(0).getLabel());
    }

    @Test
    public void
    summarisesSessions()
    {
        HomeSessionMetrics metrics =
            calculator.sessions(
                List.of(
                    session(1L,SessionLoginMethod.OKTA,ago(1),ago(0).minusSeconds(600),NOW.plus(Duration.ofMinutes(30)),null,null),
                    session(2L,SessionLoginMethod.OKTA,ago(1),ago(2),NOW.plus(Duration.ofHours(8)),null,1L),
                    session(3L,SessionLoginMethod.PASSWORD,days(3),days(3),NOW.plus(Duration.ofDays(1)),null,null),
                    session(4L,SessionLoginMethod.PASSWORD,days(2),days(2),days(1),SessionClosedReason.LOGOUT,null),
                    session(5L,SessionLoginMethod.ACCESS_TOKEN,days(10),days(10),days(9),null,null),
                    session(6L,SessionLoginMethod.OKTA,days(20),days(20),days(19),SessionClosedReason.ADMIN,null)));

        assertEquals(3,metrics.getOpen());
        assertEquals(1,metrics.getActiveLastHour());
        assertEquals(1,metrics.getExpiringWithinHour());
        assertEquals(Map.of("OKTA",2L,"PASSWORD",1L,"ACCESS_TOKEN",0L),counts(metrics.getOpenByLoginMethod()));
        assertEquals(14,metrics.getDailyLogins().size());

        HomeBreakdownData today = metrics.getDailyLogins().get(13);

        assertEquals("2026-11-02",today.getKey());
        assertEquals("2",today.getLabel());
        assertEquals(1L,today.getCounts().get("OKTA"));
        assertEquals(1L,metrics.getDailyLogins().get(3).getCounts().get("ACCESS_TOKEN"));
        assertEquals(0L,metrics.getDailyLogins().get(0).getCounts().get("OKTA"));

        Map<String,Long> outcomes = counts(metrics.getRecentOutcomes());

        assertEquals(3L,outcomes.get("OPEN"));
        assertEquals(1L,outcomes.get("LOGOUT"));
        assertEquals(0L,outcomes.get("EXPIRED"));
        assertEquals(0L,outcomes.get("ADMIN"));
        assertEquals("Still open",metrics.getRecentOutcomes().get(0).getLabel());
    }

    @Test
    public void
    treatsAnUnclosedPastExpiryAsExpired()
    {
        HomeSessionMetrics metrics =
            calculator.sessions(List.of(session(1L,SessionLoginMethod.PASSWORD,days(1),days(1),ago(1),null,null)));

        assertEquals(0,metrics.getOpen());
        assertEquals(1L,counts(metrics.getRecentOutcomes()).get("EXPIRED"));
    }

    @Test
    public void
    summarisesFeedbackByStatusTypeAndDay()
    {
        HomeFeedbackMetrics metrics =
            calculator.feedback(
                List.of(
                    feedback(FeedbackType.DEFECT,FeedbackStatus.NEW,ago(2)),
                    feedback(FeedbackType.DEFECT,FeedbackStatus.RECEIVED,days(1)),
                    feedback(FeedbackType.KUDOS,null,days(30))));

        assertEquals(3,metrics.getTotal());
        assertEquals(Map.of("NEW",2L,"SENT",0L,"RECEIVED",1L),counts(metrics.getByStatus()));
        assertEquals("New",metrics.getByStatus().get(0).getLabel());
        assertEquals("DEFECT",metrics.getByType().get(0).getKey());
        assertEquals("Defect",metrics.getByType().get(0).getLabel());
        assertEquals(Map.of("NEW",1L,"SENT",0L,"RECEIVED",1L),metrics.getByType().get(0).getCounts());
        assertEquals(1L,metrics.getByType().get(3).getCounts().get("NEW"));
        assertEquals(7,metrics.getDaily().size());
        assertEquals(1L,metrics.getDaily().get(6).getCount());
        assertEquals(1L,metrics.getDaily().get(5).getCount());
    }

    @Test
    public void
    summarisesSurveyResponses()
    {
        HomeSurveyMetrics metrics =
            calculator.survey(
                List.of(
                    survey(Map.of("satisfied",5,"needs",4,"recommend",5),ago(1)),
                    survey(Map.of("satisfied",3,"needs",2,"recommend",4),days(2)),
                    survey(Map.of("satisfied",1,"needs",1,"recommend",2),days(9))));

        assertEquals(3,metrics.getResponses());
        assertEquals("satisfied",metrics.getAverages().get(0).getKey());
        assertEquals(3.0,metrics.getAverages().get(0).getAverage());
        assertEquals(Map.of("HIGH",1L,"NEUTRAL",1L,"LOW",1L),counts(metrics.getSatisfactionBands()));
        assertEquals("4 to 5",metrics.getSatisfactionBands().get(0).getLabel());
        assertEquals(1L,metrics.getDaily().get(6).getCount());
        assertEquals(1L,metrics.getDaily().get(4).getCount());
    }

    @Test
    public void
    leavesAveragesEmptyWithoutResponses()
    {
        HomeSurveyMetrics metrics = calculator.survey(List.of());

        assertEquals(3,metrics.getAverages().size());
        assertNull(metrics.getAverages().get(0).getAverage());
    }

    private static Map<String,Long>
    counts(List<HomeCountData> data)
    {
        Map<String,Long> result = new LinkedHashMap<>();

        for (HomeCountData item : data)
            result.put(item.getKey(),item.getCount());

        return result;
    }

    private static Instant
    ago(int hours) { return NOW.minus(Duration.ofHours(hours)); }

    private static Instant
    days(int days) { return NOW.minus(Duration.ofDays(days)); }

    private static SessionData
    session(Long id,SessionLoginMethod method,Instant created,Instant lastActivity,Instant expires,SessionClosedReason reason,Long copiedFrom)
    {
        return
            new SessionData()
                .setSessionId(id)
                .setLoginMethod(method)
                .setCreated(created)
                .setLastActivity(lastActivity)
                .setExpires(expires)
                .setClosedReason(reason)
                .setCopiedFrom(copiedFrom);
    }

    private static FeedbackData
    feedback(FeedbackType type,FeedbackStatus status,Instant created)
    {
        return new FeedbackData().setType(type).setStatus(status).setCreated(created);
    }

    private static SurveyResponseData
    survey(Map<String,Integer> answers,Instant created)
    {
        return new SurveyResponseData().setAnswers(answers).setCreated(created);
    }
}

//////////////////////////////////////////////////////////////////////////////
