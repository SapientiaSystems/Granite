//////////////////////////////////////////////////////////////////////////////
// HomeMetricsCalculator.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.home;

import granite.feedback.service.requestreply.FeedbackData;
import granite.feedback.service.requestreply.FeedbackStatus;
import granite.feedback.service.requestreply.FeedbackType;
import granite.foundation.core.security.ApplicationRolesManager;
import granite.foundation.core.security.RoleDefinition;
import granite.frontendcontroller.server.control.survey.SurveyReviewLoader;
import granite.frontendcontroller.service.requestreply.HomeAverageData;
import granite.frontendcontroller.service.requestreply.HomeBreakdownData;
import granite.frontendcontroller.service.requestreply.HomeCountData;
import granite.frontendcontroller.service.requestreply.HomeFeedbackMetrics;
import granite.frontendcontroller.service.requestreply.HomeSessionMetrics;
import granite.frontendcontroller.service.requestreply.HomeSurveyMetrics;
import granite.frontendcontroller.service.requestreply.HomeUserMetrics;
import granite.session.service.requestreply.SessionData;
import granite.session.service.requestreply.SessionLoginMethod;
import granite.survey.service.requestreply.GraniteSurveyQuestions;
import granite.survey.service.requestreply.SurveyQuestionData;
import granite.survey.service.requestreply.SurveyResponseData;
import granite.user.service.requestreply.UserData;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public
class HomeMetricsCalculator
{
    public static final int      LOGIN_DAYS  = 14;
    public static final int      RECENT_DAYS = 7;
    public static final Duration HOUR        = Duration.ofHours(1);
    public static final String   OPEN        = "OPEN";
    public static final String   EXPIRED     = "EXPIRED";
    public static final String   HIGH        = "HIGH";
    public static final String   NEUTRAL     = "NEUTRAL";
    public static final String   LOW         = "LOW";

    private static final List<String>       OUTCOMES      = List.of(OPEN,"LOGOUT",EXPIRED,"REPLACED","DISPLACED","ADMIN","REVOKED_SECURITY");
    private static final Map<String,String> OUTCOME_LABEL =
        Map.of(
            OPEN,"Still open",
            "LOGOUT","Logged out",
            EXPIRED,"Expired",
            "REPLACED","Replaced",
            "DISPLACED","Displaced",
            "ADMIN","Ended by admin",
            "REVOKED_SECURITY","Security revoke");

    private final ApplicationRolesManager applicationRoles;
    private final Clock                   clock;

    public
    HomeMetricsCalculator(ApplicationRolesManager applicationRoles,Clock clock)
    {
        this.applicationRoles = applicationRoles;
        this.clock            = clock;
    }

    public Instant
    now() { return clock.instant(); }

    public List<String>
    roleLabels(Set<String> authorities)
    {
        List<String> labels = new ArrayList<>();
        Set<String>  held   = authorities == null ? Set.of() : authorities;

        for (RoleDefinition role : applicationRoles.getRoles())
            if (held.contains(role.getName()))
                labels.add(role.getLabel());

        return labels;
    }

    public HomeUserMetrics
    users(List<UserData> users)
    {
        List<HomeCountData> byRole     = new ArrayList<>();
        long                oktaLinked = 0;

        for (UserData user : users)
            if (user.getExternalIdentity() != null && !user.getExternalIdentity().isBlank())
                oktaLinked++;

        for (RoleDefinition role : applicationRoles.getRoles())
        {
            long count = 0;

            for (UserData user : users)
                if (user.getAuthorities() != null && user.getAuthorities().contains(role.getName()))
                    count++;

            byRole.add(new HomeCountData(role.getName(),role.getLabel(),count));
        }

        return
            new HomeUserMetrics()
                .setTotal(users.size())
                .setOktaLinked(oktaLinked)
                .setByRole(byRole);
    }

