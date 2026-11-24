//////////////////////////////////////////////////////////////////////////////
// FrontEndJsonSerializationTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import granite.frontendcontroller.service.requestreply.GetPreferencesOutputReply;
import granite.frontendcontroller.service.requestreply.GetSessionOutputReply;
import granite.frontendcontroller.service.requestreply.GetHomeOutputReply;
import granite.frontendcontroller.service.requestreply.GetUsersOutputReply;
import granite.frontendcontroller.service.requestreply.HomeAverageData;
import granite.frontendcontroller.service.requestreply.HomeBreakdownData;
import granite.frontendcontroller.service.requestreply.HomeCountData;
import granite.frontendcontroller.service.requestreply.HomeOutput;
import granite.frontendcontroller.service.requestreply.HomeSessionMetrics;
import granite.frontendcontroller.service.requestreply.HomeSurveyMetrics;
import granite.frontendcontroller.service.requestreply.HomeUserMetrics;
import granite.frontendcontroller.service.requestreply.HandlePreferencesInputRequest;
import granite.frontendcontroller.service.requestreply.PreferenceDefinitionData;
import granite.frontendcontroller.service.requestreply.PreferencesInput;
import granite.frontendcontroller.service.requestreply.PreferencesOutput;
import granite.frontendcontroller.service.requestreply.HandleSessionInputReply;
import granite.frontendcontroller.service.requestreply.HandleSessionInputRequest;
import granite.frontendcontroller.service.requestreply.HandleUsersInputRequest;
import granite.frontendcontroller.service.requestreply.SessionInput;
import granite.frontendcontroller.service.requestreply.SessionOutput;
import granite.frontendcontroller.service.requestreply.UsersInput;
import granite.frontendcontroller.service.requestreply.UsersOutput;
import granite.frontendcontroller.service.requestreply.RoleData;
import granite.feedback.service.requestreply.FeedbackType;
import granite.frontendcontroller.service.requestreply.FeedbackInput;
import granite.frontendcontroller.service.requestreply.HandleFeedbackInputRequest;
import granite.frontendcontroller.service.requestreply.HandleSurveyInputRequest;
import granite.frontendcontroller.service.requestreply.ContentInput;
import granite.frontendcontroller.service.requestreply.AgentInput;
import granite.frontendcontroller.service.requestreply.HandleAgentInputRequest;
import granite.frontendcontroller.service.requestreply.HandleContentInputRequest;
import granite.content.service.requestreply.ContentKind;
import granite.frontendcontroller.service.requestreply.SurveyInput;
import granite.frontendcontroller.service.requestreply.GetSessionsOutputReply;
import granite.frontendcontroller.service.requestreply.HandleSessionsInputRequest;
import granite.frontendcontroller.service.requestreply.SessionRowData;
import granite.frontendcontroller.service.requestreply.SessionsInput;
import granite.frontendcontroller.service.requestreply.SessionsOutput;
import granite.user.service.requestreply.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import strata.foundation.core.mapper.ObjectMapperSupplier;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class FrontEndJsonSerializationTest
{
    private static final String SAVE_JSON =
        "{\"input\":{\"userId\":7,\"source\":\"User.All\",\"command\":\"Save\",\"destination\":\"UsersEditableView\","
            + "\"usersToCreate\":[{\"userName\":\"amy\",\"password\":\"pw\",\"authorities\":[\"ROLE_AUDITOR\"],"
            + "\"values\":{\"theme\":\"dark\"}}]}}";

    private static final String LOGIN_JSON =
        "{\"input\":{\"source\":\"Session.New\",\"command\":\"Login\",\"destination\":\"LoginView\","
            + "\"userName\":\"amy\",\"password\":\"pw\"}}";

    private static final String COMPLETE_JSON =
        "{\"input\":{\"source\":\"Session.New\",\"command\":\"CompleteOktaLogin\",\"destination\":\"LoginView\","
            + "\"code\":\"the-code\",\"state\":\"the-state\"}}";

    private ObjectMapper mapper;

    @BeforeEach
    public void
    setUp()
    {
        mapper = new ObjectMapperSupplier().get();
    }

    @Test
    public void
    usersRequestBindsUsersInputWithoutTypeProperty()
        throws Exception
    {
        UsersInput input = mapper.readValue(SAVE_JSON,HandleUsersInputRequest.class).getInput();

        assertEquals("User.All->Save->UsersEditableView",input.getKey());
        assertEquals(1,input.getUsersToCreate().size());
        assertEquals("amy",input.getUsersToCreate().get(0).getUserName());
        assertEquals(Map.of("theme","dark"),input.getUsersToCreate().get(0).getValues());
        assertEquals(List.of(),input.getUsersToUpdate());
        assertEquals(List.of(),input.getUserIdsToDelete());
    }

    @Test
    public void
    usersReplyRoundTrips()
        throws Exception
    {
        GetUsersOutputReply reply =
            new GetUsersOutputReply()
                .setSuccess(true)
                .setOutput(
                    new UsersOutput()
                        .setUsers(
                            List.of(
                                new UserData()
                                    .setUserId(1L)
                                    .setUserName("amy")
                                    .setAuthorities(Set.of("ROLE_AUDITOR"))))
                        .setFailureMessages(List.of("bad user")));

        String              json   = mapper.writeValueAsString(reply);
        GetUsersOutputReply actual = mapper.readValue(json,GetUsersOutputReply.class);

        assertEquals("amy",actual.getOutput().getUsers().get(0).getUserName());
        assertEquals(List.of("bad user"),actual.getOutput().getFailureMessages());
        assertFalse(json.contains("@class"));
        assertFalse(json.contains("@type"));
    }

    @Test
    public void
    usersOutputSerializesRoles()
        throws Exception
    {
        GetUsersOutputReply reply =
            new GetUsersOutputReply()
                .setSuccess(true)
                .setOutput(
                    new UsersOutput()
                        .setRoles(List.of(new RoleData().setName("ROLE_MANAGER").setLabel("Manager").setImpliedRoles(List.of("ROLE_CLERK")))));

        String              json   = mapper.writeValueAsString(reply);
        GetUsersOutputReply actual = mapper.readValue(json,GetUsersOutputReply.class);

        assertTrue(json.contains("\"roles\":[{\"name\":\"ROLE_MANAGER\",\"label\":\"Manager\",\"impliedRoles\":[\"ROLE_CLERK\"]}]"),json);
        assertEquals("ROLE_MANAGER",actual.getOutput().getRoles().get(0).getName());
        assertEquals(List.of("ROLE_CLERK"),actual.getOutput().getRoles().get(0).getImpliedRoles());
    }

    @Test
    public void
    homeReplyRoundTrips()
        throws Exception
    {
        GetHomeOutputReply reply =
            new GetHomeOutputReply()
                .setSuccess(true)
                .setOutput(
                    new HomeOutput()
                        .setUserName("alice")
                        .setRoles(List.of("Admin"))
                        .setGenerated("2026-11-02T18:00:00Z")
                        .setUsers(new HomeUserMetrics().setTotal(2).setByRole(List.of(new HomeCountData("ROLE_ADMIN","Admin",1))))
                        .setSessions(new HomeSessionMetrics().setOpen(3).setDailyLogins(List.of(new HomeBreakdownData("2026-11-02","2",Map.of("OKTA",4L)))))
                        .setSurvey(new HomeSurveyMetrics().setAverages(List.of(new HomeAverageData("satisfied","Satisfied?",4.25)))));

        String             json   = mapper.writeValueAsString(reply);
        GetHomeOutputReply actual = mapper.readValue(json,GetHomeOutputReply.class);

        assertEquals("alice",actual.getOutput().getUserName());
        assertEquals(List.of("Admin"),actual.getOutput().getRoles());
        assertEquals(1L,actual.getOutput().getUsers().getByRole().get(0).getCount());
        assertEquals(4L,actual.getOutput().getSessions().getDailyLogins().get(0).getCounts().get("OKTA"));
        assertEquals(4.25,actual.getOutput().getSurvey().getAverages().get(0).getAverage());
        assertNull(actual.getOutput().getFeedback());
    }

    @Test
    public void
    sessionsRequestBindsSessionsInputWithoutTypeProperty()
        throws Exception
    {
        String json =
            "{\"input\":{\"source\":\"Session.Open\",\"command\":\"Close\",\"destination\":\"SessionsView\",\"sessionIdsToClose\":[12,13]}}";

        SessionsInput input = mapper.readValue(json,HandleSessionsInputRequest.class).getInput();

        assertEquals("Session.Open->Close->SessionsView",input.getKey());
        assertEquals(List.of(12L,13L),input.getSessionIdsToClose());
    }

    @Test
    public void
    feedbackRequestBindsFeedbackInputWithoutTypeProperty()
        throws Exception
    {
        String json =
            "{\"input\":{\"source\":\"Feedback.New\",\"command\":\"Submit\",\"destination\":\"FeedbackView\"," +
            "\"type\":\"PROBLEM\",\"summary\":\"Slow\",\"details\":{\"impact\":\"Waiting\"}}}";

        FeedbackInput input = mapper.readValue(json,HandleFeedbackInputRequest.class).getInput();

        assertEquals("Feedback.New->Submit->FeedbackView",input.getKey());
        assertEquals(FeedbackType.PROBLEM,input.getType());
        assertEquals(Map.of("impact","Waiting"),input.getDetails());
    }

    @Test
    public void
    agentRequestBindsAgentInputWithoutTypeProperty()
        throws Exception
    {
        String json =
            "{\"input\":{\"source\":\"Agent.Current\",\"command\":\"Approve\",\"destination\":\"AgentView\"," +
            "\"conversationId\":3,\"toolUseId\":\"toolu_1\"}}";

        AgentInput input = mapper.readValue(json,HandleAgentInputRequest.class).getInput();

        assertEquals("Agent.Current->Approve->AgentView",input.getKey());
        assertEquals(3L,input.getConversationId());
        assertEquals("toolu_1",input.getToolUseId());
    }

    @Test
    public void
    contentRequestBindsContentInputWithoutTypeProperty()
        throws Exception
    {
        String json =
            "{\"input\":{\"source\":\"Content.All\",\"command\":\"Search\",\"destination\":\"ContentView\"," +
            "\"query\":\"users\",\"kinds\":[\"HELP\",\"GUIDE\"]}}";

        ContentInput input = mapper.readValue(json,HandleContentInputRequest.class).getInput();

        assertEquals("Content.All->Search->ContentView",input.getKey());
        assertEquals("users",input.getQuery());
        assertEquals(List.of(ContentKind.HELP,ContentKind.GUIDE),input.getKinds());
    }

    @Test
    public void
    surveyRequestBindsSurveyInputWithoutTypeProperty()
        throws Exception
    {
        String json =
            "{\"input\":{\"source\":\"Survey.New\",\"command\":\"Submit\",\"destination\":\"SurveyView\"," +
            "\"answers\":{\"satisfied\":4,\"needs\":3,\"recommend\":5},\"comment\":\"Nice\"}}";

        SurveyInput input = mapper.readValue(json,HandleSurveyInputRequest.class).getInput();

        assertEquals("Survey.New->Submit->SurveyView",input.getKey());
        assertEquals(Map.of("satisfied",4,"needs",3,"recommend",5),input.getAnswers());
        assertEquals("Nice",input.getComment());
    }

    @Test
    public void
    sessionsReplyRoundTrips()
        throws Exception
    {
        GetSessionsOutputReply reply =
            new GetSessionsOutputReply()
                .setSuccess(true)
                .setOutput(
                    new SessionsOutput()
                        .setSessions(
                            List.of(
                                new SessionRowData()
                                    .setSessionId(12L)
                                    .setUserId(7L)
                                    .setUserName("amy")
                                    .setLoginMethod("OKTA")
                                    .setLastActivity("2026-11-01T12:00:00Z")
                                    .setValues(Map.of("cart",3))))
                        .setCurrentSessionId(11L)
                        .setFailureMessages(List.of("13: not ended")));

        String                 json   = mapper.writeValueAsString(reply);
        GetSessionsOutputReply actual = mapper.readValue(json,GetSessionsOutputReply.class);

        assertEquals("amy",actual.getOutput().getSessions().get(0).getUserName());
        assertEquals("OKTA",actual.getOutput().getSessions().get(0).getLoginMethod());
        assertEquals("2026-11-01T12:00:00Z",actual.getOutput().getSessions().get(0).getLastActivity());
        assertEquals(3,actual.getOutput().getSessions().get(0).getValues().get("cart"));
        assertEquals(11L,actual.getOutput().getCurrentSessionId());
        assertEquals(List.of("13: not ended"),actual.getOutput().getFailureMessages());
        assertFalse(json.contains("@class"));
        assertFalse(json.contains("@type"));
    }

    @Test
    public void
    preferencesReplyRoundTrips()
        throws Exception
    {
        GetPreferencesOutputReply reply =
            new GetPreferencesOutputReply()
                .setSuccess(true)
                .setOutput(
                    new PreferencesOutput()
                        .setValues(Map.of("granite.theme","dark","compact",true,"size",3))
                        .setDefinitions(
                            List.of(
                                new PreferenceDefinitionData()
                                    .setKey("granite.theme")
                                    .setType("ENUM")
                                    .setDefaultValue("light")
                                    .setAllowedValues(List.of("light","dark"))))
                        .setFailureMessages(List.of("bad")));

        String            json   = mapper.writeValueAsString(reply);
        PreferencesOutput actual = mapper.readValue(json,GetPreferencesOutputReply.class).getOutput();

        assertTrue(json.contains("\"allowedValues\":[\"light\",\"dark\"]"),json);
        assertTrue(json.contains("\"type\":\"ENUM\""),json);
        assertEquals("dark",actual.getValues().get("granite.theme"));
        assertEquals(true,actual.getValues().get("compact"));
        assertEquals(3,actual.getValues().get("size"));
        assertEquals("light",actual.getDefinitions().get(0).getDefaultValue());
        assertEquals(List.of("light","dark"),actual.getDefinitions().get(0).getAllowedValues());
        assertEquals(List.of("bad"),actual.getFailureMessages());
    }

    @Test
    public void
    preferencesRequestBindsPreferencesInput()
        throws Exception
    {
        PreferencesInput input =
            mapper
                .readValue(
                    "{\"input\":{\"source\":\"Preferences.Current\",\"command\":\"Save\",\"destination\":\"PreferencesView\","
                        + "\"values\":{\"compact\":true}}}",
                    HandlePreferencesInputRequest.class)
                .getInput();

        assertEquals("Preferences.Current->Save->PreferencesView",input.getKey());
        assertEquals(Map.of("compact",true),input.getValues());
    }

    @Test
    public void
    sessionRequestBindsSessionInput()
        throws Exception
    {
        SessionInput login    = mapper.readValue(LOGIN_JSON,HandleSessionInputRequest.class).getInput();
        SessionInput complete = mapper.readValue(COMPLETE_JSON,HandleSessionInputRequest.class).getInput();

        assertEquals("Session.New->Login->LoginView",login.getKey());
        assertEquals("amy",login.getUserName());
        assertEquals("pw",login.getPassword());
        assertNull(login.getUserId());
        assertEquals("the-code",complete.getCode());
        assertEquals("the-state",complete.getState());
    }

    @Test
    public void
    sessionInputNeverWritesPasswordOrCode()
        throws Exception
    {
        HandleSessionInputRequest request = mapper.readValue(LOGIN_JSON,HandleSessionInputRequest.class);
        SessionInput              input   = request.getInput().setCode("the-code");
        String                    json    = mapper.writeValueAsString(new HandleSessionInputReply(request));

        assertFalse(json.contains("pw"));
        assertFalse(json.contains("the-code"));
        assertFalse(input.toString().contains("pw"));
        assertFalse(input.toString().contains("the-code"));
        assertTrue(json.contains("amy"));
    }

    @Test
    public void
    sessionReplyRoundTrips()
        throws Exception
    {
        GetSessionOutputReply reply =
            new GetSessionOutputReply()
                .setSuccess(true)
                .setOutput(
                    new SessionOutput()
                        .setStatus("AUTHENTICATED")
                        .setUserName("amy")
                        .setAuthorities(Set.of("ROLE_USER"))
                        .setPasswordEnabled(true)
                        .setRedirectUrl("https://okta/authorize"));

        SessionOutput actual = mapper.readValue(mapper.writeValueAsString(reply),GetSessionOutputReply.class).getOutput();

        assertEquals("AUTHENTICATED",actual.getStatus());
        assertEquals("amy",actual.getUserName());
        assertEquals(Set.of("ROLE_USER"),actual.getAuthorities());
        assertTrue(actual.isPasswordEnabled());
        assertFalse(actual.isOktaEnabled());
        assertEquals("https://okta/authorize",actual.getRedirectUrl());
        assertFalse(actual.isLoggedOut());
        assertFalse(mapper.writeValueAsString(reply).contains("endSessionUrl"));
    }
}

//////////////////////////////////////////////////////////////////////////////
