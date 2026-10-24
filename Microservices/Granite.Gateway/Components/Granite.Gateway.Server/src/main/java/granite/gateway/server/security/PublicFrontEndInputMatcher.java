//////////////////////////////////////////////////////////////////////////////
// PublicFrontEndInputMatcher.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

public
class PublicFrontEndInputMatcher
    implements ServerWebExchangeMatcher
{
    public static final Set<String> PUBLIC_OUTPUT_KEYS = Set.of("Session.New->LoginView");
    public static final Set<String> PUBLIC_INPUT_KEYS  =
        Set.of(
            "Session.New->Login->LoginView",
            "Session.New->StartOktaLogin->LoginView",
            "Session.New->CompleteOktaLogin->LoginView");

    @Override
    public Mono<MatchResult>
    matches(ServerWebExchange exchange)
    {
        String key  = exchange.getAttribute(FrontEndInputKeyWebFilter.KEY_ATTRIBUTE);
        String path = exchange.getRequest().getPath().pathWithinApplication().value();

        if (key == null
            || !HttpMethod.POST.equals(exchange.getRequest().getMethod())
            || !FrontEndInputKeyWebFilter.PATHS.contains(path))
            return MatchResult.notMatch();

        boolean isPublic =
            path.endsWith(FrontEndInputKeyWebFilter.GET_OUTPUT)
                ? PUBLIC_OUTPUT_KEYS.contains(key)
                : PUBLIC_INPUT_KEYS.contains(key);

        return isPublic ? MatchResult.match() : MatchResult.notMatch();
    }
}

//////////////////////////////////////////////////////////////////////////////
