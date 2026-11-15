//////////////////////////////////////////////////////////////////////////////
// ClientTokenIssuer.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.client.client.requestreply.IClientCommandServiceClient;
import granite.client.service.requestreply.VerifyClientCredentialsReply;
import granite.client.service.requestreply.VerifyClientCredentialsRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public
class ClientTokenIssuer
    implements IClientTokenIssuer
{
    public static final String INVALID_CLIENT = "invalid client";
    public static final String ROLE_ADMIN     = "ROLE_ADMIN";
    public static final String SCOPE_PREFIX   = "SCOPE_";

    private final IJwtTokenService            jwtTokenService;
    private final IClientCommandServiceClient clientService;
    private final Duration                    lifetime;
    private final Duration                    verifyTimeout;
    private final Logger                      logger;

    public
    ClientTokenIssuer(
        IJwtTokenService            jwtTokenService,
        IClientCommandServiceClient clientService,
        Duration                    lifetime,
        Duration                    verifyTimeout)
    {
        this.jwtTokenService = Objects.requireNonNull(jwtTokenService,"jwtTokenService");
        this.clientService   = Objects.requireNonNull(clientService,"clientService");
        this.lifetime        = Objects.requireNonNull(lifetime,"lifetime");
        this.verifyTimeout   = Objects.requireNonNull(verifyTimeout,"verifyTimeout");
        this.logger          = LogManager.getLogger(ClientTokenIssuer.class);
    }

    @Override
    public CompletionStage<ClientTokenResult>
    issue(String clientId,String clientSecret)
    {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank())
            return CompletableFuture.completedFuture(refuse());

        CompletionStage<VerifyClientCredentialsReply> stage;

        try
        {
            stage =
                clientService.verifyClientCredentials(
                    new VerifyClientCredentialsRequest()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret));
        }
        catch (RuntimeException e)
        {
            return CompletableFuture.completedFuture(unavailable(e));
        }

        return
            stage
                .toCompletableFuture()
                .orTimeout(verifyTimeout.toMillis(),TimeUnit.MILLISECONDS)
                .handle((reply,failure) -> resolve(clientId,reply,failure));
    }

    private ClientTokenResult
    resolve(String clientId,VerifyClientCredentialsReply reply,Throwable failure)
    {
        if (failure != null)
            return unavailable(failure);

        if (reply == null)
            return unavailable(null);

        if (reply.isSuccess() && reply.isValid())
            return mint(clientId,reply);

        if (reply.isSuccess() || INVALID_CLIENT.equals(reply.getFailureMessage()))
            return refuse();

        return unavailable(null);
    }

    private ClientTokenResult
    mint(String clientId,VerifyClientCredentialsReply reply)
    {
        try
        {
            String verifiedId = reply.getClientId();

            if (!clientId.equals(verifiedId))
                return unavailable(null);

            String token =
                jwtTokenService.mintServiceToken(
                    "client:" + verifiedId,
                    filter(reply.getAuthorities()),
                    lifetime);

            logger.info("client token issued to client {}",verifiedId);
            return ClientTokenResult.ok(token,lifetime.toSeconds());
        }
        catch (RuntimeException e)
        {
            return unavailable(e);
        }
    }

    private List<String>
    filter(Collection<String> authorities)
    {
        List<String> kept = new ArrayList<>();

        if (authorities != null)
            for (String authority : authorities)
                if (authority != null && !authority.startsWith(SCOPE_PREFIX) && !ROLE_ADMIN.equals(authority))
                    kept.add(authority);

        return kept;
    }

    private ClientTokenResult
    refuse()
    {
        logger.warn("client token refused");
        return ClientTokenResult.invalidClient();
    }

    private ClientTokenResult
    unavailable(Throwable failure)
    {
        logger.warn(
            "client token unavailable: {}",
            failure != null ? failure.getClass().getSimpleName() : "client service refused verification");
        return ClientTokenResult.unavailable();
    }
}

//////////////////////////////////////////////////////////////////////////////
