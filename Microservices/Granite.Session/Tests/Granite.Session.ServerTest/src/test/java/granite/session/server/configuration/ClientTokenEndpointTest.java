//////////////////////////////////////////////////////////////////////////////
// ClientTokenEndpointTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.configuration;

import granite.client.client.requestreply.IClientCommandServiceClient;
import granite.client.service.requestreply.VerifyClientCredentialsReply;
import granite.session.server.application.IClientTokenIssuer;
import granite.session.server.platform.ClientTokenController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Tag("CommitStage")
public
class ClientTokenEndpointTest
{
    private static final String PATH      = "/session-command-service/oauth2/token";
    private static final String CLIENT_ID = "gc_abcdefghijklmnopqrstuvwxyz234567";

    private final GraniteSessionCommandServerConfiguration subject =
        new GraniteSessionCommandServerConfiguration();

    private IClientCommandServiceClient clientService;
    private MockMvc                     mvc;

    @BeforeEach
    public void
    setUp()
    {
        MockEnvironment environment = new MockEnvironment();

        environment.setActiveProfiles("local");
        clientService = Mockito.mock(IClientCommandServiceClient.class);

        IClientTokenIssuer issuer =
            subject.clientTokenIssuer(
                subject.jwtTokenService(environment,new DefaultResourceLoader()),
                clientService,
                environment);

        mvc = MockMvcBuilders.standaloneSetup(new ClientTokenController(issuer)).build();
        reply(valid());
    }

    @Test
    public void
    successHasNoStoreHeaders()
        throws Exception
    {
        MvcResult result = perform(form("client_credentials",CLIENT_ID,"secret"),null);

        assertEquals(200,result.getResponse().getStatus());
        assertEquals("no-store",result.getResponse().getHeader("Cache-Control"));
        assertEquals("no-cache",result.getResponse().getHeader("Pragma"));
        assertNull(result.getResponse().getHeader("WWW-Authenticate"));
        assertTrue(result.getResponse().getContentAsString().contains("\"token_type\":\"Bearer\""));
        assertTrue(result.getResponse().getContentAsString().contains("\"expires_in\":900"));
        assertTrue(result.getResponse().getContentAsString().contains("\"access_token\":\""));
    }

    @Test
    public void
    basicAuthenticationIsAccepted()
        throws Exception
    {
        MvcResult result = perform(form("client_credentials",null,null),basic(CLIENT_ID + ":secret"));

        assertEquals(200,result.getResponse().getStatus());
    }

    @Test
    public void
    basicCredentialsAreUrlDecoded()
        throws Exception
    {
        MvcResult result = perform(form("client_credentials",null,null),basic(CLIENT_ID + ":a%2Bb%3Ac"));

        assertEquals(200,result.getResponse().getStatus());
        Mockito
            .verify(clientService)
            .verifyClientCredentials(Mockito.argThat(r -> "a+b:c".equals(r.getClientSecret())));
    }

    @Test
    public void
    tokenEndpointErrorCases()
        throws Exception
    {
        assertError(perform(form(null,CLIENT_ID,"secret"),null),400,"invalid_request");
        assertError(perform(form("password",CLIENT_ID,"secret"),null),400,"unsupported_grant_type");
        assertError(perform(form("client_credentials",CLIENT_ID,"secret"),basic(CLIENT_ID + ":secret")),400,"invalid_request");
        assertError(perform(form("client_credentials",null,null),"Basic !!!not-base64"),400,"invalid_request");
        assertError(perform(form("client_credentials",null,null),basic("nocolon")),400,"invalid_request");
        assertError(perform(form("client_credentials",null,null),null),400,"invalid_request");
        assertError(perform(form("client_credentials",CLIENT_ID,null),null),400,"invalid_request");

        MultiValueMap<String,String> duplicated = form("client_credentials",CLIENT_ID,"secret");

        duplicated.add("grant_type","client_credentials");
        assertError(perform(duplicated,null),400,"invalid_request");
        Mockito.verifyNoInteractions(clientService);
    }

