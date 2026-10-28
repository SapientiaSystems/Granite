//////////////////////////////////////////////////////////////////////////////
// ServiceTokenIssuer.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.foundation.core.security.GraniteAuthorities;
import granite.session.service.requestreply.IssueServiceTokenReply;
import granite.session.service.requestreply.IssueServiceTokenRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public
class ServiceTokenIssuer
    implements IServiceTokenIssuer
{
    public static final String INVALID_CLIENT = "invalid client";

    private static final byte[] UNKNOWN_HASH = ServiceClientRegistration.hash("unknown-service-client");

    private final IJwtTokenService                       jwtTokenService;
    private final Map<String,ServiceClientRegistration> registrations;
    private final Logger                                 logger;

    public
    ServiceTokenIssuer(IJwtTokenService jwtTokenService,List<ServiceClientRegistration> registrations)
    {
        this.jwtTokenService = Objects.requireNonNull(jwtTokenService,"jwtTokenService");
        this.registrations   = new LinkedHashMap<>();
        this.logger          = LogManager.getLogger(ServiceTokenIssuer.class);

        for (ServiceClientRegistration registration : registrations)
            this.registrations.put(registration.getClientId(),registration);
    }

    @Override
    public IssueServiceTokenReply
    issue(IssueServiceTokenRequest request)
    {
        ServiceClientRegistration registration =
            request != null && request.getClientId() != null
                ? registrations.get(request.getClientId())
                : null;
        String                    secret       =
            request != null && request.getClientSecret() != null
                ? request.getClientSecret()
                : "";

        if (registration == null)
        {
            MessageDigest.isEqual(UNKNOWN_HASH,ServiceClientRegistration.hash(secret));
            logger.warn("service token refused: unknown client");
            return refuse(request);
        }

        boolean matches = registration.matches(secret);

        if (secret.isBlank() || !matches)
        {
            logger.warn("service token refused for client {}",registration.getClientId());
            return refuse(request);
        }

        String token =
            jwtTokenService.mintServiceToken(
                registration.getClientId(),
                List.of(GraniteAuthorities.SCOPE_INTERNAL),
                registration.getLifetime());

        logger.info("service token issued to client {}",registration.getClientId());
        return
            new IssueServiceTokenReply(request)
                .setSuccess(true)
                .setToken(token)
                .setExpiresAt(jwtTokenService.getExpiration(token));
    }

    private IssueServiceTokenReply
    refuse(IssueServiceTokenRequest request)
    {
        IssueServiceTokenReply reply =
            request != null
                ? new IssueServiceTokenReply(request)
                : new IssueServiceTokenReply();

        return
            reply
                .setSuccess(false)
                .setFailureMessage(INVALID_CLIENT);
    }
}

//////////////////////////////////////////////////////////////////////////////
