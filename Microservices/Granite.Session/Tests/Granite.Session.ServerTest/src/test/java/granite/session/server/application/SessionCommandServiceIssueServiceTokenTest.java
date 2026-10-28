//////////////////////////////////////////////////////////////////////////////
// SessionCommandServiceIssueServiceTokenTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.session.server.domain.IOktaLoginTransactionRepository;
import granite.session.server.domain.ISessionRepository;
import granite.session.server.platform.MockSessionEventSender;
import granite.session.service.requestreply.IssueServiceTokenReply;
import granite.session.service.requestreply.IssueServiceTokenRequest;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@Tag("CommitStage")
public
class SessionCommandServiceIssueServiceTokenTest
{
    @Test
    public void
    delegatesToIssuer()
    {
        IServiceTokenIssuer      issuer  = Mockito.mock(IServiceTokenIssuer.class);
        IssueServiceTokenRequest request = request();
        IssueServiceTokenReply   reply   = new IssueServiceTokenReply(request).setSuccess(true).setToken("token");

        Mockito.when(issuer.issue(request)).thenReturn(reply);

        assertSame(reply,issue(subject(issuer),request));
    }

    @Test
    public void
    withoutIssuerEveryClientIsRefused()
    {
        assertRefused(issue(subject(null),request()));
    }

    @Test
    public void
    issuerFailureIsRefusedGenerically()
    {
        IServiceTokenIssuer issuer = Mockito.mock(IServiceTokenIssuer.class);

        Mockito.when(issuer.issue(Mockito.any())).thenThrow(new IllegalStateException("signing failed"));

        assertRefused(issue(subject(issuer),request()));
    }

    private static void
    assertRefused(IssueServiceTokenReply reply)
    {
        assertFalse(reply.isSuccess());
        assertEquals(ServiceTokenIssuer.INVALID_CLIENT,reply.getFailureMessage());
        assertNull(reply.getToken());
        assertNull(reply.getException());
    }

    private static IssueServiceTokenReply
    issue(SessionCommandService subject,IssueServiceTokenRequest request)
    {
        return subject.issueServiceToken(request).toCompletableFuture().join();
    }

    private static IssueServiceTokenRequest
    request()
    {
        return
            new IssueServiceTokenRequest()
                .setClientId("granite-frontendcontroller")
                .setClientSecret("fec-secret");
    }

    private static SessionCommandService
    subject(IServiceTokenIssuer issuer)
    {
        return
            new SessionCommandService(
                Mockito.mock(ISessionRepository.class),
                Mockito.mock(IOktaLoginTransactionRepository.class),
                Mockito.mock(IJwtTokenService.class),
                new RefreshTokenService(),
                Mockito.mock(IUserCommandServiceClient.class),
                Mockito.mock(IUserQueryServiceClient.class),
                Mockito.mock(IOktaClient.class),
                new OktaSettings(),
                new RequiredAuthoritiesLoginGate(List.of()),
                new MockSessionEventSender(),
                new ModelMapper().registerModule(new MappingModule()),
                Duration.ofHours(24),
                Duration.ofMinutes(2),
                Clock.systemUTC(),
                new GraniteApplicationRolesManagerSupplier().get(),
                issuer);
    }
}

//////////////////////////////////////////////////////////////////////////////
