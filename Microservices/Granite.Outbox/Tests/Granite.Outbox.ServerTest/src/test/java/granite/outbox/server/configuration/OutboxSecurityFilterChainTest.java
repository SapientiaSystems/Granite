//////////////////////////////////////////////////////////////////////////////
// OutboxSecurityFilterChainTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.outbox.server.configuration;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.Filter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class OutboxSecurityFilterChainTest
{
    private static final String PROTECTED_PATH = "/outbox-service/get-entries";

    private static ConfigurableApplicationContext context;
    private static Filter                         securityFilterChain;
    private static KeyPair                        signingKeys;

    @BeforeAll
    public static void
    initialize()
    {
        AnnotationConfigApplicationContext created = new AnnotationConfigApplicationContext();

        created
            .getEnvironment()
            .getPropertySources()
            .addFirst(new MapPropertySource("test",Map.of("strata.health.base-path","/outbox-service/health")));
        created.register(SecuredOutboxSecurityConfiguration.class);
        created.refresh();

        context             = created;
        securityFilterChain = context.getBean("springSecurityFilterChain",Filter.class);
        signingKeys         = context.getBean(KeyPair.class);
    }

    @AfterAll
    public static void
    terminate()
    {
        context.close();
    }

    @Test
    public void
    protectedEndpointRejectsMissingToken()
        throws Exception
    {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain         chain    = run(request("POST",PROTECTED_PATH,null),response,new AtomicReference<>());

        assertEquals(401,response.getStatus());
        assertTrue(response.getHeader("WWW-Authenticate").startsWith("Bearer"));
        assertNull(chain.getRequest(),"request must not reach the controller");
    }

    @Test
    public void
    protectedEndpointRejectsUntrustedToken()
        throws Exception
    {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain         chain    =
            run(
                request("POST",PROTECTED_PATH,sign(generator.generateKeyPair(),"granite-services",300)),
                response,
                new AtomicReference<>());

        assertEquals(401,response.getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    public void
    protectedEndpointRejectsExpiredOrWrongAudienceToken()
        throws Exception
    {
        MockHttpServletResponse expired  = new MockHttpServletResponse();
        MockHttpServletResponse audience = new MockHttpServletResponse();

        run(request("POST",PROTECTED_PATH,sign(signingKeys,"granite-services",-300)),expired,new AtomicReference<>());
        run(request("POST",PROTECTED_PATH,sign(signingKeys,"other-services",300)),audience,new AtomicReference<>());

        assertEquals(401,expired.getStatus());
        assertEquals(401,audience.getStatus());
    }

    @Test
    public void
    protectedEndpointAcceptsValidTokenAndMapsAuthorities()
        throws Exception
    {
        MockHttpServletResponse         response       = new MockHttpServletResponse();
        AtomicReference<Authentication> authentication = new AtomicReference<>();
        MockFilterChain                 chain          =
            run(
                request("POST",PROTECTED_PATH,sign(signingKeys,"granite-services",300)),
                response,
                authentication);

        assertNotNull(chain.getRequest(),"request should reach the controller");
        assertTrue(response.getStatus() != 401 && response.getStatus() != 403,"status " + response.getStatus());
        assertEquals("1001",authentication.get().getName());
        assertEquals(
            Set.of("ROLE_ADMIN","SCOPE_internal"),
            authentication
                .get()
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet()));
    }

    @Test
    public void
    validTokenWithoutRoleAuthoritiesReachesServicePath()
        throws Exception
    {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain         chain    =
            run(
                request("POST",PROTECTED_PATH,sign(signingKeys,"granite-services",300,List.of())),
                response,
                new AtomicReference<>());

        assertNotNull(chain.getRequest());
        assertTrue(response.getStatus() != 401 && response.getStatus() != 403);
    }

    @Test
    public void
    healthAndPreflightArePublic()
        throws Exception
    {
        MockFilterChain health    =
            run(request("GET","/outbox-service/health/check",null),new MockHttpServletResponse(),new AtomicReference<>());
        MockFilterChain preflight =
            run(request("OPTIONS",PROTECTED_PATH,null),new MockHttpServletResponse(),new AtomicReference<>());

        assertNotNull(health.getRequest());
        assertNotNull(preflight.getRequest());
    }

    private static MockHttpServletRequest
    request(String method,String path,String token)
    {
        MockHttpServletRequest request = new MockHttpServletRequest(method,path);

        request.setServletPath(path);

        if (token != null)
            request.addHeader("Authorization","Bearer " + token);

        return request;
    }

    private static MockFilterChain
    run(
        MockHttpServletRequest          request,
        MockHttpServletResponse         response,
        AtomicReference<Authentication> authentication)
        throws ServletException,IOException
    {
        MockFilterChain chain =
            new MockFilterChain(
                new HttpServlet() {},
                (req,res,next) ->
                {
                    authentication.set(SecurityContextHolder.getContext().getAuthentication());
                    next.doFilter(req,res);
                });

        securityFilterChain.doFilter(request,response,chain);
        SecurityContextHolder.clearContext();
        return chain;
    }

    private static String
    sign(KeyPair keys,String audience,long lifetimeSeconds)
        throws JOSEException
    {
        return sign(keys,audience,lifetimeSeconds,List.of("ROLE_ADMIN","SCOPE_internal"));
    }

    private static String
    sign(KeyPair keys,String audience,long lifetimeSeconds,List<String> authorities)
        throws JOSEException
    {
        Instant      now    = Instant.now();
        JWTClaimsSet claims =
            new JWTClaimsSet.Builder()
                .issuer("granite-session")
                .subject("1001")
                .jwtID("2002")
                .audience(audience)
                .issueTime(Date.from(now.minusSeconds(600)))
                .expirationTime(Date.from(now.plusSeconds(lifetimeSeconds)))
                .claim("authorities",authorities)
                .build();
        SignedJWT    jwt    = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256),claims);

        jwt.sign(new RSASSASigner((RSAPrivateKey)keys.getPrivate()));
        return jwt.serialize();
    }
}

//////////////////////////////////////////////////////////////////////////////