    @Test
    public void
    queryStringCredentialsAreRejected()
        throws Exception
    {
        MvcResult started =
            mvc
                .perform(
                    post(PATH + "?client_id=" + CLIENT_ID + "&client_secret=secret")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type","client_credentials"))
                .andReturn();
        MvcResult result  = mvc.perform(asyncDispatch(started)).andReturn();

        assertError(result,400,"invalid_request");
        Mockito.verifyNoInteractions(clientService);
    }

    @Test
    public void
    malformedBasicValuesAreRejected()
        throws Exception
    {
        assertError(perform(form("client_credentials",null,null),basic(CLIENT_ID + ":%zz")),400,"invalid_request");
        assertError(perform(form("client_credentials",null,null),"Basic "),400,"invalid_request");
        Mockito.verifyNoInteractions(clientService);
    }

    @Test
    public void
    jsonBodyIsRejectedWithoutServerError()
        throws Exception
    {
        MvcResult result =
            mvc
                .perform(
                    post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grant_type\":\"client_credentials\"}"))
                .andReturn();

        assertEquals(415,result.getResponse().getStatus());
        Mockito.verifyNoInteractions(clientService);
    }

    @Test
    public void
    invalidCredentialsReturnInvalidClientWithChallenge()
        throws Exception
    {
        reply(new VerifyClientCredentialsReply().setSuccess(true).setValid(false).setFailureMessage("invalid client"));

        MvcResult result = perform(form("client_credentials",CLIENT_ID,"wrong"),null);

        assertError(result,401,"invalid_client");
        assertEquals("Basic realm=\"granite\"",result.getResponse().getHeader(HttpHeaders.WWW_AUTHENTICATE));
        assertFalse(result.getResponse().getContentAsString().contains("wrong"));
    }

    @Test
    public void
    clientServiceFailureReturnsTemporarilyUnavailable()
        throws Exception
    {
        Mockito
            .when(clientService.verifyClientCredentials(Mockito.any()))
            .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("down")));
        assertError(perform(form("client_credentials",CLIENT_ID,"secret"),null),503,"temporarily_unavailable");

        reply(new VerifyClientCredentialsReply().setSuccess(false).setFailureMessage("unable to verify client"));
        assertError(perform(form("client_credentials",CLIENT_ID,"secret"),null),503,"temporarily_unavailable");
    }

    @Test
    public void
    scopeParameterIsIgnored()
        throws Exception
    {
        MultiValueMap<String,String> body = form("client_credentials",CLIENT_ID,"secret");

        body.add("scope","ROLE_ADMIN");
        assertEquals(200,perform(body,null).getResponse().getStatus());
    }

    private MvcResult
    perform(MultiValueMap<String,String> body,String authorization)
        throws Exception
    {
        MockHttpServletRequestBuilder request =
            post(PATH)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .params(body);

        if (authorization != null)
            request.header("Authorization",authorization);

        MvcResult started = mvc.perform(request).andReturn();

        if (started.getRequest().isAsyncStarted())
            return mvc.perform(asyncDispatch(started)).andReturn();

        return started;
    }

    private void
    assertError(MvcResult result,int status,String error)
        throws Exception
    {
        assertEquals(status,result.getResponse().getStatus());
        assertEquals("{\"error\":\"" + error + "\"}",result.getResponse().getContentAsString());
        assertNotNull(result.getResponse().getContentType());
    }

    private void
    reply(VerifyClientCredentialsReply reply)
    {
        Mockito
            .when(clientService.verifyClientCredentials(Mockito.any()))
            .thenReturn(CompletableFuture.completedFuture(reply));
    }

    private static VerifyClientCredentialsReply
    valid()
    {
        return
            new VerifyClientCredentialsReply()
                .setSuccess(true)
                .setValid(true)
                .setClientId(CLIENT_ID)
                .setAuthorities(new TreeSet<>(List.of("ROLE_USER")));
    }

    private static MultiValueMap<String,String>
    form(String grant,String id,String secret)
    {
        MultiValueMap<String,String> body = new LinkedMultiValueMap<>();

        if (grant != null)
            body.add("grant_type",grant);

        if (id != null)
            body.add("client_id",id);

        if (secret != null)
            body.add("client_secret",secret);

        return body;
    }

    private static String
    basic(String credentials)
    {
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}

//////////////////////////////////////////////////////////////////////////////