    public HomeSessionMetrics
    sessions(List<SessionData> sessions)
    {
        Instant                         now          = now();
        Map<String,Long>                openByMethod = zeroCounts(loginMethods());
        Map<String,Long>                outcomes     = zeroCounts(OUTCOMES);
        List<LocalDate>                 loginDays    = days(LOGIN_DAYS);
        Map<LocalDate,Map<String,Long>> daily        = new LinkedHashMap<>();
        Instant                         recentStart  = startOf(loginDays.get(LOGIN_DAYS - RECENT_DAYS));
        long                            open         = 0;
        long                            active       = 0;
        long                            expiring     = 0;

        for (LocalDate day : loginDays)
            daily.put(day,zeroCounts(loginMethods()));

        for (SessionData session : sessions)
        {
            boolean isOpen = isOpen(session,now);

            if (isOpen)
            {
                open++;

                if (session.getLastActivity() != null && !session.getLastActivity().isBefore(now.minus(HOUR)))
                    active++;

                if (session.getExpires() != null && !session.getExpires().isAfter(now.plus(HOUR)))
                    expiring++;

                if (session.getLoginMethod() != null)
                    openByMethod.merge(session.getLoginMethod().name(),1L,Long::sum);
            }

            if (session.getCreated() == null)
                continue;

            if (session.getCopiedFrom() == null && session.getLoginMethod() != null)
            {
                Map<String,Long> counts = daily.get(LocalDate.ofInstant(session.getCreated(),clock.getZone()));

                if (counts != null)
                    counts.merge(session.getLoginMethod().name(),1L,Long::sum);
            }

            if (!session.getCreated().isBefore(recentStart))
                outcomes.merge(outcome(session,isOpen),1L,Long::sum);
        }

        List<HomeBreakdownData> dailyLogins = new ArrayList<>();

        for (Map.Entry<LocalDate,Map<String,Long>> entry : daily.entrySet())
            dailyLogins.add(new HomeBreakdownData(entry.getKey().toString(),dayLabel(entry.getKey()),entry.getValue()));

        return
            new HomeSessionMetrics()
                .setOpen(open)
                .setActiveLastHour(active)
                .setExpiringWithinHour(expiring)
                .setOpenByLoginMethod(counts(openByMethod,this::loginMethodLabel))
                .setDailyLogins(dailyLogins)
                .setRecentOutcomes(counts(outcomes,OUTCOME_LABEL::get));
    }

    public HomeFeedbackMetrics
    feedback(List<FeedbackData> feedback)
    {
        Map<String,Long>             byStatus = zeroCounts(statuses());
        Map<String,Map<String,Long>> byType   = new LinkedHashMap<>();
        List<Instant>                created  = new ArrayList<>();

        for (FeedbackType type : FeedbackType.values())
            byType.put(type.name(),zeroCounts(statuses()));

        for (FeedbackData item : feedback)
        {
            String status = (item.getStatus() == null ? FeedbackStatus.NEW : item.getStatus()).name();

            byStatus.merge(status,1L,Long::sum);

            if (item.getType() != null)
                byType.get(item.getType().name()).merge(status,1L,Long::sum);

            created.add(item.getCreated());
        }

        List<HomeBreakdownData> types = new ArrayList<>();

        for (Map.Entry<String,Map<String,Long>> entry : byType.entrySet())
            types.add(new HomeBreakdownData(entry.getKey(),label(entry.getKey()),entry.getValue()));

        return
            new HomeFeedbackMetrics()
                .setTotal(feedback.size())
                .setByStatus(counts(byStatus,this::label))
                .setByType(types)
                .setDaily(daily(created,RECENT_DAYS));
    }

