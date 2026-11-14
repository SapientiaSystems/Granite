//////////////////////////////////////////////////////////////////////////////
// ClientTokenPerClientRateLimitWebFilter.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public
class ClientTokenPerClientRateLimitWebFilter
    implements WebFilter
{
    public static final String UNKNOWN_KEY     = "unknown";
    public static final int    MAX_FORM_BYTES  = 8192;

    private final String                 path;
    private final FixedWindowRateLimiter limiter;
    private final Logger                 logger;

    public
    ClientTokenPerClientRateLimitWebFilter(String path,FixedWindowRateLimiter limiter)
    {
        this.path    = Objects.requireNonNull(path,"path");
        this.limiter = Objects.requireNonNull(limiter,"limiter");
        this.logger  = LogManager.getLogger(ClientTokenPerClientRateLimitWebFilter.class);
    }

    @Override
    public Mono<Void>
    filter(ServerWebExchange exchange,WebFilterChain chain)
    {
        ServerHttpRequest request = exchange.getRequest();

        if (!path.equals(request.getPath().pathWithinApplication().value()))
            return chain.filter(exchange);

        String basic = fromBasic(request.getHeaders());

        if (basic != null)
            return admit(exchange,chain,basic);

        MediaType contentType = request.getHeaders().getContentType();

        if (!HttpMethod.POST.equals(request.getMethod())
            || contentType == null
            || !MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType))
            return admit(exchange,chain,null);

        return
            DataBufferUtils
                .join(request.getBody(),MAX_FORM_BYTES)
                .map(this::toBytes)
                .defaultIfEmpty(new byte[0])
                .onErrorResume(
                    DataBufferLimitException.class,
                    e ->
                    {
                        exchange.getResponse().setStatusCode(HttpStatus.PAYLOAD_TOO_LARGE);
                        return exchange.getResponse().setComplete().then(Mono.empty());
                    })
                .flatMap(
                    bytes ->
                        admit(
                            exchange
                                .mutate()
                                .request(replay(exchange,bytes))
                                .build(),
                            chain,
                            fromForm(bytes)));
    }

    private Mono<Void>
    admit(ServerWebExchange exchange,WebFilterChain chain,String clientId)
    {
        String key = clientId == null || clientId.isEmpty() ? UNKNOWN_KEY : "id:" + clientId;

        if (limiter.tryAcquire(key))
            return chain.filter(exchange);

        logger.warn("client token rate limit exceeded");
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        return exchange.getResponse().setComplete();
    }

    private String
    fromBasic(HttpHeaders headers)
    {
        String header = headers.getFirst(HttpHeaders.AUTHORIZATION);

        if (header == null || header.length() < 6 || !header.regionMatches(true,0,"Basic ",0,6))
            return null;

        try
        {
            String decoded = new String(Base64.getDecoder().decode(header.substring(6).trim()),StandardCharsets.UTF_8);
            int    colon   = decoded.indexOf(':');

            if (colon < 0)
                return null;

            return URLDecoder.decode(decoded.substring(0,colon),StandardCharsets.UTF_8);
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    private String
    fromForm(byte[] bytes)
    {
        for (String pair : new String(bytes,StandardCharsets.UTF_8).split("&"))
        {
            int    equals = pair.indexOf('=');
            String name   = equals < 0 ? pair : pair.substring(0,equals);

            try
            {
                if ("client_id".equals(URLDecoder.decode(name,StandardCharsets.UTF_8)))
                    return equals < 0 ? null : URLDecoder.decode(pair.substring(equals + 1),StandardCharsets.UTF_8);
            }
            catch (IllegalArgumentException e)
            {
                return null;
            }
        }

        return null;
    }

    private byte[]
    toBytes(DataBuffer buffer)
    {
        try
        {
            byte[] bytes = new byte[buffer.readableByteCount()];

            buffer.read(bytes);
            return bytes;
        }
        finally
        {
            DataBufferUtils.release(buffer);
        }
    }

    private ServerHttpRequest
    replay(ServerWebExchange exchange,byte[] bytes)
    {
        return
            new ServerHttpRequestDecorator(exchange.getRequest())
            {
                @Override
                public Flux<DataBuffer>
                getBody()
                {
                    if (bytes.length == 0)
                        return Flux.empty();

                    return Flux.defer(() -> Flux.just(exchange.getResponse().bufferFactory().wrap(bytes)));
                }
            };
    }
}

//////////////////////////////////////////////////////////////////////////////
