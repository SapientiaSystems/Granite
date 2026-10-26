//////////////////////////////////////////////////////////////////////////////
// FrontEndInputKeyWebFilter.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public
class FrontEndInputKeyWebFilter
    implements WebFilter
{
    public static final String KEY_ATTRIBUTE        = "granite.gateway.front-end-input-key";
    public static final String GET_SESSION_OUTPUT   = "/get-session-output";
    public static final String HANDLE_SESSION_INPUT = "/handle-session-input";
    public static final String SEPARATOR            = "->";

    public static final Set<String> PATHS =
        Set.of(
            "/frontendcontroller-user-service" + GET_SESSION_OUTPUT,
            "/frontendcontroller-user-service" + HANDLE_SESSION_INPUT,
            "/frontendcontroller-admin-service" + GET_SESSION_OUTPUT,
            "/frontendcontroller-admin-service" + HANDLE_SESSION_INPUT);

    private final int          maxBytes;
    private final ObjectMapper mapper;

    public
    FrontEndInputKeyWebFilter(int maxBytes)
    {
        this.maxBytes = maxBytes;
        this.mapper   = new ObjectMapper();
    }

    @Override
    public Mono<Void>
    filter(ServerWebExchange exchange,WebFilterChain chain)
    {
        ServerHttpRequest request = exchange.getRequest();
        String            path    = request.getPath().pathWithinApplication().value();

        if (!HttpMethod.POST.equals(request.getMethod()) || !PATHS.contains(path))
            return chain.filter(exchange);

        return
            DataBufferUtils
                .join(request.getBody(),maxBytes)
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
                    {
                        String key = readKey(path,bytes);

                        if (key != null)
                            exchange.getAttributes().put(KEY_ATTRIBUTE,key);

                        return
                            chain.filter(
                                exchange
                                    .mutate()
                                    .request(replay(exchange,bytes))
                                    .build());
                    });
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

    private String
    readKey(String path,byte[] bytes)
    {
        try
        {
            JsonNode root = mapper.readTree(bytes);

            if (root == null || !root.isObject())
                return null;

            JsonNode     node  = path.endsWith(HANDLE_SESSION_INPUT) ? root.path("input") : root;
            List<String> parts = new ArrayList<>();

            for (String name : List.of("source","command","destination"))
            {
                JsonNode part = node.path(name);

                if (part.isTextual())
                {
                    if (part.asText().contains(SEPARATOR))
                        return null;

                    parts.add(part.asText());
                }
                else if (!part.isMissingNode() && !part.isNull())
                    return null;
            }

            return parts.isEmpty() ? null : String.join(SEPARATOR,parts);
        }
        catch (IOException e)
        {
            return null;
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
