//////////////////////////////////////////////////////////////////////////////
// GraniteFrontEndControllerConfigurationTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import com.sun.net.httpserver.HttpServer;
import granite.frontendcontroller.service.requestreply.HandleUsersInputReply;
import granite.frontendcontroller.service.requestreply.HandleUsersInputRequest;
import granite.frontendcontroller.service.requestreply.IGraniteFrontEndControllerService;
import granite.frontendcontroller.service.requestreply.UsersInput;
import granite.user.service.requestreply.UpdateUserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class GraniteFrontEndControllerConfigurationTest
{
    private HttpServer                             userService;
    private List<String>                           authorizations;
    private GraniteFrontEndControllerConfiguration subject;

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
                String path = exchange.getRequestURI().getPath();
                byte[] body =
                    (path.endsWith("/issue-service-token")
                        ? "{\"success\":true,\"token\":\"service.jwt.token\",\"expiresAt\":\"2099-01-01T00:00:00Z\"}"
                        : "{\"success\":true}").getBytes(StandardCharsets.UTF_8);

                authorizations.add(path + " " + exchange.getRequestHeaders().getFirst("Authorization"));
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
        String         baseUrl       = "http://127.0.0.1:" + userService.getAddress().getPort();

        Mockito.when(configuration.getProperty("user-command-service.base-url")).thenReturn(baseUrl + "/user-command-service");
        Mockito.when(configuration.getProperty("user-query-service.base-url")).thenReturn(baseUrl + "/user-query-service");
        Mockito.when(configuration.getProperty("session-command-service.base-url")).thenReturn(baseUrl + "/session-command-service");
        Mockito.when(configuration.getProperty("session-query-service.base-url")).thenReturn(baseUrl + "/session-query-service");
        Mockito.when(configuration.getProperty("granite.frontendcontroller.service-client.id","granite-frontendcontroller")).thenReturn("granite-frontendcontroller");
        Mockito.when(configuration.getProperty("granite.frontendcontroller.service-client.secret","")).thenReturn("fec-secret");
        Mockito.when(sslBundles.getBundle("granite-services")).thenReturn(bundle);
        Mockito.when(bundle.createSslContext()).thenReturn(SSLContext.getDefault());

        subject = new GraniteFrontEndControllerConfiguration();
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
    usersServiceSendsServiceTokenNotCallersBearer()
    {
        MockHttpServletRequest inbound = new MockHttpServletRequest("POST","/frontendcontroller-user-service/handle-users-input");

        inbound.addHeader("Authorization","Bearer caller.jwt.token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(inbound));

        IGraniteFrontEndControllerService service =
            subject.frontEndControllerService(Runnable::run,new ModelMapper(),subject.applicationRoles());

        RequestContextHolder.resetRequestAttributes();

        HandleUsersInputReply reply =
            service
                .handleUsersInput(
                    new HandleUsersInputRequest()
                        .setInput(
                            new UsersInput()
                                .setUsersToUpdate(List.of(new UpdateUserData().setUserId(2L)))
                                .setSource("User.All")
                                .setCommand("Save")
                                .setDestination("UsersEditableView")))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess());
        assertTrue(authorizations.size() >= 3);
        assertEquals(
            List.of("/issue-service-token null"),
            authorizations
                .stream()
                .filter(a -> a.contains("issue-service-token"))
                .map(a -> a.substring(a.lastIndexOf('/')))
                .distinct()
                .toList());
        assertEquals(
            List.of("Bearer service.jwt.token"),
            authorizations
                .stream()
                .filter(a -> !a.contains("issue-service-token"))
                .map(a -> a.substring(a.indexOf(' ') + 1))
                .distinct()
                .toList());
    }
}

//////////////////////////////////////////////////////////////////////////////
