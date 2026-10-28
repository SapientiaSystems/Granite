//////////////////////////////////////////////////////////////////////////////
// ServiceTokenSource.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.application;

import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.service.requestreply.IssueServiceTokenReply;
import granite.session.service.requestreply.IssueServiceTokenRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public
class ServiceTokenSource
    implements IServiceTokenSource
{
    private static final Duration RENEWAL_MARGIN = Duration.ofSeconds(60);

    private final ISessionCommandServiceClient client;
    private final Clock                        clock;
    private final String                       clientId;
    private final String                       clientSecret;
    private final Logger                       logger;
    private String                             token;
    private Instant                            renewAt;

    public
    ServiceTokenSource(
        ISessionCommandServiceClient client,
        Clock                        clock,
        String                       clientId,
        String                       clientSecret)
    {
        this.client       = Objects.requireNonNull(client,"client");
        this.clock        = Objects.requireNonNull(clock,"clock");
        this.clientId     = Objects.requireNonNull(clientId,"clientId");
        this.clientSecret = Objects.requireNonNull(clientSecret,"clientSecret");
        this.logger       = LogManager.getLogger(ServiceTokenSource.class);
        this.token        = null;
        this.renewAt      = null;
    }

    @Override
    public synchronized String
    getToken()
    {
        Instant now = clock.instant();

        if (token == null || !now.isBefore(renewAt))
            issue(now);

        return token;
    }

    private void
    issue(Instant now)
    {
        IssueServiceTokenReply reply =
            client
                .issueServiceToken(
                    new IssueServiceTokenRequest()
                        .setRequestId(UUID.randomUUID())
                        .setTimestamp(now)
                        .setClientId(clientId)
                        .setClientSecret(clientSecret))
                .toCompletableFuture()
                .join();

        if (reply == null || !reply.isSuccess() || reply.getToken() == null)
        {
            token   = null;
            renewAt = null;
            logger.warn("service token for client {} was refused",clientId);
            throw new IllegalStateException("service token unavailable for client " + clientId);
        }

        token   = reply.getToken();
        renewAt =
            reply.getExpiresAt() != null
                ? reply.getExpiresAt().minus(RENEWAL_MARGIN)
                : now;
        logger.info("service token issued for client {}",clientId);
    }
}

//////////////////////////////////////////////////////////////////////////////
