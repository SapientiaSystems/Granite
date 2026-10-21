//////////////////////////////////////////////////////////////////////////////
// GraniteJwtAuthenticationConverter.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public
class GraniteJwtAuthenticationConverter
    implements Converter<Jwt,AbstractAuthenticationToken>
{
    public static final String DEFAULT_AUTHORITIES_CLAIM = "authorities";

    private final String authoritiesClaim;

    public
    GraniteJwtAuthenticationConverter()
    {
        this(DEFAULT_AUTHORITIES_CLAIM);
    }

    public
    GraniteJwtAuthenticationConverter(String authoritiesClaim)
    {
        this.authoritiesClaim = Objects.requireNonNull(authoritiesClaim,"authoritiesClaim");
    }

    @Override
    public AbstractAuthenticationToken
    convert(Jwt jwt)
    {
        return new JwtAuthenticationToken(jwt,readAuthorities(jwt),jwt.getSubject());
    }

    public Collection<GrantedAuthority>
    readAuthorities(Jwt jwt)
    {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Object                 claim       = jwt.getClaims().get(authoritiesClaim);

        if (!(claim instanceof Collection<?> values))
            return authorities;

        for (Object value : values)
            if (value instanceof String authority && !authority.isBlank())
                authorities.add(new SimpleGrantedAuthority(authority));

        return authorities;
    }
}

//////////////////////////////////////////////////////////////////////////////
