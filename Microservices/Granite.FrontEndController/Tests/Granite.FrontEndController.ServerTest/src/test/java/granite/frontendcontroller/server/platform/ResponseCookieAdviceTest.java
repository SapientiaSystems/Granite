//////////////////////////////////////////////////////////////////////////////
// ResponseCookieAdviceTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.platform;

import granite.frontendcontroller.service.requestreply.HandleSessionInputReply;
import granite.frontendcontroller.service.requestreply.HandleSessionInputRequest;
import granite.frontendcontroller.service.requestreply.IGraniteFrontEndControllerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import strata.foundation.core.mapper.ObjectMapperSupplier;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Tag("CommitStage")
public
class ResponseCookieAdviceTest
{
    private IGraniteFrontEndControllerService service;
    private MockMvc                         mvc;

    @BeforeEach
    public void
    setUp()
    {
        service = Mockito.mock(IGraniteFrontEndControllerService.class);
        mvc =
            MockMvcBuilders
                .standaloneSetup(new GraniteFrontEndControllerServiceController(service))
                .addPlaceholderValue("granite.frontendcontroller.service-path","/frontendcontroller-user-service")
                .setControllerAdvice(new ResponseCookieAdvice())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapperSupplier().get()))
                .build();
    }

    private MvcResult
    call()
        throws Exception
    {
        MvcResult started =
            mvc
                .perform(
                    post("/frontendcontroller-user-service/handle-session-input")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();

        started.getAsyncResult();

        return mvc.perform(asyncDispatch(started)).andReturn();
    }

    @Test
    public void
    cookiesAddedOnAnotherThreadAreWrittenToTheResponse()
        throws Exception
    {
        Mockito
            .when(service.handleSessionInput(Mockito.any(HandleSessionInputRequest.class)))
            .thenAnswer(
                invocation ->
                {
                    SessionRequestContext context = SessionRequestContext.fromCurrentRequest();

                    return
                        CompletableFuture.supplyAsync(
                            () ->
                            {
                                context.addSetCookie(SessionCookies.issue("h",null,Instant.now()));
                                return new HandleSessionInputReply().setSuccess(true);
                            });
                });

        MvcResult result = call();

        assertEquals(1,result.getResponse().getHeaders("Set-Cookie").size());
        assertTrue(result.getResponse().getHeader("Set-Cookie").startsWith("GRANITE_SID=h;"));
        assertEquals("no-store",result.getResponse().getHeader("Cache-Control"));
        assertFalse(result.getResponse().getContentAsString().contains("GRANITE_SID"));
    }

    @Test
    public void
    noCookiesMeansNoHeaders()
        throws Exception
    {
        Mockito
            .when(service.handleSessionInput(Mockito.any(HandleSessionInputRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new HandleSessionInputReply().setSuccess(true)));

        MvcResult result = call();

        assertNull(result.getResponse().getHeader("Set-Cookie"));
        assertNull(result.getResponse().getHeader("Cache-Control"));
    }
}

//////////////////////////////////////////////////////////////////////////////
