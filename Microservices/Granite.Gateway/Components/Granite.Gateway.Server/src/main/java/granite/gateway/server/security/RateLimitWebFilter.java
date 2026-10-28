//////////////////////////////////////////////////////////////////////////////
// RateLimitWebFilter.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Objects;

public
class RateLimitWebFilter
    implements WebFilter
{
    private final ServerWebExchangeMatcher matcher;
    private final FixedWindowRateLimiter   limiter;
    private final Logger                   logger;

    public
    RateLimitWebFilter(ServerWebExchangeMatcher matcher,FixedWindowRateLimiter limiter)
    {
        this.matcher = Objects.requireNonNull(matcher,"matcher");
        this.limiter = Objects.requireNonNull(limiter,"limiter");
        this.logger  = LogManager.getLogger(RateLimitWebFilter.class);
    }

    @Override
    public Mono<Void>
    filter(ServerWebExchange exchange,WebFilterChain chain)
    {
        return
            matcher
                .matches(exchange)
                .flatMap(
                    match ->
                    {
                        if (!match.isMatch())
                            return chain.filter(exchange);

                        String client = getClient(exchange);

                        if (limiter.tryAcquire(client))
                            return chain.filter(exchange);

                        logger.warn("rate limit exceeded on {} for client {}",exchange.getRequest().getPath(),client);
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        return exchange.getResponse().setComplete();
                    });
    }

    private static String
    getClient(ServerWebExchange exchange)
    {
        InetSocketAddress address = exchange.getRequest().getRemoteAddress();

        if (address == null)
            return "unknown";

        return
            address.getAddress() != null
                ? address.getAddress().getHostAddress()
                : address.getHostString();
    }
}

//////////////////////////////////////////////////////////////////////////////
