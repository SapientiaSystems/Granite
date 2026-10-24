//////////////////////////////////////////////////////////////////////////////
// ResponseCookieAdviceTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.platform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import strata.frontendcontroller.server.platform.UserFrontEndControllerServiceController;
import strata.frontendcontroller.service.requestreply.HandleInputReply;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;
import strata.frontendcontroller.service.requestreply.IFrontEndControllerService;

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
    private IFrontEndControllerService service;
    private MockMvc                    mvc;

    @BeforeEach
    public void
    setUp()
    {
        service = Mockito.mock(IFrontEndControllerService.class);
        mvc =
            MockMvcBuilders
                .standaloneSetup(new UserFrontEndControllerServiceController(service))
                .setControllerAdvice(new ResponseCookieAdvice())
                .build();
    }

    private MvcResult
    call()
        throws Exception
    {
        MvcResult started =
            mvc
                .perform(
                    post("/frontendcontroller-user-service/handle-input")
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
            .when(service.handleInput(Mockito.any(HandleInputRequest.class)))
            .thenAnswer(
                invocation ->
                {
                    SessionRequestContext context = SessionRequestContext.fromCurrentRequest();

                    return
                        CompletableFuture.supplyAsync(
                            () ->
                            {
                                context.addSetCookie(SessionCookies.issue("h",null,Instant.now()));
                                return new HandleInputReply().setSuccess(true);
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
            .when(service.handleInput(Mockito.any(HandleInputRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new HandleInputReply().setSuccess(true)));

        MvcResult result = call();

        assertNull(result.getResponse().getHeader("Set-Cookie"));
        assertNull(result.getResponse().getHeader("Cache-Control"));
    }
}

//////////////////////////////////////////////////////////////////////////////
