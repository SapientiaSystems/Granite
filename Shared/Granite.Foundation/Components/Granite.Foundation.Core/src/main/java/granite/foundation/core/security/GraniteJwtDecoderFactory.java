//////////////////////////////////////////////////////////////////////////////
// GraniteJwtDecoderFactory.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.util.Map;

public
class GraniteJwtDecoderFactory
{
    public JwtDecoder
    create(GraniteResourceServerSettings settings)
    {
        return create(settings,new RestTemplate());
    }

    public JwtDecoder
    create(GraniteResourceServerSettings settings,RestOperations restOperations)
    {
        String jwkSetUri =
            settings
                .getJwkSetUri()
                .orElseThrow(
                    () ->
                        new IllegalStateException(
                            GraniteResourceServerSettings.JWK_SET_URI + " must be configured"));
        NimbusJwtDecoder decoder =
            NimbusJwtDecoder
                .withJwkSetUri(jwkSetUri)
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .restOperations(restOperations)
                .build();

        decoder.setJwtValidator(createValidator(settings));
        return decoder;
    }

    public JwtDecoder
    create(Map<String,Object> publicJwkSet,GraniteResourceServerSettings settings)
    {
        DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();

        processor.setJWSKeySelector(
            new JWSVerificationKeySelector<>(
                JWSAlgorithm.RS256,
                new ImmutableJWKSet<>(parse(publicJwkSet))));
        processor.setJWTClaimsSetVerifier((claims,context) -> {});

        NimbusJwtDecoder decoder = new NimbusJwtDecoder(processor);

        decoder.setJwtValidator(createValidator(settings));
        return decoder;
    }

    public OAuth2TokenValidator<Jwt>
    createValidator(GraniteResourceServerSettings settings)
    {
        return
            new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(settings.getIssuer()),
                new AudienceValidator(settings.getAudiences()));
    }

    private static JWKSet
    parse(Map<String,Object> publicJwkSet)
    {
        try
        {
            return JWKSet.parse(publicJwkSet);
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException("invalid JWK set",e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
