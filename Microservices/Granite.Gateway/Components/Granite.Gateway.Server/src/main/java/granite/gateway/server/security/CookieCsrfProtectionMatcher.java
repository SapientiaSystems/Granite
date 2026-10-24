//////////////////////////////////////////////////////////////////////////////
// CookieCsrfProtectionMatcher.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

public
class CookieCsrfProtectionMatcher
    implements ServerWebExchangeMatcher
{
    private static final Set<HttpMethod> SAFE_METHODS =
        Set.of(HttpMethod.GET,HttpMethod.HEAD,HttpMethod.TRACE,HttpMethod.OPTIONS);

    @Override
    public Mono<MatchResult>
    matches(ServerWebExchange exchange)
    {
        ServerHttpRequest request = exchange.getRequest();

        if (SAFE_METHODS.contains(request.getMethod())
            || request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)
            || request.getCookies().isEmpty())
            return MatchResult.notMatch();

        return MatchResult.match();
    }
}

//////////////////////////////////////////////////////////////////////////////
