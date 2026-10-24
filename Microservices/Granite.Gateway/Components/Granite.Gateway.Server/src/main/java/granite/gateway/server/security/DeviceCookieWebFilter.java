//////////////////////////////////////////////////////////////////////////////
// DeviceCookieWebFilter.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;

public
class DeviceCookieWebFilter
    implements WebFilter
{
    public static final String DEVICE_COOKIE = "GRANITE_DID";

    private final Duration     maxAge;
    private final SecureRandom random;

    public
    DeviceCookieWebFilter(Duration maxAge)
    {
        this.maxAge = Objects.requireNonNull(maxAge,"maxAge");
        this.random = new SecureRandom();
    }

    @Override
    public Mono<Void>
    filter(ServerWebExchange exchange,WebFilterChain chain)
    {
        if (exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)
            || exchange.getRequest().getCookies().getFirst(DEVICE_COOKIE) != null)
            return chain.filter(exchange);

        String device = newDeviceId();

        exchange
            .getResponse()
            .addCookie(
                ResponseCookie
                    .from(DEVICE_COOKIE,device)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(maxAge)
                    .build());

        return
            chain.filter(
                exchange
                    .mutate()
                    .request(
                        request ->
                            request.headers(headers -> CookieHeaders.append(headers,DEVICE_COOKIE,device)))
                    .build());
    }

    private String
    newDeviceId()
    {
        byte[] bytes = new byte[32];

        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

//////////////////////////////////////////////////////////////////////////////
