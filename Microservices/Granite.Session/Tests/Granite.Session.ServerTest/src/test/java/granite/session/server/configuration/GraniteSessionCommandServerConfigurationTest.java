//////////////////////////////////////////////////////////////////////////////
// GraniteSessionCommandServerConfigurationTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.configuration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import granite.client.client.requestreply.IClientCommandServiceClient;
import granite.client.service.requestreply.VerifyClientCredentialsRequest;
import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.foundation.core.security.GraniteJwtDecoderFactory;
import granite.foundation.core.security.GraniteResourceServerSettings;
import granite.session.server.application.IJwtTokenService;
import granite.session.server.application.IServiceTokenProvider;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.security.oauth2.jwt.Jwt;
import granite.session.server.application.CatalogRoleLoginGate;
import granite.session.server.application.IJwkSetProvider;
import granite.session.server.application.RequiredAuthoritiesLoginGate;
import granite.session.server.application.ILoginGate;
import granite.session.server.application.JwtTokenService;
import granite.session.server.application.IOktaClient;
import granite.session.server.application.OktaRoleAssignment;
import granite.session.server.application.OktaSettings;
import granite.session.server.application.OktaUnmappedGroups;
import granite.session.server.application.RefreshTokenService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.env.MockEnvironment;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class GraniteSessionCommandServerConfigurationTest
{
    private static final String PRIVATE_KEY = "classpath:jwt-test-private-key.pem";
    private static final String PUBLIC_KEY  = "classpath:jwt-test-public-key.pem";

    private final GraniteSessionCommandServerConfiguration subject =
        new GraniteSessionCommandServerConfiguration();

    @Test
    public void
    localAndDevelopmentLoadClasspathKeys()
    {
        for (String profile : new String[] {"local","development"})
        {
            MockEnvironment env = new MockEnvironment();

            env.setActiveProfiles(profile);
            assertNotNull(newJwtTokenService(env,PRIVATE_KEY,PUBLIC_KEY));
        }
    }

    @Test
    public void
    loginGateReadsRequiredAuthorities()
    {
        MockEnvironment env = new MockEnvironment();

        env.setProperty("granite.session.login.required-authorities","ROLE_ADMIN,ROLE_AUDITOR");

        ILoginGate gate = subject.loginGate(env,new GraniteApplicationRolesManagerSupplier().get());

        assertInstanceOf(RequiredAuthoritiesLoginGate.class,gate);
        assertFalse(gate.permits(Set.of("ROLE_USER")));
        assertTrue(gate.permits(Set.of("ROLE_AUDITOR")));
    }

    @Test
    public void
    loginGateAbsentUsesCatalogRoles()
    {
        ILoginGate gate = subject.loginGate(new MockEnvironment(),new GraniteApplicationRolesManagerSupplier().get());

        assertInstanceOf(CatalogRoleLoginGate.class,gate);
        assertTrue(gate.permits(Set.of("ROLE_USER")));
        assertFalse(gate.permits(Set.of("SCOPE_internal")));
        assertFalse(gate.permits(Set.of()));
    }

    @Test
    public void
    loginGateEmptyPropertyUsesCatalogRoles()
    {
        MockEnvironment env = new MockEnvironment();

        env.setProperty("granite.session.login.required-authorities","");
        assertInstanceOf(CatalogRoleLoginGate.class,subject.loginGate(env,new GraniteApplicationRolesManagerSupplier().get()));
    }

    @Test
    public void
    groupMappingCheckIdentifiesNonCatalogKeys()
    {
        OktaSettings settings =
            new OktaSettings()
                .setGroupMapping(
                    Map.of(
                        "GC-Admins","ROLE_ADMIN",
                        "GC-Bogus","ROLE_Nope",
                        "GC-Blank"," "));

        assertEquals(
            Set.of("GC-Bogus","GC-Blank"),
            Set.copyOf(subject.oktaGroupMappingCheck(settings,new GraniteApplicationRolesManagerSupplier().get()).getIgnoredGroups()));
    }

    @Test
    public void
    groupMappingCheckIsEmptyWhenAllMappingsAreCatalogRoles()
    {
        OktaSettings settings = new OktaSettings().setGroupMapping(Map.of("GC-Admins","ROLE_ADMIN"));

        assertTrue(subject.oktaGroupMappingCheck(settings,new GraniteApplicationRolesManagerSupplier().get()).getIgnoredGroups().isEmpty());
    }

    @Test
    public void
    requiredAuthoritiesCheckIdentifiesNonCatalogValues()
    {
        MockEnvironment env = new MockEnvironment();

        env.setProperty("granite.session.login.required-authorities","ROLE_ADMIN, ROLE_Nope ,SCOPE_internal,");

        assertEquals(
            List.of("ROLE_Nope","SCOPE_internal"),
            subject.requiredAuthoritiesCheck(env,new GraniteApplicationRolesManagerSupplier().get()).getRejectedAuthorities());
    }

    @Test
    public void
    requiredAuthoritiesCheckIsEmptyWhenAbsentOrAllCatalogRoles()
    {
        MockEnvironment env = new MockEnvironment();

        assertTrue(subject.requiredAuthoritiesCheck(env,new GraniteApplicationRolesManagerSupplier().get()).getRejectedAuthorities().isEmpty());

        env.setProperty("granite.session.login.required-authorities","ROLE_ADMIN,ROLE_AUDITOR");
        assertTrue(subject.requiredAuthoritiesCheck(env,new GraniteApplicationRolesManagerSupplier().get()).getRejectedAuthorities().isEmpty());
    }

    @Test
    public void
    refreshTokenServiceDefaultsToRefreshTokenService()
    {
        assertInstanceOf(RefreshTokenService.class,subject.refreshTokenService());
    }

    @Test
    public void
    audienceBindsFromCommaSeparatedList()
    {
        MockEnvironment env = new MockEnvironment();

        env.setActiveProfiles("local");
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

        assertTrue(e.getMessage().contains("local"));
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
    localCombinedWithProductionRejectsClasspathKeys()
    {
        MockEnvironment env = new MockEnvironment();

        env.setActiveProfiles("local","production");
        assertThrows(
            IllegalStateException.class,
            () -> newJwtTokenService(env,PRIVATE_KEY,PUBLIC_KEY));
    }

    @Test
    public void
    productionRejectsClasspathPublicKeyEvenWhenPrivateKeyIsExternal()
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
    jwkSetProviderPublishesKeyMatchingTokenKid()
    {
        MockEnvironment env = new MockEnvironment();

        env.setActiveProfiles("local");

        JwtTokenService minter   = (JwtTokenService)newJwtTokenService(env,PRIVATE_KEY,PUBLIC_KEY);
        IJwkSetProvider provider = subject.jwkSetProvider(env,new DefaultResourceLoader());

        assertEquals(List.of(minter.getKeyId()),provider.getKeyIds());
    }

    @Test
    public void
    jwkSetProviderIncludesPreviousPublicKey(@TempDir Path directory)
        throws Exception
    {
        MockEnvironment env = new MockEnvironment();

        env.setActiveProfiles("local");
        env.setProperty("granite.session.jwt.public-key-location",PUBLIC_KEY);
        env.setProperty(
            "granite.session.jwt.previous-public-key-location",
            writePublicKeyPem(directory).toUri().toString());

        assertEquals(2,subject.jwkSetProvider(env,new DefaultResourceLoader()).getKeyIds().size());
    }

    @Test
    public void
    productionRejectsClasspathPreviousPublicKey(@TempDir Path directory)
        throws Exception
    {
        MockEnvironment env = new MockEnvironment();

        env.setActiveProfiles("production");
        env.setProperty(
            "granite.session.jwt.public-key-location",
            writePublicKeyPem(directory).toUri().toString());
        env.setProperty("granite.session.jwt.previous-public-key-location",PUBLIC_KEY);

        IllegalStateException e =
            assertThrows(
                IllegalStateException.class,
                () -> subject.jwkSetProvider(env,new DefaultResourceLoader()));

        assertTrue(e.getMessage().contains("previous-public-key-location"));
    }

    @Test
    public void
    productionRequiresExplicitSessionIssuerAndAudience()
    {
        for (String missing : new String[] {"granite.session.jwt.issuer","granite.session.jwt.audience"})
        {
            MockEnvironment env = new MockEnvironment();

            env.setActiveProfiles("production");
            env.setProperty("granite.session.jwt.private-key-location",PRIVATE_KEY);
            env.setProperty("granite.session.jwt.public-key-location",PUBLIC_KEY);

            if (!missing.endsWith("issuer"))
                env.setProperty("granite.session.jwt.issuer","poplar-session");

            if (!missing.endsWith("audience"))
                env.setProperty("granite.session.jwt.audience","poplar-services");

            IllegalStateException e =
                assertThrows(
                    IllegalStateException.class,
                    () -> subject.jwtTokenService(env,new DefaultResourceLoader()));

            assertEquals(
                "granite.session.jwt.issuer and granite.session.jwt.audience must be set in production",
                e.getMessage());
        }
    }

    @Test
    public void
    productionAcceptsExplicitSessionIssuerAndAudience()
    {
        MockEnvironment env = new MockEnvironment();

        env.setActiveProfiles("production");
        env.setProperty("granite.session.jwt.private-key-location",PRIVATE_KEY);
        env.setProperty("granite.session.jwt.public-key-location",PUBLIC_KEY);
        env.setProperty("granite.session.jwt.issuer","poplar-session");
        env.setProperty("granite.session.jwt.audience","poplar-services");

        IllegalStateException e =
            assertThrows(
                IllegalStateException.class,
                () -> subject.jwtTokenService(env,new DefaultResourceLoader()));

        assertFalse(e.getMessage().contains("must be set in production"));
        assertTrue(e.getMessage().contains("local"));
    }

    @Test
    public void
    nonProductionKeepsSessionIssuerAndAudienceDefaults()
    {
        MockEnvironment env = new MockEnvironment();

        env.setActiveProfiles("local");
        env.setProperty("granite.session.jwt.private-key-location",PRIVATE_KEY);
        env.setProperty("granite.session.jwt.public-key-location",PUBLIC_KEY);

        assertNotNull(subject.jwtTokenService(env,new DefaultResourceLoader()));
    }

    @Test
    public void
    oktaSettingsBindFromTheEnvironment()
    {
        MockEnvironment env =
            new MockEnvironment()
                .withProperty("granite.session.login.okta.enabled","true")
                .withProperty("granite.session.okta.issuer","https://example.okta.com/oauth2/default")
                .withProperty("granite.session.okta.client-id","client-1")
                .withProperty("granite.session.okta.redirect-uri","https://app.example.com/login/callback")
                .withProperty("granite.session.okta.role-assignment","every_login")
                .withProperty("granite.session.okta.unmapped-groups","refuse")
                .withProperty("granite.session.okta.group-mapping[Granite Admins]","ROLE_ADMIN")
                .withProperty("granite.session.okta.group-mapping.auditors","ROLE_AUDITOR");
        OktaSettings    settings = subject.oktaSettings(env);

        assertTrue(settings.isEnabled());
        assertEquals("https://example.okta.com/oauth2/default",settings.getIssuer());
        assertEquals("client-1",settings.getClientId());
        assertEquals("https://app.example.com/login/callback",settings.getRedirectUri());
        assertEquals(OktaRoleAssignment.EVERY_LOGIN,settings.getRoleAssignment());
        assertEquals(OktaUnmappedGroups.REFUSE,settings.getUnmappedGroups());
        assertEquals(Map.of("Granite Admins","ROLE_ADMIN","auditors","ROLE_AUDITOR"),settings.getGroupMapping());
        assertEquals(java.time.Duration.ofMinutes(5),settings.getTransactionTtl());
    }

    @Test
    public void
    oktaIsDisabledByDefaultAndNeedsNoKeyStore()
    {
        MockEnvironment env      = new MockEnvironment();
        OktaSettings    settings = subject.oktaSettings(env);

        assertFalse(settings.isEnabled());
        assertEquals(OktaRoleAssignment.ON_CREATE,settings.getRoleAssignment());
        assertEquals(OktaUnmappedGroups.DEFAULT_ROLE,settings.getUnmappedGroups());
        assertInstanceOf(
            IOktaClient.class,
            subject.oktaClient(settings,env,new DefaultResourceLoader(),java.time.Clock.systemUTC()));
    }

    @Test
    public void
    clientCommandClientTargetsGatewayWithServiceToken()
    {
        WireMockServer gateway = new WireMockServer(wireMockConfig().dynamicPort());

        gateway.start();

        try
        {
            gateway.stubFor(
                post(urlEqualTo("/client-command-service/verify-client-credentials"))
                    .willReturn(okJson("{\"success\":true,\"valid\":true,\"clientId\":\"gc_x\"}")));

            MockEnvironment env = new MockEnvironment();

            env.setActiveProfiles("local");
            env.setProperty("granite.client-command-service.host",gateway.baseUrl());

            IJwtTokenService            tokens   = subject.jwtTokenService(env,new DefaultResourceLoader());
            IServiceTokenProvider       provider = subject.serviceTokenProvider(tokens,java.time.Clock.systemUTC(),env);
            IClientCommandServiceClient client   = subject.clientCommandServiceClient(noSslBundles(),env,provider);

            assertTrue(
                client
                    .verifyClientCredentials(
                        new VerifyClientCredentialsRequest().setClientId("gc_x").setClientSecret("s"))
                    .toCompletableFuture()
                    .join()
                    .isValid());

            List<LoggedRequest> requests = gateway.findAll(postRequestedFor(anyUrl()));

            assertEquals(1,requests.size());

            String header = requests.get(0).getHeader("Authorization");

            assertTrue(header != null && header.startsWith("Bearer "));

            Jwt jwt =
                new GraniteJwtDecoderFactory()
                    .create(
                        subject.jwkSetProvider(env,new DefaultResourceLoader()).getPublicJwkSet(),
                        GraniteResourceServerSettings.from(env))
                    .decode(header.substring("Bearer ".length()));

            assertEquals("service:granite-session",jwt.getSubject());
        }
        finally
        {
            gateway.stop();
        }
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<SslBundles>
    noSslBundles()
    {
        return Mockito.mock(ObjectProvider.class);
    }

    private Object
    newJwtTokenService(MockEnvironment env,String privateKey,String publicKey)
    {
        env.setProperty("granite.session.jwt.private-key-location",privateKey);
        env.setProperty("granite.session.jwt.public-key-location",publicKey);
        env.setProperty("granite.session.jwt.issuer","granite-session");
        env.setProperty("granite.session.jwt.ttl","PT15M");
        return subject.jwtTokenService(env,new DefaultResourceLoader());
    }

    private Path
    writePublicKeyPem(Path directory)
        throws Exception
    {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);

        String pem =
            "-----BEGIN PUBLIC KEY-----\n"
                + Base64
                    .getMimeEncoder(64,"\n".getBytes(StandardCharsets.US_ASCII))
                    .encodeToString(generator.generateKeyPair().getPublic().getEncoded())
                + "\n-----END PUBLIC KEY-----\n";

        return Files.writeString(directory.resolve("previous-public-key.pem"),pem);
    }
}

//////////////////////////////////////////////////////////////////////////////
