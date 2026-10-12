//////////////////////////////////////////////////////////////////////////////
// Oauth2IdTokenValidator.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public
class Oauth2IdTokenValidator
    implements IOauth2IdTokenValidator
{
    private static final String EMAIL_CLAIM = "email";
    private static final String ISS_CLAIM   = "iss";
    private static final String ALG_HEADER  = "alg";
    private static final String RS256       = "RS256";

    private final Oauth2ProvidersProperties          properties;
    private final ConcurrentMap<String,NimbusJwtDecoder> decoders;

    public
    Oauth2IdTokenValidator(Oauth2ProvidersProperties properties)
    {
        Objects.requireNonNull(properties,"properties");

        this.properties = properties;
        this.decoders   = new ConcurrentHashMap<>();
    }

    @Override
    public Oauth2ValidatedIdentity
    validate(String provider,String idToken)
        throws Oauth2ValidationException
    {
        Objects.requireNonNull(provider,"provider");
        Objects.requireNonNull(idToken,"idToken");

        Oauth2ProviderConfig config = properties.getProviders().get(provider);

        if (config == null)
            throw new Oauth2ValidationException("unknown provider: " + provider);

        NimbusJwtDecoder decoder = getOrCreateDecoder(provider,config);
        Jwt              jwt;

        try
        {
            jwt = decoder.decode(idToken);
        }
        catch (JwtException e)
        {
            String message = e.getMessage() != null ? e.getMessage() : "";
            String lower   = message.toLowerCase();

            if (lower.contains("expired"))
                throw new Oauth2ValidationException("expired: " + message,e);

            if (lower.contains("signature") || lower.contains("rejected"))
                throw new Oauth2ValidationException("bad signature: " + message,e);

            throw new Oauth2ValidationException("token validation failed: " + message,e);
        }

        Object alg = jwt.getHeaders().get(ALG_HEADER);

        if (!RS256.equals(String.valueOf(alg)))
            throw new Oauth2ValidationException("bad algorithm: " + alg);

        String iss = jwt.getClaimAsString(ISS_CLAIM);

        if (!config.getIssuerUri().equals(iss))
            throw new Oauth2ValidationException("bad issuer: " + iss);

        List<String> aud = jwt.getAudience();

        if (aud == null || !aud.contains(config.getAudience()))
            throw new Oauth2ValidationException("bad audience: " + aud);

        String sub = jwt.getSubject();

        if (sub == null || sub.isBlank())
            throw new Oauth2ValidationException("missing sub claim");

        String email = jwt.getClaimAsString(EMAIL_CLAIM);

        return Oauth2ValidatedIdentity.of(provider,sub,Optional.ofNullable(email));
    }

    private NimbusJwtDecoder
    getOrCreateDecoder(String provider,Oauth2ProviderConfig config)
    {
        return decoders.computeIfAbsent(provider,p -> createDecoder(config));
    }

    private NimbusJwtDecoder
    createDecoder(Oauth2ProviderConfig config)
    {
        return
            NimbusJwtDecoder.withJwkSetUri(config.getJwksUri())
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
    }
}

//////////////////////////////////////////////////////////////////////////////
