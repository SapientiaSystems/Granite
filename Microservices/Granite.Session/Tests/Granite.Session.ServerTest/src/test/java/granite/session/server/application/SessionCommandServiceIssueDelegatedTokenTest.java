//////////////////////////////////////////////////////////////////////////////
// SessionCommandServiceIssueDelegatedTokenTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import granite.session.server.domain.IOktaLoginTransactionRepository;
import granite.session.server.domain.ISessionRepository;
import granite.session.server.domain.Session;
import granite.session.server.platform.MockSessionEventSender;
import granite.session.service.requestreply.IssueDelegatedTokenReply;
import granite.session.service.requestreply.IssueDelegatedTokenRequest;
import granite.session.service.requestreply.SessionClosedReason;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class SessionCommandServiceIssueDelegatedTokenTest
{
    private ISessionRepository  repository;
    private IJwtTokenService    jwtTokenService;
    private IServiceTokenIssuer issuer;
    private Session             session;

    @BeforeEach
    public void
    setUp()
    {
        repository      = Mockito.mock(ISessionRepository.class);
        jwtTokenService = Mockito.mock(IJwtTokenService.class);
        issuer          = Mockito.mock(IServiceTokenIssuer.class);
        session         = new Session().setSessionId(11L).setUserId(7L).setExpires(Instant.now().plus(Duration.ofHours(1))).setAuthorities(Set.of("ROLE_USER"));
        session.setCreated(Instant.now().minus(Duration.ofMinutes(5)));

        Mockito.when(issuer.mayDelegate("granite-agent","agent-secret")).thenReturn(true);
        Mockito.when(repository.findById(11L)).thenReturn(Optional.of(session));
        Mockito.when(jwtTokenService.mintDelegated(session,"granite-agent",SessionCommandService.DELEGATED_TOKEN_LIFETIME)).thenReturn("delegated");
        Mockito.when(jwtTokenService.getExpiration("delegated")).thenReturn(Instant.now().plus(SessionCommandService.DELEGATED_TOKEN_LIFETIME));
    }

    @Test
    public void
    issuesForTheOwnerOfAnOpenSession()
    {
        IssueDelegatedTokenReply reply = issue(request(7L));

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals("delegated",reply.getToken());
        Mockito.verify(jwtTokenService).mintDelegated(session,"granite-agent",SessionCommandService.DELEGATED_TOKEN_LIFETIME);
    }

    @Test
    public void
    refusesAnotherUsersSession()
    {
        assertRefused(issue(request(8L)),"invalid session");
    }

    @Test
    public void
    refusesAClosedSession()
    {
        session.setClosedReason(SessionClosedReason.LOGOUT);

        assertRefused(issue(request(7L)),"invalid session");
    }

    @Test
    public void
    refusesAClientWithoutDelegation()
    {
        Mockito.when(issuer.mayDelegate("granite-agent","agent-secret")).thenReturn(false);

        assertRefused(issue(request(7L)),ServiceTokenIssuer.INVALID_CLIENT);
        Mockito.verifyNoInteractions(repository);
    }

    private void
    assertRefused(IssueDelegatedTokenReply reply,String message)
    {
        assertFalse(reply.isSuccess());
        assertEquals(message,reply.getFailureMessage());
        assertNull(reply.getToken());
        Mockito.verify(jwtTokenService,Mockito.never()).mintDelegated(Mockito.any(),Mockito.any(),Mockito.any());
    }

    private IssueDelegatedTokenReply
    issue(IssueDelegatedTokenRequest request)
    {
        return subject().issueDelegatedToken(request).toCompletableFuture().join();
    }

    private static IssueDelegatedTokenRequest
    request(Long userId)
    {
        return
            new IssueDelegatedTokenRequest()
                .setClientId("granite-agent")
                .setClientSecret("agent-secret")
                .setSessionId(11L)
                .setUserId(userId);
    }

    private SessionCommandService
    subject()
    {
        return
            new SessionCommandService(
                repository,
                Mockito.mock(IOktaLoginTransactionRepository.class),
                jwtTokenService,
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
