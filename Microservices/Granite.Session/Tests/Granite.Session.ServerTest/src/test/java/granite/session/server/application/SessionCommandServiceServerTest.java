//////////////////////////////////////////////////////////////////////////////
// SessionCommandServiceServerTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import granite.session.service.requestreply.ISessionCommandService;
import granite.session.service.requestreply.LoginReply;
import granite.session.service.requestreply.LoginWithOauth2Reply;
import granite.session.service.requestreply.LoginWithOauth2Request;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.request.RequestContextHolder;
import strata.server.spring.inject.RequestAttributeMap;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
@ActiveProfiles("dev")
public
class SessionCommandServiceServerTest
    extends granite.session.service.requestreply.SessionCommandServiceTest
{
    @Autowired
    private ISessionCommandService subject;

    @Autowired
    private RSAKey signingKey;

    @Autowired
    private EntityManagerFactory factory;

    @Value("${granite.session.oauth2.providers.test.issuer-uri}")
    private String issuer;

    @Value("${granite.session.oauth2.providers.test.audience}")
    private String audience;

    @BeforeEach
    public void
    setUp()
    {
        RequestContextHolder.setRequestAttributes(new RequestAttributeMap());
    }

    @AfterEach
    public void
    tearDown()
    {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void
    testLoginWritesCreatedOutboxEvent()
    {
        LoginReply reply =
            subject
                .login(newLoginRequest(uniqueUserName(),uniqueDeviceId()))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertCreatedOutboxEvent(reply.getSessionData().getSessionId());
    }

    @Test
    public void
    testLoginWithOauth2WritesCreatedOutboxEvent()
    {
        LoginWithOauth2Reply reply =
            subject
                .loginWithOauth2(
                    new LoginWithOauth2Request()
                        .setProvider(oauth2Provider())
                        .setIdToken(oauth2IdToken())
                        .setDeviceId(uniqueDeviceId()))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertCreatedOutboxEvent(reply.getSessionData().getSessionId());
    }

    @Override
    protected ISessionCommandService
    getSubject() { return subject; }

    @Override
    protected String
    oauth2IdToken()
    {
        Instant      now    = Instant.now();
        JWTClaimsSet claims =
            new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .subject("external-" + UUID.randomUUID())
                .claim("email","external@example.com")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(300)))
                .build();

        try
        {
            SignedJWT signed =
                new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                        .keyID(signingKey.getKeyID())
                        .build(),
                    claims);

            signed.sign(new RSASSASigner(signingKey.toRSAPrivateKey()));
            return signed.serialize();
        }
        catch (JOSEException e)
        {
            throw new IllegalStateException("failed to sign test id token",e);
        }
    }

    private void
    assertCreatedOutboxEvent(Long sessionId)
    {
        EntityManager manager = factory.createEntityManager();

        try
        {
            List<String> payloads =
                manager
                    .createQuery(
                        "select e.eventPayload from OutboxEvent e where e.sourceId = :id",
                        String.class)
                    .setParameter("id",sessionId.toString())
                    .getResultList();

            assertTrue(
                payloads.stream().anyMatch(payload -> payload.contains("CREATED")),
                "no CREATED outbox event for session " + sessionId + ": " + payloads);
        }
        finally
        {
            manager.close();
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
