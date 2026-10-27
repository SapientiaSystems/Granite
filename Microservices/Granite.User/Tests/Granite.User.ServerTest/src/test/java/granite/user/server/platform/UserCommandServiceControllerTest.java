//////////////////////////////////////////////////////////////////////////////
// UserCommandServiceControllerTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.platform;

import granite.user.service.requestreply.IUserCommandService;
import granite.user.service.requestreply.MergeUserValuesRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class UserCommandServiceControllerTest
{
    @Test
    public void
    everyCommandOperationHasAPostMapping()
    {
        for (Method operation : IUserCommandService.class.getMethods())
        {
            Optional<Method> handler =
                Arrays
                    .stream(UserCommandServiceController.class.getMethods())
                    .filter(m -> m.getName().equals(operation.getName()))
                    .findFirst();

            assertTrue(handler.isPresent(),"no controller method for " + operation.getName());

            PostMapping mapping = handler.get().getAnnotation(PostMapping.class);

            assertTrue(mapping != null,"no @PostMapping on " + operation.getName());
            assertArrayEquals(new String[] {"/" + toPath(operation.getName())},mapping.value(),operation.getName());
        }
    }

    @Test
    public void
    mergeUserValuesMapsToMergeUserValuesPath() throws NoSuchMethodException
    {
        Method handler =
            UserCommandServiceController.class.getMethod("mergeUserValues",MergeUserValuesRequest.class);

        assertArrayEquals(
            new String[] {"/merge-user-values"},
            handler.getAnnotation(PostMapping.class).value());
    }

    private String
    toPath(String name)
    {
        return name.replaceAll("([a-z])([A-Z])","$1-$2").toLowerCase();
    }
}

//////////////////////////////////////////////////////////////////////////////
