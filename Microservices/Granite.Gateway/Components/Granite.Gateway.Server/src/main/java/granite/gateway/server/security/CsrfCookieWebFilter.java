//////////////////////////////////////////////////////////////////////////////
// CsrfCookieWebFilter.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import org.springframework.http.HttpHeaders;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRepository;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Objects;

public
class CsrfCookieWebFilter
    implements WebFilter
{
    private final ServerCsrfTokenRepository repository;

    public
    CsrfCookieWebFilter(ServerCsrfTokenRepository repository)
    {
        this.repository = Objects.requireNonNull(repository,"repository");
    }

    @Override
    public Mono<Void>
    filter(ServerWebExchange exchange,WebFilterChain chain)
    {
        if (exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION))
            return chain.filter(exchange);

        return
            repository
                .loadToken(exchange)
                .switchIfEmpty(
                    Mono.defer(
                        () ->
                            repository
                                .generateToken(exchange)
                                .delayUntil(token -> repository.saveToken(exchange,token))))
                .then(Mono.defer(() -> chain.filter(exchange)));
    }
}

//////////////////////////////////////////////////////////////////////////////