    public HomeSurveyMetrics
    survey(List<SurveyResponseData> responses)
    {
        Map<String,Double>    averages = SurveyReviewLoader.averages(responses);
        List<HomeAverageData> result   = new ArrayList<>();
        Map<String,Long>      bands    = zeroCounts(List.of(HIGH,NEUTRAL,LOW));
        List<Instant>         created  = new ArrayList<>();

        for (SurveyQuestionData question : GraniteSurveyQuestions.questions())
            result.add(new HomeAverageData(question.getKey(),question.getLabel(),averages.get(question.getKey())));

        for (SurveyResponseData response : responses)
        {
            Integer satisfied = response.getAnswers() == null ? null : response.getAnswers().get(GraniteSurveyQuestions.SATISFIED);

            if (satisfied != null)
                bands.merge(satisfied >= 4 ? HIGH : satisfied == 3 ? NEUTRAL : LOW,1L,Long::sum);

            created.add(response.getCreated());
        }

        return
            new HomeSurveyMetrics()
                .setResponses(responses.size())
                .setAverages(result)
                .setSatisfactionBands(counts(bands,this::bandLabel))
                .setDaily(daily(created,RECENT_DAYS));
    }

    protected boolean
    isOpen(SessionData session,Instant now)
    {
        return session.getClosedReason() == null && (session.getExpires() == null || session.getExpires().isAfter(now));
    }

    protected String
    outcome(SessionData session,boolean isOpen)
    {
        if (isOpen)
            return OPEN;

        return session.getClosedReason() == null ? EXPIRED : session.getClosedReason().name();
    }

    protected List<LocalDate>
    days(int count)
    {
        LocalDate       today = LocalDate.ofInstant(now(),clock.getZone());
        List<LocalDate> days  = new ArrayList<>();

        for (int offset = count - 1; offset >= 0; offset--)
            days.add(today.minusDays(offset));

        return days;
    }

    protected List<HomeCountData>
    daily(List<Instant> created,int count)
    {
        Map<LocalDate,Long> perDay = new LinkedHashMap<>();

        for (LocalDate day : days(count))
            perDay.put(day,0L);

        for (Instant instant : created)
            if (instant != null)
                perDay.computeIfPresent(LocalDate.ofInstant(instant,clock.getZone()),(day,n) -> n + 1);

        List<HomeCountData> result = new ArrayList<>();

        for (Map.Entry<LocalDate,Long> entry : perDay.entrySet())
            result.add(new HomeCountData(entry.getKey().toString(),dayLabel(entry.getKey()),entry.getValue()));

        return result;
    }

    private Instant
    startOf(LocalDate day)
    {
        return day.atStartOfDay(clock.getZone()).toInstant();
    }

    private String
    dayLabel(LocalDate day)
    {
        return String.valueOf(day.getDayOfMonth());
    }

    private String
    loginMethodLabel(String method)
    {
        return
            switch (method)
            {
                case "OKTA"         -> "Okta";
                case "PASSWORD"     -> "Password";
                case "ACCESS_TOKEN" -> "Access token";
                default             -> method;
            };
    }

    private String
    bandLabel(String band)
    {
        return
            switch (band)
            {
                case HIGH    -> "4 to 5";
                case NEUTRAL -> "3";
                default      -> "1 to 2";
            };
    }

    private String
    label(String name)
    {
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    private List<String>
    loginMethods()
    {
        return List.of(SessionLoginMethod.OKTA.name(),SessionLoginMethod.PASSWORD.name(),SessionLoginMethod.ACCESS_TOKEN.name());
    }

    private List<String>
    statuses()
    {
        List<String> names = new ArrayList<>();

        for (FeedbackStatus status : FeedbackStatus.values())
            names.add(status.name());

        return names;
    }

    private Map<String,Long>
    zeroCounts(List<String> keys)
    {
        Map<String,Long> counts = new LinkedHashMap<>();

        for (String key : keys)
            counts.put(key,0L);

        return counts;
    }

    private List<HomeCountData>
    counts(Map<String,Long> counts,Function<String,String> labeller)
    {
        List<HomeCountData> result = new ArrayList<>();

        for (Map.Entry<String,Long> entry : counts.entrySet())
            result.add(new HomeCountData(entry.getKey(),labeller.apply(entry.getKey()),entry.getValue()));

        return result;
    }
}

//////////////////////////////////////////////////////////////////////////////
