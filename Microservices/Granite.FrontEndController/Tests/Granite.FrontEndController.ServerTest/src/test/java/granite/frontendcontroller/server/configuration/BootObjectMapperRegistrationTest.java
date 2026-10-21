//////////////////////////////////////////////////////////////////////////////
// BootObjectMapperRegistrationTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import granite.frontendcontroller.service.requestreply.UsersInput;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@Tag("CommitStage")
public
class BootObjectMapperRegistrationTest
{
    private static final String REQUEST_JSON =
        "{\"input\":{\"userId\":7,\"source\":\"User.All\",\"command\":\"Save\",\"destination\":\"UsersEditableView\","
            + "\"usersToCreate\":[{\"userName\":\"amy\",\"password\":\"pw\",\"authorities\":[\"ROLE_AUDITOR\"],"
            + "\"values\":{\"theme\":\"dark\"}}],\"usersToUpdate\":[],\"userIdsToDelete\":[]}}";

    private final ApplicationContextRunner runner =
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .withBean(
                Module.class,
                () -> new GraniteUserFrontEndControllerConfiguration().frontEndJacksonModule());

    @Test
    public void
    bootObjectMapperDeducesUsersInput()
    {
        runner.run(
            context ->
            {
                ObjectMapper mapper = context.getBean(ObjectMapper.class);

                HandleInputRequest request = mapper.readValue(REQUEST_JSON,HandleInputRequest.class);

                UsersInput input = assertInstanceOf(UsersInput.class,request.getInput());

                assertEquals(1,input.getUsersToCreate().size());
                assertEquals("amy",input.getUsersToCreate().get(0).getUserName());
            });
    }
}

//////////////////////////////////////////////////////////////////////////////
