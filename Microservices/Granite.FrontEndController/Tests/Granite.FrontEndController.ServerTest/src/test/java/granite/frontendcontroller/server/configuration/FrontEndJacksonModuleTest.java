//////////////////////////////////////////////////////////////////////////////
// FrontEndJacksonModuleTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import granite.frontendcontroller.service.requestreply.CompleteOktaLoginInput;
import granite.frontendcontroller.service.requestreply.FrontEndJacksonModule;
import granite.frontendcontroller.service.requestreply.LoginInput;
import granite.frontendcontroller.service.requestreply.LoginMethodsOutput;
import granite.frontendcontroller.service.requestreply.LogoutInput;
import granite.frontendcontroller.service.requestreply.LogoutOutput;
import granite.frontendcontroller.service.requestreply.RedirectOutput;
import granite.frontendcontroller.service.requestreply.SessionOutput;
import granite.frontendcontroller.service.requestreply.StartOktaLoginInput;
import granite.frontendcontroller.service.requestreply.UsersInput;
import granite.frontendcontroller.service.requestreply.UsersOutput;
import granite.user.service.requestreply.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import strata.foundation.core.mapper.ObjectMapperSupplier;
import strata.frontendcontroller.service.requestreply.FrontEndInput;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@Tag("CommitStage")
public
class FrontEndJacksonModuleTest
{
    private static final String SAVE_JSON =
        "{\"userId\":7,\"source\":\"User.All\",\"command\":\"Save\",\"destination\":\"UsersEditableView\","
            + "\"usersToCreate\":[{\"userName\":\"amy\",\"password\":\"pw\",\"authorities\":[\"ROLE_AUDITOR\"],"
            + "\"values\":{\"theme\":\"dark\"}}],\"usersToUpdate\":[],\"userIdsToDelete\":[]}";

    private static final String DELETE_JSON =
        "{\"userId\":7,\"source\":\"User.All\",\"command\":\"Save\",\"destination\":\"UsersEditableView\","
            + "\"usersToCreate\":[],\"usersToUpdate\":[],\"userIdsToDelete\":[3]}";

    private ObjectMapper mapper;

    @BeforeEach
    public void
    setUp()
    {
        mapper = new ObjectMapperSupplier().get().registerModule(new FrontEndJacksonModule());
    }

    @Test
    public void
    saveShapedJsonDeserializesAsUsersInput()
        throws Exception
    {
        UsersInput input = assertInstanceOf(UsersInput.class,mapper.readValue(SAVE_JSON,FrontEndInput.class));

        assertEquals("User.All->Save->UsersEditableView",input.getKey());
        assertEquals(1,input.getUsersToCreate().size());
        assertEquals("amy",input.getUsersToCreate().get(0).getUserName());
        assertEquals(Map.of("theme","dark"),input.getUsersToCreate().get(0).getValues());
        assertEquals(List.of(),input.getUsersToUpdate());
        assertEquals(List.of(),input.getUserIdsToDelete());
    }

    @Test
    public void
    deleteShapedJsonDeserializesAsUsersInput()
        throws Exception
    {
        UsersInput input = assertInstanceOf(UsersInput.class,mapper.readValue(DELETE_JSON,FrontEndInput.class));

        assertEquals(List.of(),input.getUsersToCreate());
        assertEquals(List.of(),input.getUsersToUpdate());
        assertEquals(List.of(3L),input.getUserIdsToDelete());
    }

    @Test
    public void
    handleInputRequestBodyDeserializes()
        throws Exception
    {
        HandleInputRequest request = mapper.readValue("{\"input\":" + SAVE_JSON + "}",HandleInputRequest.class);

        assertInstanceOf(UsersInput.class,request.getInput());
    }

