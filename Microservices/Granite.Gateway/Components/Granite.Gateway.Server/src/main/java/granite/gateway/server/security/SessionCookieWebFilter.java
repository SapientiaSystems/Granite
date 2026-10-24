//////////////////////////////////////////////////////////////////////////////
// SessionCookieWebFilter.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

public
class SessionCookieWebFilter
    implements WebFilter
{
    public static final String SESSION_COOKIE = "GRANITE_SID";

    private final ISessionResolver         resolver;
    private final ServerWebExchangeMatcher publicMatcher;

    public
    SessionCookieWebFilter(ISessionResolver resolver,ServerWebExchangeMatcher publicMatcher)
    {
        this.resolver      = Objects.requireNonNull(resolver,"resolver");
        this.publicMatcher = Objects.requireNonNull(publicMatcher,"publicMatcher");
    }

    @Override
    public Mono<Void>
    filter(ServerWebExchange exchange,WebFilterChain chain)
    {
        HttpCookie session = exchange.getRequest().getCookies().getFirst(SESSION_COOKIE);

        if (session == null)
            return chain.filter(exchange);

        if (exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION))
            return chain.filter(forward(exchange,null));

        return
            publicMatcher
                .matches(exchange)
                .flatMap(
                    result ->
                        result.isMatch()
                            ? chain.filter(forward(exchange,null))
                            : resolveAndForward(exchange,chain,session.getValue()));
    }

    private Mono<Void>
    resolveAndForward(ServerWebExchange exchange,WebFilterChain chain,String handle)
    {
        return
            resolver
                .resolve(handle)
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty())
                .flatMap(
                    token ->
                        token.isPresent()
                            ? chain.filter(forward(exchange,token.get()))
                            : unauthorized(exchange));
    }

    private ServerWebExchange
    forward(ServerWebExchange exchange,String accessToken)
    {
        return
            exchange
                .mutate()
                .request(
                    request ->
                        request.headers(
                            headers ->
                            {
                                CookieHeaders.strip(headers,SESSION_COOKIE);

                                if (accessToken != null)
                                    headers.setBearerAuth(accessToken);
                            }))
                .build();
    }

    private Mono<Void>
    unauthorized(ServerWebExchange exchange)
    {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}

//////////////////////////////////////////////////////////////////////////////
