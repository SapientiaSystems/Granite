//////////////////////////////////////////////////////////////////////////////
// SessionJwtValidatorTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.client.event;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.session.service.requestreply.FindSessionsByKeyReply;
import granite.session.service.requestreply.FindSessionsByKeyRequest;
import granite.session.service.requestreply.SessionData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class SessionJwtValidatorTest
{
    private static final long USER_ID    = 42L;
    private static final long SESSION_ID = 7L;

    private RSAPrivateKey              privateKey;
    private RSAPublicKey               publicKey;
    private ISessionQueryServiceClient client;
    private SessionJwtValidator        validator;

    @BeforeEach
    public void
    setUp()
        throws Exception
    {
        KeyPair pair = newKeyPair();

        privateKey = (RSAPrivateKey)pair.getPrivate();
        publicKey  = (RSAPublicKey)pair.getPublic();
        client     = Mockito.mock(ISessionQueryServiceClient.class);
        validator  = new SessionJwtValidator(publicKey,client,Clock.systemUTC());
    }

    @Test
    public void
    validReturnsUserId()
        throws Exception
    {
        stubSessions(List.of(session(Instant.now().plus(Duration.ofHours(1)))));
        assertEquals(
            Optional.of(USER_ID),
            validator.validate(mint(privateKey,Duration.ofHours(1))));
    }

    @Test
    public void
    expiredJwtReturnsEmpty()
        throws Exception
    {
        stubSessions(List.of(session(Instant.now().plus(Duration.ofHours(1)))));
        assertTrue(validator.validate(mint(privateKey,Duration.ofHours(-1))).isEmpty());
    }

    @Test
    public void
    tamperedJwtReturnsEmpty()
        throws Exception
    {
        RSAPrivateKey other = (RSAPrivateKey)newKeyPair().getPrivate();

        stubSessions(List.of(session(Instant.now().plus(Duration.ofHours(1)))));
        assertTrue(validator.validate(mint(other,Duration.ofHours(1))).isEmpty());
    }

    @Test
    public void
    jwtWithRevokedSessionReturnsEmpty()
        throws Exception
    {
        stubSessions(List.of());
        assertTrue(validator.validate(mint(privateKey,Duration.ofHours(1))).isEmpty());
    }

    @Test
    public void
    jwtWithSessionExpiresInPastReturnsEmpty()
        throws Exception
    {
        stubSessions(List.of(session(Instant.now().minus(Duration.ofMinutes(5)))));
        assertTrue(validator.validate(mint(privateKey,Duration.ofHours(1))).isEmpty());
    }

    @Test
    public void
    jwtWithClosedSessionReturnsEmpty()
        throws Exception
    {
        stubSessions(
            List.of(
                session(Instant.now().plus(Duration.ofHours(1)))
                    .setClosedReason(granite.session.service.requestreply.SessionClosedReason.REPLACED)));
        assertTrue(validator.validate(mint(privateKey,Duration.ofHours(1))).isEmpty());
    }

    private KeyPair
    newKeyPair()
        throws Exception
    {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private void
    stubSessions(List<SessionData> sessions)
    {
        FindSessionsByKeyReply reply = new FindSessionsByKeyReply().setSessions(sessions);

        reply.setSuccess(true);
        Mockito
            .when(client.findSessionsByKey(Mockito.any(FindSessionsByKeyRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(reply));
    }

    private SessionData
    session(Instant expires)
    {
        return new SessionData().setSessionId(SESSION_ID).setUserId(USER_ID).setExpires(expires);
    }

    private String
    mint(RSAPrivateKey key,Duration ttl)
        throws Exception
    {
        Instant      now    = Instant.now();
        JWTClaimsSet claims =
            new JWTClaimsSet.Builder()
                .subject(String.valueOf(USER_ID))
                .jwtID(String.valueOf(SESSION_ID))
                .issueTime(Date.from(now.minus(Duration.ofHours(2))))
                .expirationTime(Date.from(now.plus(ttl)))
                .build();
        SignedJWT    jwt    = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256),claims);

        jwt.sign(new RSASSASigner(key));
        return jwt.serialize();
    }
}

//////////////////////////////////////////////////////////////////////////////
