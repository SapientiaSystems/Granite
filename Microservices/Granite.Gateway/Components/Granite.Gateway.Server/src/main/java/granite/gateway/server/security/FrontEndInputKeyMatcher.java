//////////////////////////////////////////////////////////////////////////////
// FrontEndInputKeyMatcher.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

public
class FrontEndInputKeyMatcher
    implements ServerWebExchangeMatcher
{
    private final Set<String> outputKeys;
    private final Set<String> inputKeys;

    public
    FrontEndInputKeyMatcher(Set<String> outputKeys,Set<String> inputKeys)
    {
        this.outputKeys = outputKeys;
        this.inputKeys  = inputKeys;
    }

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

        boolean isMatch =
            path.endsWith(FrontEndInputKeyWebFilter.GET_OUTPUT)
                ? outputKeys.contains(key)
                : inputKeys.contains(key);

        return isMatch ? MatchResult.match() : MatchResult.notMatch();
    }
}

//////////////////////////////////////////////////////////////////////////////
