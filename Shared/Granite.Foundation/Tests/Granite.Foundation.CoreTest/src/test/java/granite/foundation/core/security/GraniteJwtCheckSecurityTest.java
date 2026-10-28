//////////////////////////////////////////////////////////////////////////////
// GraniteJwtCheckSecurityTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("CommitStage")
public
class GraniteJwtCheckSecurityTest
{
    private static final String ISSUER   = "granite-session";
    private static final String AUDIENCE = "granite-services";

    private static KeyPair trusted;
    private static KeyPair untrusted;
    private static RSAKey  jwk;

    private GenericWebApplicationContext context;

    @BeforeAll
    public static void
    generateKeys()
        throws Exception
    {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);
        trusted   = generator.generateKeyPair();
        untrusted = generator.generateKeyPair();
        jwk       =
            new RSAKey.Builder((RSAPublicKey)trusted.getPublic())
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyIDFromThumbprint()
                .build();
    }

    @AfterEach
    public void
    closeContext()
    {
        if (context != null)
            context.close();
    }

    @Test
    public void
    requiredAcceptsValidToken()
        throws Exception
    {
        mvc(JwtCheckMode.REQUIRED,new MockEnvironment())
            .perform(get("/api/ping").header("Authorization","Bearer " + sign(trusted,Duration.ofMinutes(5))))
            .andExpect(status().isOk())
            .andExpect(content().string("pong"));
    }

    @Test
    public void
    requiredRejectsExpiredToken()
        throws Exception
    {
        mvc(JwtCheckMode.REQUIRED,new MockEnvironment())
            .perform(get("/api/ping").header("Authorization","Bearer " + sign(trusted,Duration.ofMinutes(-5))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void
    requiredRejectsBadSignature()
        throws Exception
    {
        mvc(JwtCheckMode.REQUIRED,new MockEnvironment())
            .perform(get("/api/ping").header("Authorization","Bearer " + sign(untrusted,Duration.ofMinutes(5))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void
    requiredRejectsMissingToken()
        throws Exception
    {
        mvc(JwtCheckMode.REQUIRED,new MockEnvironment())
            .perform(get("/api/ping"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void
    requiredPermitsTokenlessPathWithoutToken()
        throws Exception
    {
        mvc(JwtCheckMode.REQUIRED,new MockEnvironment())
            .perform(get("/health/live"))
            .andExpect(status().isOk());
    }

    @Test
    public void
    requiredPermitsPreflightWithoutToken()
        throws Exception
    {
        int status =
            mvc(JwtCheckMode.REQUIRED,new MockEnvironment())
                .perform(options("/api/ping"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertTrue(status != 401 && status != 403);
    }

    @Test
    public void
    requiredDoesNotAuthorizeOnAuthorities()
        throws Exception
    {
        mvc(JwtCheckMode.REQUIRED,new MockEnvironment())
            .perform(get("/api/ping").header("Authorization","Bearer " + sign(trusted,Duration.ofMinutes(5),List.of())))
            .andExpect(status().isOk());
    }

    @Test
    public void
    optionalPermitsMissingToken()
        throws Exception
    {
        mvc(JwtCheckMode.OPTIONAL,new MockEnvironment())
            .perform(get("/api/ping"))
            .andExpect(status().isOk())
            .andExpect(content().string("pong"));
    }

    @Test
    public void
    optionalAcceptsValidToken()
        throws Exception
    {
        mvc(JwtCheckMode.OPTIONAL,new MockEnvironment())
            .perform(get("/api/ping").header("Authorization","Bearer " + sign(trusted,Duration.ofMinutes(5))))
            .andExpect(status().isOk());
    }

    @Test
    public void
    optionalRejectsInvalidToken()
        throws Exception
    {
        mvc(JwtCheckMode.OPTIONAL,new MockEnvironment())
            .perform(get("/api/ping").header("Authorization","Bearer " + sign(untrusted,Duration.ofMinutes(5))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void
    optionalRejectsExpiredToken()
        throws Exception
    {
        mvc(JwtCheckMode.OPTIONAL,new MockEnvironment())
            .perform(get("/api/ping").header("Authorization","Bearer " + sign(trusted,Duration.ofMinutes(-5))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void
    defaultTokenlessPathsHonourHealthBasePathAndProfile()
    {
        GraniteJwtCheckSecurity security = new GraniteJwtCheckSecurity();
        MockEnvironment         dev      = new MockEnvironment().withProperty("strata.health.base-path","/status");
        MockEnvironment         prod     = new MockEnvironment();

        prod.setActiveProfiles("production");

        List<String> devPaths  = security.defaultTokenlessPaths(dev);
        List<String> prodPaths = security.defaultTokenlessPaths(prod);

        assertTrue(
            devPaths.containsAll(
                List.of("/status/**","/error","/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html")));
        assertEquals(List.of("/health/**","/error"),prodPaths);
    }

    private MockMvc
    mvc(JwtCheckMode mode,MockEnvironment environment)
        throws Exception
    {
        JwtDecoder decoder =
            new GraniteJwtDecoderFactory()
                .create(
                    new JWKSet(jwk).toPublicJWKSet().toJSONObject(),
                    GraniteResourceServerSettings.from(new MockEnvironment()));

        context = new GenericWebApplicationContext(new MockServletContext());
        context.setEnvironment(environment);
        context.registerBean(JwtDecoder.class,() -> decoder);
        context.registerBean(JwtCheckMode.class,() -> mode);
        new AnnotatedBeanDefinitionReader(context).register(GraniteJwtCheckTestConfiguration.class);
        context.refresh();

        return
            MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain",Filter.class))
                .build();
    }

    private static String
    sign(KeyPair keys,Duration lifetime)
        throws JOSEException
    {
        return sign(keys,lifetime,List.of("ROLE_USER"));
    }

    private static String
    sign(KeyPair keys,Duration lifetime,List<String> authorities)
        throws JOSEException
    {
        Instant      now    = Instant.now();
        JWTClaimsSet claims =
            new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .subject("1001")
                .jwtID("2002")
                .audience(AUDIENCE)
                .issueTime(Date.from(now.minus(Duration.ofMinutes(10))))
                .expirationTime(Date.from(now.plus(lifetime)))
                .claim("authorities",authorities)
                .build();
        SignedJWT    jwt    =
            new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwk.getKeyID()).build(),
                claims);

        jwt.sign(new RSASSASigner((RSAPrivateKey)keys.getPrivate()));
        return jwt.serialize();
    }
}

//////////////////////////////////////////////////////////////////////////////
