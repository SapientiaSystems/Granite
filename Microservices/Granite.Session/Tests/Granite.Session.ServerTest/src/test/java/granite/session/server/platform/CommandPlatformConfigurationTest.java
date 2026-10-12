//////////////////////////////////////////////////////////////////////////////
// CommandPlatformConfigurationTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.platform;

import granite.session.server.application.Oauth2ProviderConfig;
import granite.session.server.application.Oauth2ProvidersProperties;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class CommandPlatformConfigurationTest
{
    private static final String PRIVATE_KEY = "classpath:jwt-test-private-key.pem";
    private static final String PUBLIC_KEY  = "classpath:jwt-test-public-key.pem";

    private final CommandPlatformConfiguration subject =
        new CommandPlatformConfiguration();

    @Test
    public void
    devProfileLoadsClasspathKeys()
    {
        MockEnvironment env = new MockEnvironment();

        env.setActiveProfiles("dev");
        assertNotNull(newJwtTokenService(env,PRIVATE_KEY,PUBLIC_KEY));
    }

    @Test
    public void
    audienceBindsFromCommaSeparatedList()
    {
        MockEnvironment env = new MockEnvironment();

        env.setActiveProfiles("dev");
        env.setProperty("granite.session.jwt.audience","a,b");
        assertNotNull(newJwtTokenService(env,PRIVATE_KEY,PUBLIC_KEY));
    }

    @Test
    public void
    noProfileRejectsClasspathKeys()
    {
        IllegalStateException e =
            assertThrows(
                IllegalStateException.class,
                () ->
                    newJwtTokenService(
                        new MockEnvironment(),PRIVATE_KEY,PUBLIC_KEY));

        assertTrue(e.getMessage().contains("dev"));
    }

    @Test
    public void
    testAndProductionProfilesRejectClasspathKeys()
    {
        for (String profile : new String[] {"test","production"})
        {
            MockEnvironment env = new MockEnvironment();

            env.setActiveProfiles(profile);
            assertThrows(
                IllegalStateException.class,
                () -> newJwtTokenService(env,PRIVATE_KEY,PUBLIC_KEY));
        }
    }

    @Test
    public void
    devProfileCombinedWithProductionRejectsClasspathKeys()
    {
        MockEnvironment env = new MockEnvironment();

        env.setActiveProfiles("dev","production");
        assertThrows(
            IllegalStateException.class,
            () -> newJwtTokenService(env,PRIVATE_KEY,PUBLIC_KEY));
    }

    @Test
    public void
    nonDevProfileRejectsClasspathPublicKeyEvenWhenPrivateKeyIsExternal()
    {
        MockEnvironment env = new MockEnvironment();

        env.setActiveProfiles("production");
        assertThrows(
            IllegalStateException.class,
            () ->
                newJwtTokenService(
                    env,"file:/mnt/secrets/jwt-private-key.pem",PUBLIC_KEY));
    }

    @Test
    public void
    failsClosedWhenUnprefixedClasspathPathInProduction()
    {
        MockEnvironment env = new MockEnvironment();

        env.setActiveProfiles("production");
        assertThrows(
            IllegalStateException.class,
            () ->
                newJwtTokenService(
                    env,"jwt-test-private-key.pem","jwt-test-public-key.pem"));
    }

    @Test
    public void
    failsClosedWhenClasspathStarColonPrefixInTest()
    {
        MockEnvironment env = new MockEnvironment();

        env.setActiveProfiles("test");
        assertThrows(
            IllegalStateException.class,
            () ->
                newJwtTokenService(
                    env,"classpath*:jwt-test-private-key.pem","classpath*:jwt-test-public-key.pem"));
    }

    @Test
    public void
    oauth2ProviderConfigFieldsAreValidated()
    {
        jakarta.validation.Validator validator =
            jakarta.validation.Validation
                .buildDefaultValidatorFactory()
                .getValidator();
        Oauth2ProvidersProperties properties =
            new Oauth2ProvidersProperties()
                .setProviders(Map.of("google",new Oauth2ProviderConfig()));

        assertTrue(validator.validate(properties).size() >= 3);
    }

    private Object
    newJwtTokenService(MockEnvironment env,String privateKey,String publicKey)
    {
        return
            subject.jwtTokenService(
                env,
                new DefaultResourceLoader(),
                privateKey,
                publicKey,
                "granite-session",
                "PT15M");
    }
}

//////////////////////////////////////////////////////////////////////////////
