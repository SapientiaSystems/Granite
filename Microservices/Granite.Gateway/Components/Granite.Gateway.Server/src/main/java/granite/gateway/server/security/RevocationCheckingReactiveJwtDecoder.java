//////////////////////////////////////////////////////////////////////////////
// RevocationCheckingReactiveJwtDecoder.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

import java.util.Objects;

public
class RevocationCheckingReactiveJwtDecoder
    implements ReactiveJwtDecoder
{
    private final ReactiveJwtDecoder        delegate;
    private final ISessionRevocationChecker checker;

    public
    RevocationCheckingReactiveJwtDecoder(ReactiveJwtDecoder delegate,ISessionRevocationChecker checker)
    {
        this.delegate = Objects.requireNonNull(delegate,"delegate");
        this.checker  = Objects.requireNonNull(checker,"checker");
    }

    @Override
    public Mono<Jwt>
    decode(String token)
        throws JwtException
    {
        return
            delegate
                .decode(token)
                .flatMap(
                    jwt ->
                        checker
                            .isActive(jwt)
                            .flatMap(
                                active ->
                                    active
                                        ? Mono.just(jwt)
                                        : Mono.error(new BadJwtException("session is no longer active"))));
    }
}

//////////////////////////////////////////////////////////////////////////////
