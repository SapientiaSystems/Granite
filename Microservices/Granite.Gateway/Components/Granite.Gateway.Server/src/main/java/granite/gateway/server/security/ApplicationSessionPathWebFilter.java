//////////////////////////////////////////////////////////////////////////////
// ApplicationSessionPathWebFilter.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Objects;

public
class ApplicationSessionPathWebFilter
    implements WebFilter
{
    public static final String APPLICATION_CLAIM  = "app";
    public static final String CLIENT_APPLICATION = "client";

    private final ServerWebExchangeMatcher allowed;

    public
    ApplicationSessionPathWebFilter(ServerWebExchangeMatcher allowed)
    {
        this.allowed = Objects.requireNonNull(allowed,"allowed");
    }

    @Override
    public Mono<Void>
    filter(ServerWebExchange exchange,WebFilterChain chain)
    {
        return
            ReactiveSecurityContextHolder
                .getContext()
                .mapNotNull(context -> context.getAuthentication())
                .filter(authentication -> authentication.getPrincipal() instanceof Jwt)
                .map(authentication -> (Jwt)authentication.getPrincipal())
                .filter(jwt -> CLIENT_APPLICATION.equals(jwt.getClaimAsString(APPLICATION_CLAIM)))
                .flatMap(jwt -> allowed.matches(exchange))
                .map(match -> !match.isMatch())
                .defaultIfEmpty(false)
                .flatMap(
                    refuse ->
                    {
                        if (!refuse)
                            return chain.filter(exchange);

                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    });
    }
}

//////////////////////////////////////////////////////////////////////////////
