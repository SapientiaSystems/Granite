//////////////////////////////////////////////////////////////////////////////
// ServiceTokenIssuerTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import granite.session.service.requestreply.IssueServiceTokenReply;
import granite.session.service.requestreply.IssueServiceTokenRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class ServiceTokenIssuerTest
{
    private static final String   CLIENT_ID = "granite-frontendcontroller";
    private static final String   SECRET    = "fec-secret-value-1";
    private static final String   TOKEN     = "minted.service.token";
    private static final Duration LIFETIME  = Duration.ofMinutes(7);
    private static final Instant  EXPIRES   = Instant.parse("2026-10-28T12:07:00Z");

    private IJwtTokenService   jwtTokenService;
    private ServiceTokenIssuer subject;

    @BeforeEach
    public void
    setUp()
        throws Exception
    {
        jwtTokenService = Mockito.mock(IJwtTokenService.class);
        subject         =
            new ServiceTokenIssuer(
                jwtTokenService,
                List.of(new ServiceClientRegistration(CLIENT_ID,hex(SECRET),LIFETIME)));

        Mockito
            .when(jwtTokenService.mintServiceToken(CLIENT_ID,List.of("SCOPE_internal"),LIFETIME))
            .thenReturn(TOKEN);
        Mockito.when(jwtTokenService.getExpiration(TOKEN)).thenReturn(EXPIRES);
    }

    @Test
    public void
    correctCredentialsIssueInternalToken()
    {
        IssueServiceTokenReply reply = subject.issue(request(CLIENT_ID,SECRET));

        assertTrue(reply.isSuccess());
        assertEquals(TOKEN,reply.getToken());
        assertEquals(EXPIRES,reply.getExpiresAt());
        Mockito
            .verify(jwtTokenService)
            .mintServiceToken(CLIENT_ID,List.of("SCOPE_internal"),LIFETIME);
    }

    @Test
    public void
    base64SecretHashIsAccepted()
        throws Exception
    {
        ServiceTokenIssuer issuer =
            new ServiceTokenIssuer(
                jwtTokenService,
                List.of(new ServiceClientRegistration(CLIENT_ID,Base64.getEncoder().encodeToString(sha256(SECRET)),LIFETIME)));

        assertTrue(issuer.issue(request(CLIENT_ID,SECRET)).isSuccess());
    }

    @Test
    public void
    wrongSecretIsRefusedAndNothingMinted()
    {
        assertRefused(subject.issue(request(CLIENT_ID,"wrong-secret")));
    }

    @Test
    public void
    unknownClientIsRefusedAndNothingMinted()
    {
        assertRefused(subject.issue(request("someone-else",SECRET)));
    }

    @Test
    public void
    missingCredentialsAreRefused()
    {
        assertRefused(subject.issue(request(null,SECRET)));
        assertRefused(subject.issue(request(CLIENT_ID,null)));
        assertRefused(subject.issue(null));
    }

    @Test
    public void
    emptyOrBlankSecretIsRefused()
    {
        assertRefused(subject.issue(request(CLIENT_ID,"")));
        assertRefused(subject.issue(request(CLIENT_ID,"   ")));
    }

    @Test
    public void
    registrationWithHashOfEmptySecretFailsNamingOnlyTheClient()
        throws Exception
    {
        IllegalArgumentException e =
            assertThrows(
                IllegalArgumentException.class,
                () -> new ServiceClientRegistration(CLIENT_ID,hex(""),LIFETIME));

        assertTrue(e.getMessage().contains(CLIENT_ID));
        assertFalse(e.getMessage().contains(hex("")));
    }

    @Test
    public void
    lifetimeIsCappedAtOneHour()
        throws Exception
    {
        new ServiceClientRegistration(CLIENT_ID,hex(SECRET),Duration.ofHours(1));
        assertThrows(
            IllegalArgumentException.class,
            () -> new ServiceClientRegistration(CLIENT_ID,hex(SECRET),Duration.ofMinutes(61)));
        assertThrows(
            IllegalArgumentException.class,
            () -> new ServiceClientRegistration(CLIENT_ID,hex(SECRET),Duration.ZERO));
    }

    @Test
    public void
    blankSecretHashIsRejected()
    {
        assertThrows(IllegalArgumentException.class,() -> new ServiceClientRegistration(CLIENT_ID,"",LIFETIME));
        assertThrows(IllegalArgumentException.class,() -> new ServiceClientRegistration(CLIENT_ID,"abc",LIFETIME));
    }

    @Test
    public void
    neverLogsOrPrintsSecretOrToken()
    {
        Logger                      logger   = (Logger)LoggerFactory.getLogger("granite.session");
        Level                       previous = logger.getLevel();
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        IssueServiceTokenRequest    request  = request(CLIENT_ID,SECRET);
        IssueServiceTokenReply      reply;

        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.TRACE);

        try
        {
            reply = subject.issue(request);
            subject.issue(request(CLIENT_ID,"wrong-secret"));
            subject.issue(request("someone-else",SECRET));
        }
        finally
        {
            logger.detachAppender(appender);
            logger.setLevel(previous);
        }

        assertFalse(appender.list.isEmpty());

        for (ILoggingEvent event : appender.list)
        {
            String message = event.getFormattedMessage();

            for (String secret : new String[] {SECRET,"wrong-secret",TOKEN})
                assertFalse(message.contains(secret),"secret logged: " + message);
        }

        assertFalse(request.toString().contains(SECRET));
        assertTrue(request.toString().contains(CLIENT_ID));
        assertFalse(reply.toString().contains(TOKEN));
    }

    private void
    assertRefused(IssueServiceTokenReply reply)
    {
        assertFalse(reply.isSuccess());
        assertEquals("invalid client",reply.getFailureMessage());
        assertNull(reply.getToken());
        Mockito
            .verify(jwtTokenService,Mockito.never())
            .mintServiceToken(Mockito.anyString(),Mockito.anyCollection(),Mockito.any());
    }

    private static IssueServiceTokenRequest
    request(String clientId,String secret)
    {
        return
            new IssueServiceTokenRequest()
                .setClientId(clientId)
                .setClientSecret(secret);
    }

    static String
    hex(String secret)
        throws NoSuchAlgorithmException
    {
        return HexFormat.of().formatHex(sha256(secret));
    }

    private static byte[]
    sha256(String secret)
        throws NoSuchAlgorithmException
    {
        return MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
    }
}

//////////////////////////////////////////////////////////////////////////////
