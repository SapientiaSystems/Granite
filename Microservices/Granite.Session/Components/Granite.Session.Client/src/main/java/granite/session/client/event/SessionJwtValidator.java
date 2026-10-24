//////////////////////////////////////////////////////////////////////////////
// SessionJwtValidator.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.client.event;

import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.session.service.requestreply.FindSessionsByKeyReply;
import granite.session.service.requestreply.FindSessionsByKeyRequest;
import granite.session.service.requestreply.SessionData;
import granite.session.service.requestreply.SessionKey;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.util.Optional;

public
class SessionJwtValidator
    implements ISessionJwtValidator
{
    private final JwtDecoder                 decoder;
    private final ISessionQueryServiceClient sessionQueryClient;
    private final Clock                      clock;

    public
    SessionJwtValidator(
        RSAPublicKey                publicKey,
        ISessionQueryServiceClient  sessionQueryClient,
        Clock                       clock)
    {
        this(
            NimbusJwtDecoder.withPublicKey(publicKey).build(),
            sessionQueryClient,
            clock);
    }

    public
    SessionJwtValidator(
        JwtDecoder                  decoder,
        ISessionQueryServiceClient  sessionQueryClient,
        Clock                       clock)
    {
        this.decoder            = decoder;
        this.sessionQueryClient = sessionQueryClient;
        this.clock              = clock;
    }

    @Override
    public Optional<Long>
    validate(String jwt)
    {
        try
        {
            Jwt                     token   = decoder.decode(jwt);
            Long                    userId  = Long.valueOf(token.getSubject());
            Long                    id      = Long.valueOf(token.getId());
            FindSessionsByKeyReply  reply   =
                sessionQueryClient
                    .findSessionsByKey(
                        new FindSessionsByKeyRequest().setKey(SessionKey.ofSessionId(id)))
                    .toCompletableFuture()
                    .join();

            if (!reply.isSuccess() ||
                reply.getSessions() == null ||
                reply.getSessions().isEmpty())
                return Optional.empty();

            SessionData session = reply.getSessions().get(0);

            if (session.getClosedReason() != null ||
                session.getExpires() == null ||
                !session.getExpires().isAfter(clock.instant()))
                return Optional.empty();

            return Optional.of(userId);
        }
        catch (JwtException | NumberFormatException e)
        {
            return Optional.empty();
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
