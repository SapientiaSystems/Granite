//////////////////////////////////////////////////////////////////////////////
// GraniteResourceServerSettingsTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.security;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("CommitStage")
public
class GraniteResourceServerSettingsTest
{
    private static final String MESSAGE =
        "granite.security.jwt.issuer and granite.security.jwt.audience must be set in production";

    @Test
    public void
    productionRequiresExplicitIssuerAndAudience()
    {
        for (String missing : new String[] {
            GraniteResourceServerSettings.ISSUER,
            GraniteResourceServerSettings.AUDIENCE})
        {
            Map<String,Object> properties = new HashMap<>();

            properties.put(GraniteResourceServerSettings.ISSUER,"poplar-session");
            properties.put(GraniteResourceServerSettings.AUDIENCE,"poplar-services");
            properties.remove(missing);

            IllegalStateException e =
                assertThrows(
                    IllegalStateException.class,
                    () -> GraniteResourceServerSettings.from(environment("production",properties)));

            assertEquals(MESSAGE,e.getMessage());
        }

        assertThrows(
            IllegalStateException.class,
            () -> GraniteResourceServerSettings.from(environment("production",Map.of())));
    }

    @Test
    public void
    productionAcceptsExplicitValues()
    {
        GraniteResourceServerSettings settings =
            GraniteResourceServerSettings.from(
                environment(
                    "production",
                    Map.of(
                        GraniteResourceServerSettings.ISSUER,"poplar-session",
                        GraniteResourceServerSettings.AUDIENCE,"poplar-services")));

        assertEquals("poplar-session",settings.getIssuer());
        assertEquals(List.of("poplar-services"),settings.getAudiences());
    }

    @Test
    public void
    nonProductionKeepsDefaults()
    {
        for (String profile : new String[] {"local","development","test"})
        {
            GraniteResourceServerSettings settings =
                GraniteResourceServerSettings.from(environment(profile,Map.of()));

            assertEquals(GraniteResourceServerSettings.DEFAULT_ISSUER,settings.getIssuer());
            assertEquals(List.of(GraniteResourceServerSettings.DEFAULT_AUDIENCE),settings.getAudiences());
        }
    }

    private static StandardEnvironment
    environment(String profile,Map<String,Object> properties)
    {
        StandardEnvironment environment = new StandardEnvironment();

        environment.setActiveProfiles(profile);
        environment.getPropertySources().addFirst(new MapPropertySource("test",properties));
        return environment;
    }
}

//////////////////////////////////////////////////////////////////////////////
