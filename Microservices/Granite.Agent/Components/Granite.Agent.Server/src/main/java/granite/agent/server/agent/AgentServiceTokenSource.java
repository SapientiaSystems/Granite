//////////////////////////////////////////////////////////////////////////////
// AgentServiceTokenSource.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.agent;

import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.service.requestreply.IssueServiceTokenReply;
import granite.session.service.requestreply.IssueServiceTokenRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public
class AgentServiceTokenSource
    implements Supplier<String>
{
    public static final Duration ISSUE_TIMEOUT  = Duration.ofSeconds(5);
    public static final Duration RENEWAL_MARGIN = Duration.ofSeconds(60);

    private final Supplier<ISessionCommandServiceClient> client;
    private final Clock                                  clock;
    private final String                                 clientId;
    private final String                                 clientSecret;
    private final Logger                                 logger;
    private String                                       token;
    private Instant                                      renewAt;

    public
    AgentServiceTokenSource(Supplier<ISessionCommandServiceClient> client,Clock clock,String clientId,String clientSecret)
    {
        this.client       = Objects.requireNonNull(client,"client");
        this.clock        = Objects.requireNonNull(clock,"clock");
        this.clientId     = clientId == null ? "" : clientId;
        this.clientSecret = clientSecret == null ? "" : clientSecret;
        this.logger       = LogManager.getLogger(AgentServiceTokenSource.class);
        this.token        = null;
        this.renewAt      = null;
    }

    @Override
    public synchronized String
    get()
    {
        Instant now = Instant.now(clock);

        if (token != null && renewAt != null && now.isBefore(renewAt))
            return token;

        if (clientId.isBlank() || clientSecret.isBlank())
            throw new IllegalStateException("agent service client is not configured");

        IssueServiceTokenReply reply =
            client
                .get()
                .issueServiceToken(new IssueServiceTokenRequest().setClientId(clientId).setClientSecret(clientSecret))
                .toCompletableFuture()
                .orTimeout(ISSUE_TIMEOUT.toMillis(),TimeUnit.MILLISECONDS)
                .join();

        if (reply == null || !reply.isSuccess() || reply.getToken() == null)
        {
            logger.warn("service token refused for client {}",clientId);
            throw new IllegalStateException("service token refused");
        }

        token   = reply.getToken();
        renewAt = reply.getExpiresAt() == null ? now.plus(Duration.ofMinutes(4)) : reply.getExpiresAt().minus(RENEWAL_MARGIN);
        return token;
    }
}

//////////////////////////////////////////////////////////////////////////////
