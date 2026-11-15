//////////////////////////////////////////////////////////////////////////////
// ClientIdGeneratorTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.application;

import granite.client.server.configuration.GraniteClientCommandServerConfiguration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class ClientIdGeneratorTest
{
    @Test
    public void
    defaultPrefixIsGc()
    {
        ClientIdGenerator generator = new ClientIdGenerator(ClientIdGenerator.DEFAULT_PREFIX);

        assertEquals("gc_",generator.getPrefix());
        assertTrue(generator.newClientId().matches("gc_[a-z2-7]{32}"));
    }

    @Test
    public void
    configuredPrefixIsUsedForNewIds()
    {
        assertTrue(new ClientIdGenerator("pc_").newClientId().matches("pc_[a-z2-7]{32}"));
        assertTrue(new ClientIdGenerator("poplar1_").newClientId().matches("poplar1_[a-z2-7]{32}"));
    }

    @Test
    public void
    invalidPrefixesAreRefused()
    {
        for (String prefix : new String[] {null,"","pc","PC_","1c_","p-c_","poplarxyz_","pc__"})
            assertThrows(IllegalArgumentException.class,() -> new ClientIdGenerator(prefix),String.valueOf(prefix));
    }

    @Test
    public void
    configurationReadsTheIdPrefixProperty()
    {
        GraniteClientCommandServerConfiguration configuration = new GraniteClientCommandServerConfiguration();
        MockEnvironment                         environment   = new MockEnvironment();

        assertEquals("gc_",configuration.clientIdGenerator(environment).getPrefix());

        environment.setProperty("granite.client.id-prefix","pc_");
        assertEquals("pc_",configuration.clientIdGenerator(environment).getPrefix());

        environment.setProperty("granite.client.id-prefix","Bad");
        assertThrows(IllegalArgumentException.class,() -> configuration.clientIdGenerator(environment));
    }
}

//////////////////////////////////////////////////////////////////////////////
