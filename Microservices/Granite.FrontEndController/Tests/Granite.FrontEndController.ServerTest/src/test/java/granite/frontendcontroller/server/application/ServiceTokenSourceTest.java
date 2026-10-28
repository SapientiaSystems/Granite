//////////////////////////////////////////////////////////////////////////////
// ServiceTokenSourceTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.application;

import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.service.requestreply.IssueServiceTokenReply;
import granite.session.service.requestreply.IssueServiceTokenRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("CommitStage")
public
class ServiceTokenSourceTest
{
    private static final Instant NOW = Instant.parse("2026-10-28T12:00:00Z");

    private ISessionCommandServiceClient client;
    private Clock                        clock;
    private ServiceTokenSource           subject;

    @BeforeEach
    public void
    setUp()
    {
        client  = Mockito.mock(ISessionCommandServiceClient.class);
        clock   = Mockito.mock(Clock.class);
        subject = new ServiceTokenSource(client,clock,"granite-frontendcontroller","fec-secret");

        Mockito
            .when(client.issueServiceToken(Mockito.any()))
            .thenReturn(
                issued("token-1",NOW.plus(Duration.ofMinutes(10))),
                issued("token-2",NOW.plus(Duration.ofMinutes(20))));
    }

    @Test
    public void
    sendsConfiguredClientCredentials()
    {
        ArgumentCaptor<IssueServiceTokenRequest> request = ArgumentCaptor.forClass(IssueServiceTokenRequest.class);

        Mockito.when(clock.instant()).thenReturn(NOW);
        subject.getToken();

        Mockito.verify(client).issueServiceToken(request.capture());
        assertEquals("granite-frontendcontroller",request.getValue().getClientId());
        assertEquals("fec-secret",request.getValue().getClientSecret());
    }

    @Test
    public void
    reusesTokenUntilRenewalMargin()
    {
        Mockito.when(clock.instant()).thenReturn(NOW,NOW.plus(Duration.ofMinutes(8)));

        assertEquals("token-1",subject.getToken());
        assertEquals("token-1",subject.getToken());
        Mockito.verify(client,Mockito.times(1)).issueServiceToken(Mockito.any());
    }

    @Test
    public void
    renewsTokenWithinOneMinuteOfExpiry()
    {
        Mockito.when(clock.instant()).thenReturn(NOW,NOW.plus(Duration.ofMinutes(9)));

        assertEquals("token-1",subject.getToken());
        assertEquals("token-2",subject.getToken());
        Mockito.verify(client,Mockito.times(2)).issueServiceToken(Mockito.any());
    }

    @Test
    public void
    refusedIssueFailsWithoutRevealingSecret()
    {
        Mockito.when(clock.instant()).thenReturn(NOW);
        Mockito
            .when(client.issueServiceToken(Mockito.any()))
            .thenReturn(CompletableFuture.completedFuture(new IssueServiceTokenReply().setSuccess(false).setFailureMessage("invalid client")));

        IllegalStateException e = assertThrows(IllegalStateException.class,() -> subject.getToken());

        assertFalse(e.getMessage().contains("fec-secret"));
    }

    private static CompletableFuture<IssueServiceTokenReply>
    issued(String token,Instant expiresAt)
    {
        return
            CompletableFuture.completedFuture(
                new IssueServiceTokenReply()
                    .setSuccess(true)
                    .setToken(token)
                    .setExpiresAt(expiresAt));
    }
}

//////////////////////////////////////////////////////////////////////////////
