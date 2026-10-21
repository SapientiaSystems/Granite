//////////////////////////////////////////////////////////////////////////////
// AudienceValidator.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public
class AudienceValidator
    implements OAuth2TokenValidator<Jwt>
{
    private final Set<String> accepted;

    public
    AudienceValidator(Collection<String> accepted)
    {
        Objects.requireNonNull(accepted,"accepted");

        if (accepted.isEmpty())
            throw new IllegalArgumentException("accepted audiences must not be empty");

        this.accepted = Set.copyOf(accepted);
    }

    @Override
    public OAuth2TokenValidatorResult
    validate(Jwt jwt)
    {
        List<String> audience = jwt.getAudience();

        if (audience != null)
            for (String value : audience)
                if (accepted.contains(value))
                    return OAuth2TokenValidatorResult.success();

        return
            OAuth2TokenValidatorResult.failure(
                new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_TOKEN,
                    "The aud claim does not contain an accepted audience",
                    null));
    }
}

//////////////////////////////////////////////////////////////////////////////
