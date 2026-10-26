//////////////////////////////////////////////////////////////////////////////
// FrontEndJsonSerializationTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import granite.frontendcontroller.service.requestreply.GetSessionOutputReply;
import granite.frontendcontroller.service.requestreply.GetUsersOutputReply;
import granite.frontendcontroller.service.requestreply.HandleSessionInputReply;
import granite.frontendcontroller.service.requestreply.HandleSessionInputRequest;
import granite.frontendcontroller.service.requestreply.HandleUsersInputRequest;
import granite.frontendcontroller.service.requestreply.SessionInput;
import granite.frontendcontroller.service.requestreply.SessionOutput;
import granite.frontendcontroller.service.requestreply.UsersInput;
import granite.frontendcontroller.service.requestreply.UsersOutput;
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
        assertNull(actual.getEndSessionUrl());
    }
}

//////////////////////////////////////////////////////////////////////////////