    @Test
    public void
    usersOutputRoundTrips()
        throws Exception
    {
        UsersOutput output =
            new UsersOutput()
                .setUsers(
                    List.of(
                        new UserData()
                            .setUserId(1L)
                            .setUserName("amy")
                            .setAuthorities(Set.of("ROLE_AUDITOR"))
                            .setValues(Map.of("theme","dark"))))
                .setFailureMessages(List.of("bad user"));

        output.setSource("User.All").setCommand("Save").setDestination("UsersEditableView");

        String json = mapper.writeValueAsString(output);

        UsersOutput actual = assertInstanceOf(UsersOutput.class,mapper.readValue(json,FrontEndOutput.class));

        assertEquals(1,actual.getUsers().size());
        assertEquals("amy",actual.getUsers().get(0).getUserName());
        assertEquals(Map.of("theme","dark"),actual.getUsers().get(0).getValues());
        assertEquals(List.of("bad user"),actual.getFailureMessages());
        assertFalse(json.contains("@class"));
        assertFalse(json.contains("@type"));
    }

    private static final String BASE =
        "\"userId\":0,\"source\":\"Session.New\",\"command\":\"X\",\"destination\":\"LoginView\"";

    @Test
    public void
    sessionInputsDeduceTheirOwnTypes()
        throws Exception
    {
        LoginInput login =
            assertInstanceOf(
                LoginInput.class,
                mapper.readValue("{" + BASE + ",\"userName\":\"amy\",\"password\":\"pw\"}",FrontEndInput.class));

        assertEquals("amy",login.getUserName());
        assertEquals("pw",login.getPassword());
        assertFalse(login.toString().contains("pw"));

        assertTrue(
            assertInstanceOf(
                LogoutInput.class,
                mapper.readValue("{" + BASE + ",\"logout\":true}",FrontEndInput.class)).isLogout());
        assertEquals(
            "/home",
            assertInstanceOf(
                StartOktaLoginInput.class,
                mapper.readValue("{" + BASE + ",\"returnTo\":\"/home\"}",FrontEndInput.class)).getReturnTo());

        CompleteOktaLoginInput complete =
            assertInstanceOf(
                CompleteOktaLoginInput.class,
                mapper.readValue("{" + BASE + ",\"code\":\"c\",\"state\":\"s\"}",FrontEndInput.class));

        assertEquals("c",complete.getCode());
        assertEquals("s",complete.getState());
    }

    @Test
    public void
    sessionOutputsRoundTripToTheirOwnTypes()
        throws Exception
    {
        LoginMethodsOutput methods =
            assertInstanceOf(
                LoginMethodsOutput.class,
                roundTrip(new LoginMethodsOutput().setPasswordEnabled(true).setOktaEnabled(false)));

        assertTrue(methods.isPasswordEnabled());
        assertFalse(methods.isOktaEnabled());

        SessionOutput session =
            assertInstanceOf(
                SessionOutput.class,
                roundTrip(new SessionOutput().setUserName("amy").setAuthorities(Set.of("ROLE_ADMIN")).setStatus("AUTHENTICATED")));

        assertEquals("amy",session.getUserName());
        assertEquals(Set.of("ROLE_ADMIN"),session.getAuthorities());
        assertEquals("AUTHENTICATED",session.getStatus());
        assertEquals(Set.of(),assertInstanceOf(SessionOutput.class,roundTrip(new SessionOutput())).getAuthorities());

        LogoutOutput logout = assertInstanceOf(LogoutOutput.class,roundTrip(new LogoutOutput().setLoggedOut(true)));

        assertTrue(logout.isLoggedOut());
        assertNull(logout.getEndSessionUrl());
        assertEquals("https://x",assertInstanceOf(RedirectOutput.class,roundTrip(new RedirectOutput().setUrl("https://x"))).getUrl());
    }

    @Test
    public void
    loginInputPasswordIsNeverSerialized()
        throws Exception
    {
        String json = mapper.writeValueAsString(new LoginInput().setUserName("amy").setPassword("pw-secret"));

        assertFalse(json.contains("pw-secret"));
        assertFalse(json.contains("password"));
    }

    private FrontEndOutput
    roundTrip(FrontEndOutput output)
        throws Exception
    {
        return mapper.readValue(mapper.writeValueAsString(output),FrontEndOutput.class);
    }

    @Test
    public void
    configurationsExposeModuleBean()
    {
        assertInstanceOf(FrontEndJacksonModule.class,new GraniteUserFrontEndControllerConfiguration().frontEndJacksonModule());
        assertInstanceOf(FrontEndJacksonModule.class,new GraniteAdminFrontEndControllerConfiguration().frontEndJacksonModule());
    }
}

//////////////////////////////////////////////////////////////////////////////
