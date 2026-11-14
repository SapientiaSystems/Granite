//////////////////////////////////////////////////////////////////////////////
// ClientControllerMappingTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.platform;

import granite.client.service.requestreply.IClientCommandService;
import granite.client.service.requestreply.IClientQueryService;
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
class ClientControllerMappingTest
{
    @Test
    public void
    everyCommandOperationHasAPostMapping()
    {
        assertMapped(IClientCommandService.class,ClientCommandServiceController.class);
    }

    @Test
    public void
    everyQueryOperationHasAPostMapping()
    {
        assertMapped(IClientQueryService.class,ClientQueryServiceController.class);
    }

    private void
    assertMapped(Class<?> contract,Class<?> controller)
    {
        for (Method operation : contract.getMethods())
        {
            Optional<Method> handler =
                Arrays
                    .stream(controller.getMethods())
                    .filter(m -> m.getName().equals(operation.getName()))
                    .findFirst();

            assertTrue(handler.isPresent(),"no controller method for " + operation.getName());

            PostMapping mapping = handler.get().getAnnotation(PostMapping.class);

            assertTrue(mapping != null,"no @PostMapping on " + operation.getName());
            assertArrayEquals(
                new String[] {"/" + toPath(operation.getName())},
                mapping.value(),
                operation.getName());
        }
    }

    private String
    toPath(String name) { return name.replaceAll("([a-z])([A-Z])","$1-$2").toLowerCase(); }
}

//////////////////////////////////////////////////////////////////////////////
