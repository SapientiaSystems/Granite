//////////////////////////////////////////////////////////////////////////////
// SessionDelegatedTokenSource.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.agent;

import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.service.requestreply.IssueDelegatedTokenReply;
import granite.session.service.requestreply.IssueDelegatedTokenRequest;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public
class SessionDelegatedTokenSource
    implements IDelegatedTokenSource
{
    private final Supplier<ISessionCommandServiceClient> client;
    private final String                                 clientId;
    private final String                                 clientSecret;

    public
    SessionDelegatedTokenSource(Supplier<ISessionCommandServiceClient> client,String clientId,String clientSecret)
    {
        this.client       = Objects.requireNonNull(client,"client");
        this.clientId     = clientId == null ? "" : clientId;
        this.clientSecret = clientSecret == null ? "" : clientSecret;
    }

    @Override
    public String
    issue(Long sessionId,Long userId)
    {
        IssueDelegatedTokenReply reply =
            client
                .get()
                .issueDelegatedToken(
                    new IssueDelegatedTokenRequest()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .setSessionId(sessionId)
                        .setUserId(userId))
                .toCompletableFuture()
                .orTimeout(AgentServiceTokenSource.ISSUE_TIMEOUT.toMillis(),TimeUnit.MILLISECONDS)
                .join();

        if (reply == null || !reply.isSuccess() || reply.getToken() == null)
            throw new IllegalStateException("delegated token refused");

        return reply.getToken();
    }
}

//////////////////////////////////////////////////////////////////////////////
