//////////////////////////////////////////////////////////////////////////////
// GraniteResourceServerSettings.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.security;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.env.PropertyResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public
class GraniteResourceServerSettings
{
    public static final String JWK_SET_URI      = "granite.security.jwt.jwk-set-uri";
    public static final String ISSUER           = "granite.security.jwt.issuer";
    public static final String AUDIENCE         = "granite.security.jwt.audience";
    public static final String SSL_BUNDLE       = "granite.security.jwt.ssl-bundle";
    public static final String DEFAULT_ISSUER   = "granite-session";
    public static final String DEFAULT_AUDIENCE = "granite-services";

    private final String       jwkSetUri;
    private final String       issuer;
    private final List<String> audiences;

    public
    GraniteResourceServerSettings(String jwkSetUri,String issuer,List<String> audiences)
    {
        this.jwkSetUri = jwkSetUri;
        this.issuer    = Objects.requireNonNull(issuer,"issuer");
        this.audiences = List.copyOf(Objects.requireNonNull(audiences,"audiences"));

        if (this.audiences.isEmpty())
            throw new IllegalArgumentException("audiences must not be empty");
    }

    public Optional<String>
    getJwkSetUri() { return Optional.ofNullable(jwkSetUri).filter(uri -> !uri.isBlank()); }

    public String
    getIssuer() { return issuer; }

    public List<String>
    getAudiences() { return audiences; }

    public static GraniteResourceServerSettings
    from(PropertyResolver properties)
    {
        return
            new GraniteResourceServerSettings(
                properties.getProperty(JWK_SET_URI),
                properties.getProperty(ISSUER,DEFAULT_ISSUER),
                splitAudience(properties.getProperty(AUDIENCE,DEFAULT_AUDIENCE)));
    }

    public static GraniteResourceServerSettings
    from(Environment environment)
    {
        if (environment.acceptsProfiles(Profiles.of("production"))
            && !(environment.containsProperty(ISSUER) && environment.containsProperty(AUDIENCE)))
            throw
                new IllegalStateException(
                    ISSUER + " and " + AUDIENCE + " must be set in production");

        return from((PropertyResolver)environment);
    }

    private static List<String>
    splitAudience(String value)
    {
        List<String> audiences = new ArrayList<>();

        for (String audience : value.split(","))
            if (!audience.isBlank())
                audiences.add(audience.trim());

        return audiences;
    }
}

//////////////////////////////////////////////////////////////////////////////
