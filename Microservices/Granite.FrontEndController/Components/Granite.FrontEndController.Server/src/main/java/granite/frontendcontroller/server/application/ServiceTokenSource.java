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
import java.util.concurrent.TimeUnit;

public
class ServiceTokenSource
    implements IServiceTokenSource
{
    public static final Duration DEFAULT_ISSUE_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration FAILURE_BACKOFF       = Duration.ofSeconds(5);

    private static final Duration RENEWAL_MARGIN = Duration.ofSeconds(60);

    private final ISessionCommandServiceClient client;
    private final Clock                        clock;
    private final String                       clientId;
    private final String                       clientSecret;
    private final Duration                     issueTimeout;
    private final Logger                       logger;
    private String                             token;
    private Instant                            expiresAt;
    private Instant                            renewAt;
    private Instant                            retryAt;

    public
    ServiceTokenSource(
        ISessionCommandServiceClient client,
        Clock                        clock,
        String                       clientId,
        String                       clientSecret,
        Duration                     issueTimeout)
    {
        this.client       = Objects.requireNonNull(client,"client");
        this.clock        = Objects.requireNonNull(clock,"clock");
        this.clientId     = clientId != null ? clientId : "";
        this.clientSecret = clientSecret != null ? clientSecret : "";
        this.issueTimeout = Objects.requireNonNull(issueTimeout,"issueTimeout");
        this.logger       = LogManager.getLogger(ServiceTokenSource.class);
        this.token        = null;
        this.expiresAt    = null;
        this.renewAt      = null;
        this.retryAt      = null;
    }

    @Override
    public synchronized String
    getToken()
    {
        Instant now = clock.instant();

        if (token != null && now.isBefore(renewAt))
            return token;

        if (retryAt != null && now.isBefore(retryAt))
            return cachedOrFail(now);

        try
        {
            issue(now);
            retryAt = null;
        }
        catch (RuntimeException e)
        {
            retryAt = clock.instant().plus(FAILURE_BACKOFF);

            if (token != null && expiresAt != null && now.isBefore(expiresAt))
            {
                logger.warn("service token renewal failed for client {}; using the cached token",clientId);
                return token;
            }

            token     = null;
            expiresAt = null;
            renewAt   = null;
            throw e;
        }

        return token;
    }

    private String
    cachedOrFail(Instant now)
    {
        if (token != null && expiresAt != null && now.isBefore(expiresAt))
            return token;

        throw new IllegalStateException("service token unavailable for client " + clientId);
    }

    private void
    issue(Instant now)
    {
        if (clientId.isBlank() || clientSecret.isBlank())
            throw new IllegalStateException("service client id or secret is not configured" + (clientId.isBlank() ? "" : " for client " + clientId));

        IssueServiceTokenReply reply;

        try
        {
            reply =
                client
                    .issueServiceToken(
                        new IssueServiceTokenRequest()
                            .setRequestId(UUID.randomUUID())
                            .setTimestamp(now)
                            .setClientId(clientId)
                            .setClientSecret(clientSecret))
                    .toCompletableFuture()
                    .orTimeout(issueTimeout.toMillis(),TimeUnit.MILLISECONDS)
                    .join();
        }
        catch (RuntimeException e)
        {
            logger.warn("service token request failed for client {}: {}",clientId,e.getClass().getName());
            throw new IllegalStateException("service token unavailable for client " + clientId);
        }

        if (reply == null || !reply.isSuccess() || reply.getToken() == null)
        {
            logger.warn("service token for client {} was refused",clientId);
            throw new IllegalStateException("service token unavailable for client " + clientId);
        }

        token     = reply.getToken();
        expiresAt = reply.getExpiresAt();
        renewAt   =
            expiresAt != null
                ? expiresAt.minus(RENEWAL_MARGIN)
                : now;
        logger.info("service token issued for client {}",clientId);
    }
}

//////////////////////////////////////////////////////////////////////////////
