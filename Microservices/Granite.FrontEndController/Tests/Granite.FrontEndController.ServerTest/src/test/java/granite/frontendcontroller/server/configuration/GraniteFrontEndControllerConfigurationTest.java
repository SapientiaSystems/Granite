//////////////////////////////////////////////////////////////////////////////
// GraniteFrontEndControllerConfigurationTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import com.sun.net.httpserver.HttpServer;
import granite.frontendcontroller.server.platform.SessionCookies;
import granite.frontendcontroller.server.platform.SessionRequestContextTest;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
    private IConfiguration                         configuration;
    private SslBundles                             sslBundles;
    private SslBundle                              bundle;
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

        configuration = Mockito.mock(IConfiguration.class);
        sslBundles    = Mockito.mock(SslBundles.class);
        bundle        = Mockito.mock(SslBundle.class);

        String baseUrl = "http://127.0.0.1:" + userService.getAddress().getPort();

        Mockito.when(configuration.getProperty("user-command-service.base-url")).thenReturn(baseUrl + "/user-command-service");
        Mockito.when(configuration.getProperty("user-query-service.base-url")).thenReturn(baseUrl + "/user-query-service");
        Mockito.when(configuration.getProperty("session-command-service.base-url")).thenReturn(baseUrl + "/session-command-service");
        Mockito.when(configuration.getProperty("session-query-service.base-url")).thenReturn(baseUrl + "/session-query-service");
        Mockito.when(configuration.getProperty("granite.frontendcontroller.service-client.id","granite-frontendcontroller")).thenReturn("granite-frontendcontroller");
        Mockito.when(configuration.getProperty("granite.frontendcontroller.service-client.secret","")).thenReturn("fec-secret");
        Mockito.when(configuration.getProperty("granite.frontendcontroller.ssl-bundle","granite-services")).thenReturn("granite-services");
        Mockito.when(configuration.getProperty("granite.cookies.prefix","GRANITE")).thenReturn("GRANITE");
        Mockito.when(configuration.getProperty("granite.session.login.password.enabled","true")).thenReturn("true");
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
        SecurityContextHolder.clearContext();
        userService.stop(0);
    }

    @Test
    public void
    configuredCookiePrefixReachesTheContextCookies()
    {
        Mockito.when(configuration.getProperty("granite.cookies.prefix","GRANITE")).thenReturn("POPLAR");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest("POST","/x")));

        SessionCookies cookies = subject.createContext(new ModelMapper(),Runnable::run,subject.applicationRoles()).getSessionCookies();

        assertEquals("__Host-POPLAR_SID",cookies.getSessionName());
        assertEquals("__Host-POPLAR_DID",cookies.getDeviceName());
        assertEquals("__Host-POPLAR_LOGIN",cookies.getLoginName());
    }

    @Test
    public void
    cookiePrefixDefaultsToGranite()
    {
        Mockito.when(configuration.getProperty("granite.cookies.prefix","GRANITE")).thenAnswer(i -> i.getArgument(1));

        assertEquals("__Host-GRANITE_SID",subject.cookieNames().getSession());
    }

    @Test
    public void
    usersServiceSendsServiceTokenNotCallersBearer()
    {
        MockHttpServletRequest inbound = new MockHttpServletRequest("POST","/frontendcontroller-user-service/handle-users-input");

        inbound.addHeader("Authorization","Bearer caller.jwt.token");

        Jwt caller = SessionRequestContextTest.jwt(11L,1L);

        inbound.setUserPrincipal(new JwtAuthenticationToken(caller,List.of(),caller.getSubject()));
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
        assertServiceTokenOnly();
    }

    @Test
    public void
    clientsUseConfiguredSslBundle()
    {
        Mockito.when(configuration.getProperty("granite.frontendcontroller.ssl-bundle","granite-services")).thenReturn("app-services");
        Mockito.when(sslBundles.getBundle("app-services")).thenReturn(bundle);

        MockHttpServletRequest inbound = new MockHttpServletRequest("POST","/frontendcontroller-user-service/handle-users-input");
        Jwt                    caller  = SessionRequestContextTest.jwt(11L,1L);

        inbound.setUserPrincipal(new JwtAuthenticationToken(caller,List.of(),caller.getSubject()));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(inbound));

        IGraniteFrontEndControllerService service =
            subject.frontEndControllerService(Runnable::run,new ModelMapper(),subject.applicationRoles());

        RequestContextHolder.resetRequestAttributes();

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

        assertTrue(authorizations.stream().anyMatch(a -> a.contains("/issue-service-token ")));
        Mockito.verify(sslBundles,Mockito.atLeast(4)).getBundle("app-services");

        Mockito.clearInvocations(sslBundles);
        subject.createServiceTokenClient();

        Mockito.verify(sslBundles).getBundle("app-services");
        Mockito.verify(sslBundles,Mockito.never()).getBundle("granite-services");
    }

    @Test
    public void
    clientsUseGraniteSslBundleByDefault()
    {
        subject.createServiceTokenClient();
        subject.createUserQueryClient();

        Mockito.verify(sslBundles,Mockito.atLeast(2)).getBundle("granite-services");
        Mockito.verify(sslBundles,Mockito.never()).getBundle(Mockito.argThat(name -> !"granite-services".equals(name)));
    }

    private void
    assertServiceTokenOnly()
    {
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
