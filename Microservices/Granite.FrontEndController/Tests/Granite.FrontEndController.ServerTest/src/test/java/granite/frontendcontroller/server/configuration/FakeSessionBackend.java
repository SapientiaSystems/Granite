//////////////////////////////////////////////////////////////////////////////
// FakeSessionBackend.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import strata.foundation.core.mapper.ObjectMapperSupplier;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public
class FakeSessionBackend
{
    public static final String SERVICE_TOKEN       = "service.jwt.token";
    public static final String ISSUE_SERVICE_TOKEN = "issue-service-token";

    private final HttpServer         server;
    private final ObjectMapper       mapper;
    private final Map<String,String> responses;
    private final List<String>       paths;
    private final List<String>       authorizations;
    private final List<String>       bodies;
    private final List<String>       serviceTokenAuthorizations;

    public
    FakeSessionBackend()
        throws Exception
    {
        server                     = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        mapper                     = new ObjectMapperSupplier().get();
        responses                  = new ConcurrentHashMap<>();
        paths                      = new CopyOnWriteArrayList<>();
        authorizations             = new CopyOnWriteArrayList<>();
        bodies                     = new CopyOnWriteArrayList<>();
        serviceTokenAuthorizations = new CopyOnWriteArrayList<>();

        responses.put(
            ISSUE_SERVICE_TOKEN,
            "{\"success\":true,\"token\":\"" + SERVICE_TOKEN + "\",\"expiresAt\":\"2099-01-01T00:00:00Z\"}");

        server.createContext("/",exchange ->
        {
            String path = exchange.getRequestURI().getPath();
            String body = new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8);
            String reply = "{\"success\":false}";

            for (Map.Entry<String,String> entry : responses.entrySet())
            {
                if (path.endsWith("/" + entry.getKey()))
                    reply = entry.getValue();
            }

            if (path.endsWith("/" + ISSUE_SERVICE_TOKEN))
                serviceTokenAuthorizations.add(String.valueOf(exchange.getRequestHeaders().getFirst("Authorization")));
            else
            {
                paths.add(path);
                authorizations.add(String.valueOf(exchange.getRequestHeaders().getFirst("Authorization")));
                bodies.add(body);
            }

            byte[] bytes = reply.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type","application/json");
            exchange.sendResponseHeaders(200,bytes.length);

            try (OutputStream out = exchange.getResponseBody())
            {
                out.write(bytes);
            }
        });
        server.start();
    }

    public FakeSessionBackend
    respond(String operation,Object reply)
        throws Exception
    {
        responses.put(operation,mapper.writeValueAsString(reply));
        return this;
    }

    public String
    baseUrl() { return "http://127.0.0.1:" + server.getAddress().getPort(); }

    public List<String>
    paths() { return paths; }

    public List<String>
    authorizations() { return authorizations; }

    public List<String>
    bodies() { return bodies; }

    public List<String>
    serviceTokenAuthorizations() { return serviceTokenAuthorizations; }

    public void
    stop() { server.stop(0); }
}

//////////////////////////////////////////////////////////////////////////////
