//////////////////////////////////////////////////////////////////////////////
// GraniteUserFrontEndControllerConfigurationTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import com.sun.net.httpserver.HttpServer;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.service.requestreply.UpdateUsersRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import strata.foundation.core.configuration.IConfiguration;

import javax.net.ssl.SSLContext;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("CommitStage")
public
class GraniteUserFrontEndControllerConfigurationTest
{
    private HttpServer                               userService;
    private List<String>                             authorizations;
    private GraniteUserFrontEndControllerConfiguration subject;

    @BeforeEach
    public void
    setUp()
        throws Exception
    {
        authorizations = new CopyOnWriteArrayList<>();
        userService    = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        userService.createContext(
            "/",
            exchange ->
            {
                byte[] body = "{\"success\":true}".getBytes(StandardCharsets.UTF_8);

                authorizations.add(String.valueOf(exchange.getRequestHeaders().getFirst("Authorization")));
                exchange.getResponseHeaders().add("Content-Type","application/json");
                exchange.sendResponseHeaders(200,body.length);

                try (OutputStream out = exchange.getResponseBody())
                {
                    out.write(body);
                }
            });
        userService.start();

        IConfiguration configuration = Mockito.mock(IConfiguration.class);
        SslBundles     sslBundles    = Mockito.mock(SslBundles.class);
        SslBundle      bundle        = Mockito.mock(SslBundle.class);

        Mockito
            .when(configuration.getProperty("user-command-service.base-url"))
            .thenReturn("http://127.0.0.1:" + userService.getAddress().getPort() + "/user-command-service");
        Mockito.when(sslBundles.getBundle("granite-services")).thenReturn(bundle);
        Mockito.when(bundle.createSslContext()).thenReturn(SSLContext.getDefault());

        subject = new GraniteUserFrontEndControllerConfiguration();
        ReflectionTestUtils.setField(subject,"configuration",configuration);
        ReflectionTestUtils.setField(subject,"sslBundles",sslBundles);
    }

    @AfterEach
    public void
    tearDown()
    {
        RequestContextHolder.resetRequestAttributes();
        userService.stop(0);
    }

    @Test
    public void
    userCommandClientForwardsCallersBearerToken()
    {
        MockHttpServletRequest inbound = new MockHttpServletRequest("POST","/frontendcontroller-user-service/handle-input");

        inbound.addHeader("Authorization","Bearer caller.jwt.token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(inbound));

        IUserCommandServiceClient client = subject.userCommandServiceClient();

        client.updateUsers(new UpdateUsersRequest()).toCompletableFuture().join();

        assertEquals(List.of("Bearer caller.jwt.token"),authorizations);
    }
}

//////////////////////////////////////////////////////////////////////////////
