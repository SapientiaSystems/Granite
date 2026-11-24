//////////////////////////////////////////////////////////////////////////////
// JwtCallerSubject.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.platform;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.function.Supplier;

public
class JwtCallerSubject
    implements Supplier<Optional<String>>
{
    @Override
    public Optional<String>
    get()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt)
            return Optional.ofNullable(jwt.getSubject());

        return Optional.empty();
    }
}

//////////////////////////////////////////////////////////////////////////////
